package com.example.gestionfichiers

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import java.io.File
import java.util.PriorityQueue

/** Un stockage du téléphone : interne, carte SD ou clé USB (EF-14). */
data class Volume(val root: File, val label: String, val removable: Boolean) {
    val isUsb: Boolean get() = label.contains("usb", ignoreCase = true)
}

/** Espace d'un stockage, en octets. */
data class SpaceInfo(val total: Long, val free: Long) {
    val used: Long get() = (total - free).coerceAtLeast(0)
    val usedFraction: Float get() = if (total > 0) (used.toDouble() / total).toFloat().coerceIn(0f, 1f) else 0f
}

/** Dossiers d'accès rapide (EF-15). */
enum class QuickDir(val label: String, val type: String) {
    DOWNLOADS("Téléchargements", Environment.DIRECTORY_DOWNLOADS),
    DCIM("Photos", Environment.DIRECTORY_DCIM),
    PICTURES("Images", Environment.DIRECTORY_PICTURES),
    DOCUMENTS("Documents", Environment.DIRECTORY_DOCUMENTS),
    MUSIC("Musique", Environment.DIRECTORY_MUSIC),
    MOVIES("Vidéos", Environment.DIRECTORY_MOVIES);

    @Suppress("DEPRECATION")
    val dir: File get() = Environment.getExternalStoragePublicDirectory(type)
}

data class SizedFile(val file: File, val size: Long)

/** Résultat de l'analyse du stockage (EF-20). */
data class StorageAnalysis(
    val root: File,
    val folders: List<SizedFile>,
    val files: List<SizedFile>,
    val trashSize: Long
)

object Storage {

    /** Stockages montés : l'interne en premier, puis les cartes SD et clés USB. */
    fun volumes(context: Context): List<Volume> {
        val sm = context.getSystemService(StorageManager::class.java)
        val primary = Environment.getExternalStorageDirectory()
        val result = mutableListOf(Volume(primary, "Stockage interne", removable = false))

        val roots: List<File> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            sm.storageVolumes.mapNotNull { v ->
                val mounted = v.state == Environment.MEDIA_MOUNTED || v.state == Environment.MEDIA_MOUNTED_READ_ONLY
                if (mounted) v.directory else null
            }
        } else {
            // Avant Android 11 : on déduit la racine des dossiers privés de l'app sur chaque stockage
            context.getExternalFilesDirs(null).mapNotNull { dir ->
                dir?.absolutePath?.substringBefore("/Android/data")?.let(::File)
            }
        }

        roots.filter { it.absolutePath != primary.absolutePath && it.isDirectory }
            .distinctBy { it.absolutePath }
            .forEach { root ->
                val sv = runCatching { sm.getStorageVolume(root) }.getOrNull()
                val label = sv?.getDescription(context)?.takeIf { it.isNotBlank() } ?: root.name
                result += Volume(root, label, removable = sv?.isRemovable ?: true)
            }
        return result
    }

    fun space(root: File): SpaceInfo? = runCatching {
        val stat = StatFs(root.absolutePath)
        SpaceInfo(total = stat.totalBytes, free = stat.availableBytes)
    }.getOrNull()

    fun quickDirs(): List<QuickDir> = QuickDir.entries.filter { it.dir.isDirectory }

    /**
     * EF-20 : taille des dossiers du premier niveau et plus gros fichiers du stockage.
     * La corbeille est comptée à part.
     */
    fun analyze(root: File, isCancelled: () -> Boolean = { false }): StorageAnalysis {
        val maxFiles = 50
        val biggest = PriorityQueue<SizedFile>(compareBy { it.size }) // tas min : on garde les 50 plus gros
        fun offer(f: File, size: Long) {
            if (biggest.size < maxFiles) biggest.add(SizedFile(f, size))
            else if (size > biggest.peek()!!.size) {
                biggest.poll()
                biggest.add(SizedFile(f, size))
            }
        }

        val folders = mutableListOf<SizedFile>()
        var trashSize = 0L
        root.listFiles()?.forEach { child ->
            if (isCancelled()) return@forEach
            if (child.isDirectory) {
                val isTrash = child.name == FileOps.TRASH_DIR_NAME
                var sum = 0L
                child.walkTopDown()
                    .onEnter { !isCancelled() }
                    .filter { it.isFile }
                    .forEach { f ->
                        val len = f.length()
                        sum += len
                        if (!isTrash) offer(f, len)
                    }
                if (isTrash) trashSize = sum else folders += SizedFile(child, sum)
            } else if (child.isFile) {
                offer(child, child.length())
            }
        }
        return StorageAnalysis(
            root = root,
            folders = folders.sortedByDescending { it.size }.take(20),
            files = biggest.sortedByDescending { it.size },
            trashSize = trashSize
        )
    }
}
