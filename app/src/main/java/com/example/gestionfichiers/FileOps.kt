package com.example.gestionfichiers

import java.io.File
import java.io.InputStream
import java.io.OutputStream

/** Levée quand l'utilisateur touche « Annuler » pendant une opération (F11). */
class CancelledException : Exception("Opération annulée")

/**
 * Suivi d'avancement (F11) : total en octets, octets traités, fichier en cours.
 * [isCancelled] est vérifié entre deux fichiers.
 */
class Progress(
    val total: Long,
    private val onUpdate: (done: Long, total: Long, current: String) -> Unit = { _, _, _ -> },
    private val isCancelled: () -> Boolean = { false }
) {
    var done = 0L
        private set

    fun add(bytes: Long, current: String) {
        done += bytes
        onUpdate(done, total, current)
    }

    fun checkCancelled() {
        if (isCancelled()) throw CancelledException()
    }

    companion object {
        val NONE get() = Progress(0)
    }
}

/** Opérations sur les fichiers. Sans interface : testable seul. */
object FileOps {

    const val TRASH_DIR_NAME = ".Corbeille"

    /** F1 / EF-12 : contenu d'un dossier, dossiers d'abord puis fichiers, dans l'ordre demandé. */
    fun list(dir: File, sortBy: SortBy = SortBy.NAME, descending: Boolean = false): List<File> =
        (dir.listFiles()?.toList() ?: emptyList())
            .filter { it.name != TRASH_DIR_NAME }
            .sortedWith(comparator(sortBy, descending))

    /** EF-12 : dossiers toujours en premier ; à critère égal, ordre alphabétique. */
    fun comparator(sortBy: SortBy, descending: Boolean): Comparator<File> {
        val byName = compareBy<File> { it.name.lowercase() }
        val key: Comparator<File> = when (sortBy) {
            SortBy.NAME -> byName
            SortBy.DATE -> compareBy<File> { it.lastModified() }.then(byName)
            SortBy.SIZE -> compareBy<File> { if (it.isDirectory) 0L else it.length() }.then(byName)
            SortBy.TYPE -> compareBy<File> { if (it.isDirectory) "" else it.extension.lowercase() }.then(byName)
        }
        return compareBy<File> { !it.isDirectory }.then(if (descending) key.reversed() else key)
    }

    /** Copie un flux avec avancement (partagé par la copie et les archives ZIP). */
    fun copyStream(input: InputStream, output: OutputStream, progress: Progress, label: String) {
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val n = input.read(buffer)
            if (n < 0) break
            output.write(buffer, 0, n)
            progress.add(n.toLong(), label)
        }
    }

    /** « Archive.zip » pris ? → « Archive (2).zip », puis « Archive (3).zip »… */
    fun freeName(dir: File, name: String): File {
        val first = File(dir, name)
        if (!first.exists()) return first
        val dot = name.lastIndexOf('.')
        val (base, ext) = if (dot > 0) name.substring(0, dot) to name.substring(dot) else name to ""
        var i = 2
        while (true) {
            val candidate = File(dir, "$base ($i)$ext")
            if (!candidate.exists()) return candidate
            i++
        }
    }

    /** Taille totale (récursive) d'une liste d'éléments, pour la barre d'avancement. */
    fun sizeOf(files: List<File>): Long = files.sumOf { f ->
        if (f.isDirectory) f.walkTopDown().filter { it.isFile }.sumOf { it.length() } else f.length()
    }

    private fun checkDestination(src: File, targetDir: File): File {
        val origin = src.canonicalFile
        val target = targetDir.canonicalFile
        require(!(src.isDirectory && (target == origin || target.path.startsWith(origin.path + File.separator)))) {
            "Impossible de placer « ${src.name} » dans lui-même"
        }
        val dest = File(targetDir, src.name)
        require(!dest.exists()) { "« ${dest.name} » existe déjà ici" }
        return dest
    }

    /** F11 : copie récursive avec avancement, annulable entre deux fichiers. */
    private fun copyWithProgress(src: File, dest: File, progress: Progress) {
        progress.checkCancelled()
        if (src.isDirectory) {
            check(dest.isDirectory || dest.mkdirs()) { "Création impossible : « ${dest.name} »" }
            src.listFiles()?.forEach { child -> copyWithProgress(child, File(dest, child.name), progress) }
        } else {
            src.inputStream().use { input ->
                dest.outputStream().use { output -> copyStream(input, output, progress, src.name) }
            }
            dest.setLastModified(src.lastModified())
        }
    }

    /** Copie d'un élément vers un chemin précis (utilisé aussi par la corbeille). */
    fun copyTree(src: File, dest: File, progress: Progress) = copyWithProgress(src, dest, progress)

    /** Déplace un élément : renameTo si possible (instantané), sinon copie puis suppression. */
    fun moveTo(src: File, dest: File, progress: Progress) {
        progress.checkCancelled()
        val size = sizeOf(listOf(src))
        if (src.renameTo(dest)) {
            progress.add(size, src.name)
        } else {
            copyWithProgress(src, dest, progress)
            check(src.deleteRecursively()) { "Impossible de supprimer l'original « ${src.name} »" }
        }
    }

    /** F4 : copie (récursive pour les dossiers). */
    fun copy(sources: List<File>, targetDir: File, progress: Progress = Progress.NONE): Result<Unit> = runCatching {
        sources.forEach { src -> copyWithProgress(src, checkDestination(src, targetDir), progress) }
    }

    /** F5 : déplacement. */
    fun move(sources: List<File>, targetDir: File, progress: Progress = Progress.NONE): Result<Unit> = runCatching {
        sources.forEach { src -> moveTo(src, checkDestination(src, targetDir), progress) }
    }

    /** Suppression définitive (utilisée par la corbeille). */
    fun delete(items: List<File>): Result<Unit> = runCatching {
        items.forEach { file ->
            check(file.deleteRecursively()) { "Suppression impossible : « ${file.name} »" }
        }
    }

    private fun validName(rawName: String): String {
        val name = rawName.trim()
        require(name.isNotEmpty()) { "Le nom ne peut pas être vide" }
        require('/' !in name) { "Le nom ne peut pas contenir « / »" }
        require(name != "." && name != "..") { "Ce nom n'est pas autorisé" }
        return name
    }

    /** F7 : création d'un dossier avec validation du nom. */
    fun createFolder(parent: File, rawName: String): Result<File> = runCatching {
        val name = validName(rawName)
        val dir = File(parent, name)
        require(!dir.exists()) { "« $name » existe déjà" }
        check(dir.mkdir()) { "Création impossible" }
        dir
    }

    /** F8 : renommer un fichier ou un dossier. */
    fun rename(file: File, rawName: String): Result<File> = runCatching {
        val name = validName(rawName)
        if (name == file.name) return@runCatching file
        val dest = File(file.parentFile, name)
        // Autorise un simple changement de majuscules (même fichier sur certains stockages)
        require(!dest.exists() || dest.name.equals(file.name, ignoreCase = true)) { "« $name » existe déjà" }
        check(file.renameTo(dest)) { "Renommage impossible" }
        dest
    }

    /** F9 : recherche par nom dans un dossier et ses sous-dossiers. */
    fun search(root: File, query: String, limit: Int = 500, isCancelled: () -> Boolean = { false }): List<File> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return root.walkTopDown()
            .onEnter { it.name != TRASH_DIR_NAME && !isCancelled() }
            .filter { it != root && it.name.contains(q, ignoreCase = true) }
            .take(limit)
            .sortedWith(compareBy<File>({ !it.isDirectory }, { it.name.lowercase() }))
            .toList()
    }
}
