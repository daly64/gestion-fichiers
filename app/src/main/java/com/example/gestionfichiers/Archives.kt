package com.example.gestionfichiers

import com.github.junrar.Archive
import com.github.junrar.UnrarCallback
import com.github.junrar.exception.RarException
import com.github.junrar.exception.UnsupportedRarV5Exception
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.apache.commons.compress.PasswordRequiredException
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import net.lingala.zip4j.ZipFile as Zip4jFile
import net.lingala.zip4j.exception.ZipException as Zip4jException
import net.lingala.zip4j.io.inputstream.ZipInputStream as Zip4jInputStream
import net.lingala.zip4j.io.outputstream.ZipOutputStream as Zip4jOutputStream

/** L'archive est protégée : il faut demander le mot de passe à l'utilisateur. */
class PasswordNeededException : Exception("Cette archive est protégée par un mot de passe")

/** Le mot de passe fourni ne permet pas d'ouvrir l'archive. */
class WrongPasswordException : Exception("Mot de passe incorrect")

enum class ArchiveFormat(val extension: String, val label: String) {
    ZIP("zip", "ZIP"),
    SEVEN_Z("7z", "7Z"),
    RAR("rar", "RAR");

    companion object {
        fun of(file: File): ArchiveFormat? =
            if (!file.isFile) null else entries.firstOrNull { file.extension.equals(it.extension, ignoreCase = true) }
    }
}

/**
 * EF-17 / EF-18 / EF-21 : compression (ZIP, ZIP avec mot de passe, 7Z)
 * et extraction (ZIP, 7Z, RAR, avec ou sans mot de passe).
 * En cas d'erreur ou d'annulation, le résultat partiel est effacé.
 */
object Archives {

    /** Les archives créées sous Windows utilisent souvent l'ancien encodage CP437 pour les noms. */
    private val FALLBACK_CHARSET: Charset = Charset.forName("CP437")

    fun isArchive(file: File) = ArchiveFormat.of(file) != null

    // ------------------------------------------------------------------
    // Compression
    // ------------------------------------------------------------------

    /** Fichiers à archiver, avec leur chemin dans l'archive (les dossiers finissent par « / »). */
    private fun entriesOf(sources: List<File>): List<Pair<File, String>> {
        val out = mutableListOf<Pair<File, String>>()
        fun walk(f: File, path: String) {
            if (f.isDirectory) {
                out += f to "$path/"
                f.listFiles()?.forEach { walk(it, "$path/${it.name}") }
            } else {
                out += f to path
            }
        }
        sources.forEach { walk(it, it.name) }
        return out
    }

    fun compress(
        sources: List<File>,
        dest: File,
        format: ArchiveFormat,
        password: String?,
        progress: Progress
    ): Result<Unit> = runCatching {
        try {
            when {
                format == ArchiveFormat.SEVEN_Z -> compress7z(sources, dest, progress)
                !password.isNullOrEmpty() -> compressZipEncrypted(sources, dest, password, progress)
                else -> compressZip(sources, dest, progress)
            }
        } catch (e: Throwable) {
            dest.delete()
            throw e
        }
    }

    private fun compressZip(sources: List<File>, dest: File, progress: Progress) {
        ZipOutputStream(BufferedOutputStream(dest.outputStream())).use { zos ->
            entriesOf(sources).forEach { (file, path) ->
                progress.checkCancelled()
                zos.putNextEntry(ZipEntry(path).apply { time = file.lastModified() })
                if (file.isFile) file.inputStream().use { FileOps.copyStream(it, zos, progress, file.name) }
                zos.closeEntry()
            }
        }
    }

    /** ZIP chiffré en AES-256 (s'ouvre avec 7-Zip, WinRAR, et la plupart des gestionnaires de fichiers). */
    private fun compressZipEncrypted(sources: List<File>, dest: File, password: String, progress: Progress) {
        Zip4jOutputStream(BufferedOutputStream(dest.outputStream()), password.toCharArray()).use { zos ->
            entriesOf(sources).forEach { (file, path) ->
                progress.checkCancelled()
                val params = ZipParameters().apply {
                    fileNameInZip = path
                    lastModifiedFileTime = file.lastModified()
                    if (file.isFile) {
                        isEncryptFiles = true
                        encryptionMethod = EncryptionMethod.AES
                        aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
                    }
                }
                zos.putNextEntry(params)
                if (file.isFile) file.inputStream().use { FileOps.copyStream(it, zos, progress, file.name) }
                zos.closeEntry()
            }
        }
    }

    private fun compress7z(sources: List<File>, dest: File, progress: Progress) {
        SevenZOutputFile(dest).use { out ->
            entriesOf(sources).forEach { (file, path) ->
                progress.checkCancelled()
                val entry = out.createArchiveEntry(file, path.removeSuffix("/"))
                out.putArchiveEntry(entry)
                if (file.isFile) {
                    file.inputStream().use { input ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val n = input.read(buffer)
                            if (n < 0) break
                            out.write(buffer, 0, n)
                            progress.add(n.toLong(), file.name)
                        }
                    }
                }
                out.closeArchiveEntry()
            }
            out.finish()
        }
    }

    // ------------------------------------------------------------------
    // Extraction
    // ------------------------------------------------------------------

    /** Taille décompressée totale, pour la barre d'avancement (taille de l'archive si inconnue). */
    fun uncompressedSize(file: File, password: String?): Long = runCatching {
        when (ArchiveFormat.of(file)) {
            ArchiveFormat.ZIP -> Zip4jFile(file).use { z -> z.fileHeaders.sumOf { it.uncompressedSize.coerceAtLeast(0) } }
            ArchiveFormat.SEVEN_Z -> open7z(file, password).use { z -> z.entries.sumOf { it.size.coerceAtLeast(0) } }
            ArchiveFormat.RAR -> openRar(file, password).use { a -> a.fileHeaders.sumOf { it.fullUnpackSize.coerceAtLeast(0) } }
            null -> file.length()
        }
    }.getOrDefault(file.length()).takeIf { it > 0 } ?: file.length()

    /** Extrait [archive] dans [destDir] (créé ici). Lève [PasswordNeededException] ou [WrongPasswordException]. */
    fun extract(archive: File, destDir: File, password: String?, progress: Progress): Result<Unit> = runCatching {
        val format = ArchiveFormat.of(archive) ?: error("Format d'archive non pris en charge")
        // Vérification avant de créer le dossier : pas de dossier vide laissé si le mot de passe manque
        if (format == ArchiveFormat.ZIP && password.isNullOrEmpty() && isZipEncrypted(archive)) {
            throw PasswordNeededException()
        }
        check(destDir.mkdirs()) { "Création impossible : « ${destDir.name} »" }
        try {
            when (format) {
                ArchiveFormat.ZIP ->
                    if (!password.isNullOrEmpty()) extractZipEncrypted(archive, destDir, password, progress)
                    else extractZip(archive, destDir, progress)
                ArchiveFormat.SEVEN_Z -> extract7z(archive, destDir, password, progress)
                ArchiveFormat.RAR -> extractRar(archive, destDir, password, progress)
            }
        } catch (e: Throwable) {
            destDir.deleteRecursively()
            throw e
        }
    }

    private fun isZipEncrypted(file: File): Boolean =
        runCatching { Zip4jFile(file).use { it.isEncrypted } }.getOrDefault(false)

    /** Protection « zip slip » : aucun fichier ne doit sortir du dossier d'extraction. */
    private fun safeTarget(destDir: File, name: String): File {
        val base = destDir.canonicalFile
        val out = File(destDir, name)
        val canonical = out.canonicalFile
        require(canonical == base || canonical.path.startsWith(base.path + File.separator)) {
            "Archive refusée : chemin dangereux « $name »"
        }
        return out
    }

    private fun writeEntry(
        destDir: File,
        name: String,
        isDirectory: Boolean,
        time: Long,
        input: InputStream?,
        progress: Progress
    ) {
        progress.checkCancelled()
        val out = safeTarget(destDir, name.replace('\\', '/'))
        if (isDirectory) {
            check(out.isDirectory || out.mkdirs()) { "Création impossible : « ${out.name} »" }
            return
        }
        out.parentFile?.let { check(it.isDirectory || it.mkdirs()) { "Création impossible : « ${it.name} »" } }
        out.outputStream().use { output -> if (input != null) FileOps.copyStream(input, output, progress, out.name) }
        if (time > 0) out.setLastModified(time)
    }

    private fun extractZip(zip: File, destDir: File, progress: Progress) {
        try {
            extractZipWith(zip, destDir, progress, Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            // Nom de fichier illisible en UTF-8 : on recommence avec l'encodage Windows
            destDir.listFiles()?.forEach { it.deleteRecursively() }
            extractZipWith(zip, destDir, progress, FALLBACK_CHARSET)
        }
    }

    private fun extractZipWith(zip: File, destDir: File, progress: Progress, charset: Charset) {
        ZipInputStream(BufferedInputStream(zip.inputStream()), charset).use { zis ->
            while (true) {
                val entry = zis.nextEntry ?: break
                writeEntry(destDir, entry.name, entry.isDirectory, entry.time, zis, progress)
                zis.closeEntry()
            }
        }
    }

    private fun extractZipEncrypted(zip: File, destDir: File, password: String, progress: Progress) {
        try {
            Zip4jInputStream(BufferedInputStream(zip.inputStream()), password.toCharArray()).use { zis ->
                while (true) {
                    val header = zis.nextEntry ?: break
                    writeEntry(destDir, header.fileName, header.isDirectory, 0L, zis, progress)
                }
            }
        } catch (e: Zip4jException) {
            if (e.type == Zip4jException.Type.WRONG_PASSWORD) throw WrongPasswordException()
            throw e
        }
    }

    private fun open7z(file: File, password: String?): SevenZFile {
        val builder = SevenZFile.builder().setFile(file)
        if (!password.isNullOrEmpty()) builder.setPassword(password.toCharArray())
        return builder.get()
    }

    private fun extract7z(file: File, destDir: File, password: String?, progress: Progress) {
        try {
            open7z(file, password).use { z ->
                while (true) {
                    val entry = z.nextEntry ?: break
                    // Lecture séquentielle de l'entrée courante
                    val input = if (entry.isDirectory || !entry.hasStream()) null else object : InputStream() {
                        override fun read(): Int = z.read()
                        override fun read(b: ByteArray, off: Int, len: Int): Int = z.read(b, off, len)
                    }
                    writeEntry(
                        destDir, entry.name, entry.isDirectory,
                        if (entry.hasLastModifiedDate) entry.lastModifiedDate.time else 0L,
                        input, progress
                    )
                }
            }
        } catch (e: PasswordRequiredException) {
            throw if (password.isNullOrEmpty()) PasswordNeededException() else WrongPasswordException()
        } catch (e: java.io.IOException) {
            // Un mauvais mot de passe donne des données illisibles (erreur de somme de contrôle)
            if (!password.isNullOrEmpty()) throw WrongPasswordException()
            throw e
        }
    }

    private fun openRar(file: File, password: String?): Archive =
        try {
            Archive(file, null as UnrarCallback?, password)
        } catch (e: UnsupportedRarV5Exception) {
            throw IllegalStateException("Les archives RAR 5 ne sont pas prises en charge : utilisez le format RAR 4, ZIP ou 7Z")
        }

    private fun extractRar(file: File, destDir: File, password: String?, progress: Progress) {
        openRar(file, password).use { archive ->
            val encrypted = archive.isEncrypted || archive.fileHeaders.any { it.isEncrypted }
            if (encrypted && password.isNullOrEmpty()) throw PasswordNeededException()
            archive.fileHeaders.forEach { header ->
                val name = header.fileName
                if (header.isDirectory) {
                    writeEntry(destDir, name, true, 0L, null, progress)
                } else {
                    progress.checkCancelled()
                    val out = safeTarget(destDir, name.replace('\\', '/'))
                    out.parentFile?.let { check(it.isDirectory || it.mkdirs()) { "Création impossible : « ${it.name} »" } }
                    try {
                        CountingOutputStream(out.outputStream(), progress, out.name).use { archive.extractFile(header, it) }
                    } catch (e: RarException) {
                        throw if (encrypted) WrongPasswordException() else e
                    }
                    header.mTime?.let { out.setLastModified(it.time) }
                }
            }
        }
    }

    /** Compte les octets écrits, pour l'avancement des extractions RAR (écriture faite par la bibliothèque). */
    private class CountingOutputStream(
        private val target: OutputStream,
        private val progress: Progress,
        private val label: String
    ) : OutputStream() {
        override fun write(b: Int) {
            target.write(b)
            progress.add(1, label)
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            progress.checkCancelled()
            target.write(b, off, len)
            progress.add(len.toLong(), label)
        }

        override fun flush() = target.flush()
        override fun close() = target.close()
    }
}
