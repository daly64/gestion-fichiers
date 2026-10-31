package com.example.gestionfichiers

import java.io.File

/** Opérations sur les fichiers (F1, F4, F5, F6, F7). Sans interface : testable seul. */
object FileOps {

    /** F1 : contenu d'un dossier, dossiers d'abord puis fichiers, triés par nom. */
    fun list(dir: File): List<File> =
        (dir.listFiles()?.toList() ?: emptyList())
            .sortedWith(compareBy<File>({ !it.isDirectory }, { it.name.lowercase() }))

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

    /** F4 : copie (récursive pour les dossiers). */
    fun copy(sources: List<File>, targetDir: File): Result<Unit> = runCatching {
        sources.forEach { src ->
            val dest = checkDestination(src, targetDir)
            if (src.isDirectory) src.copyRecursively(dest, overwrite = false)
            else src.copyTo(dest, overwrite = false)
        }
    }

    /** F5 : déplacement. renameTo si possible, sinon copie puis suppression. */
    fun move(sources: List<File>, targetDir: File): Result<Unit> = runCatching {
        sources.forEach { src ->
            val dest = checkDestination(src, targetDir)
            if (!src.renameTo(dest)) {
                if (src.isDirectory) src.copyRecursively(dest, overwrite = false)
                else src.copyTo(dest, overwrite = false)
                check(src.deleteRecursively()) { "Impossible de supprimer l'original « ${src.name} »" }
            }
        }
    }

    /** F6 : suppression (récursive pour les dossiers). */
    fun delete(items: List<File>): Result<Unit> = runCatching {
        items.forEach { file ->
            check(file.deleteRecursively()) { "Suppression impossible : « ${file.name} »" }
        }
    }

    /** F7 : création d'un dossier avec validation du nom. */
    fun createFolder(parent: File, rawName: String): Result<File> = runCatching {
        val name = rawName.trim()
        require(name.isNotEmpty()) { "Le nom ne peut pas être vide" }
        require('/' !in name) { "Le nom ne peut pas contenir « / »" }
        val dir = File(parent, name)
        require(!dir.exists()) { "« $name » existe déjà" }
        check(dir.mkdir()) { "Création impossible" }
        dir
    }
}
