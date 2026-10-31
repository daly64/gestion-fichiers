package com.example.gestionfichiers

import java.io.File

/** Élément de la corbeille : fichier stocké, chemin d'origine, date de suppression. */
data class TrashItem(val file: File, val originalPath: String, val deletedAt: Long) {
    /** Nom d'origine, sans le préfixe horodaté ajouté dans la corbeille. */
    val displayName: String get() = file.name.substringAfter('_')
}

/**
 * F10 : corbeille dans le dossier caché « .Corbeille » à la racine du stockage.
 * Chaque élément « <horodatage>_<nom> » est accompagné d'un fichier « .info » :
 * ligne 1 = chemin d'origine, ligne 2 = date de suppression (millisecondes).
 */
class Trash(root: File) {

    val dir = File(root, FileOps.TRASH_DIR_NAME)

    private fun infoFor(f: File) = File(dir, f.name + ".info")

    fun list(): List<TrashItem> =
        (dir.listFiles()?.toList() ?: emptyList())
            .filter { !it.name.endsWith(".info") && it.name != ".nomedia" }
            .map { f ->
                val lines = infoFor(f).takeIf { it.exists() }?.readLines() ?: emptyList()
                TrashItem(
                    file = f,
                    originalPath = lines.getOrNull(0).orEmpty(),
                    deletedAt = lines.getOrNull(1)?.toLongOrNull() ?: f.lastModified()
                )
            }
            .sortedByDescending { it.deletedAt }

    fun moveToTrash(items: List<File>, progress: Progress): Result<Unit> = runCatching {
        check(dir.isDirectory || dir.mkdirs()) { "Impossible de créer la corbeille" }
        File(dir, ".nomedia").createNewFile() // évite que la galerie affiche la corbeille
        val trashPath = dir.canonicalPath
        items.forEach { src ->
            require(!src.canonicalPath.startsWith(trashPath)) { "« ${src.name} » est déjà dans la corbeille" }
            val now = System.currentTimeMillis()
            val dest = File(dir, "${now}_${src.name}")
            val original = src.absolutePath
            FileOps.moveTo(src, dest, progress)
            infoFor(dest).writeText("$original\n$now\n")
        }
    }

    fun restore(items: List<TrashItem>, progress: Progress): Result<Unit> = runCatching {
        items.forEach { item ->
            require(item.originalPath.isNotEmpty()) { "Emplacement d'origine inconnu : « ${item.displayName} »" }
            val original = File(item.originalPath)
            original.parentFile?.let { check(it.isDirectory || it.mkdirs()) { "Dossier d'origine introuvable" } }
            FileOps.moveTo(item.file, freeName(original), progress)
            infoFor(item.file).delete()
        }
    }

    fun deleteForever(items: List<TrashItem>): Result<Unit> = runCatching {
        items.forEach { item ->
            check(item.file.deleteRecursively()) { "Suppression impossible : « ${item.displayName} »" }
            infoFor(item.file).delete()
        }
    }

    fun empty(): Result<Unit> = deleteForever(list())

    /** Efface les éléments supprimés depuis plus de [days] jours. Renvoie leur nombre. */
    fun purgeOlderThan(days: Int): Int {
        val limit = System.currentTimeMillis() - days * 24L * 60 * 60 * 1000
        val old = list().filter { it.deletedAt < limit }
        deleteForever(old)
        return old.size
    }

    /** « photo.jpg » pris ? → « photo (restauré).jpg », puis « photo (restauré 2).jpg »… */
    private fun freeName(f: File): File {
        if (!f.exists()) return f
        val hasExt = f.isFile && f.extension.isNotEmpty()
        val base = if (hasExt) f.nameWithoutExtension else f.name
        val ext = if (hasExt) ".${f.extension}" else ""
        var i = 1
        while (true) {
            val suffix = if (i == 1) " (restauré)" else " (restauré $i)"
            val candidate = File(f.parentFile, base + suffix + ext)
            if (!candidate.exists()) return candidate
            i++
        }
    }
}
