package com.example.gestionfichiers

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** EF-17 / EF-18 : compression et extraction d'archives ZIP. */
object Zip {

    /** Les archives créées sous Windows utilisent souvent l'ancien encodage CP437 pour les noms. */
    private val FALLBACK_CHARSET: Charset = Charset.forName("CP437")

    fun isZip(file: File) = file.isFile && file.extension.equals("zip", ignoreCase = true)

    /** Compresse [sources] dans [dest]. En cas d'erreur ou d'annulation, l'archive partielle est effacée. */
    fun compress(sources: List<File>, dest: File, progress: Progress): Result<Unit> = runCatching {
        try {
            ZipOutputStream(BufferedOutputStream(dest.outputStream())).use { zos ->
                sources.forEach { src -> addEntry(zos, src, src.name, progress) }
            }
        } catch (e: Throwable) {
            dest.delete()
            throw e
        }
    }

    private fun addEntry(zos: ZipOutputStream, file: File, path: String, progress: Progress) {
        progress.checkCancelled()
        if (file.isDirectory) {
            zos.putNextEntry(ZipEntry("$path/").apply { time = file.lastModified() })
            zos.closeEntry()
            file.listFiles()?.forEach { child -> addEntry(zos, child, "$path/${child.name}", progress) }
        } else {
            zos.putNextEntry(ZipEntry(path).apply { time = file.lastModified() })
            file.inputStream().use { input -> FileOps.copyStream(input, zos, progress, file.name) }
            zos.closeEntry()
        }
    }

    /** Taille décompressée totale, pour la barre d'avancement. */
    fun uncompressedSize(zip: File): Long =
        listOf(Charsets.UTF_8, FALLBACK_CHARSET).firstNotNullOfOrNull { cs ->
            runCatching {
                ZipFile(zip, cs).use { z -> z.entries().asSequence().sumOf { it.size.coerceAtLeast(0) } }
            }.getOrNull()
        } ?: zip.length()

    /** Extrait [zip] dans [destDir] (créé ici). En cas d'erreur ou d'annulation, [destDir] est effacé. */
    fun extract(zip: File, destDir: File, progress: Progress): Result<Unit> = runCatching {
        check(destDir.mkdirs()) { "Création impossible : « ${destDir.name} »" }
        try {
            try {
                extractWith(zip, destDir, progress, Charsets.UTF_8)
            } catch (e: IllegalArgumentException) {
                // Nom de fichier illisible en UTF-8 : on recommence avec l'encodage Windows
                destDir.listFiles()?.forEach { it.deleteRecursively() }
                extractWith(zip, destDir, progress, FALLBACK_CHARSET)
            }
        } catch (e: Throwable) {
            destDir.deleteRecursively()
            throw e
        }
    }

    private fun extractWith(zip: File, destDir: File, progress: Progress, charset: Charset) {
        val base = destDir.canonicalPath + File.separator
        ZipInputStream(BufferedInputStream(zip.inputStream()), charset).use { zis ->
            while (true) {
                val entry = zis.nextEntry ?: break
                progress.checkCancelled()
                val out = File(destDir, entry.name)
                // Protection « zip slip » : aucun fichier ne doit sortir du dossier d'extraction
                require(out.canonicalPath.startsWith(base) || out.canonicalPath == destDir.canonicalPath) {
                    "Archive refusée : chemin dangereux « ${entry.name} »"
                }
                if (entry.isDirectory) {
                    check(out.isDirectory || out.mkdirs()) { "Création impossible : « ${out.name} »" }
                } else {
                    out.parentFile?.let { check(it.isDirectory || it.mkdirs()) { "Création impossible : « ${it.name} »" } }
                    out.outputStream().use { output -> FileOps.copyStream(zis, output, progress, out.name) }
                    if (entry.time > 0) out.setLastModified(entry.time)
                }
                zis.closeEntry()
            }
        }
    }
}
