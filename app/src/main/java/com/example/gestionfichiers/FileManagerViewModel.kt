package com.example.gestionfichiers

import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class OpType { COPY, MOVE }

/** Copie ou déplacement en attente : l'utilisateur choisit le dossier puis « Coller ici ». */
data class PendingOp(val type: OpType, val files: List<File>)

class FileManagerViewModel : ViewModel() {

    val root: File = Environment.getExternalStorageDirectory()

    var currentDir by mutableStateOf(root)
        private set
    var items by mutableStateOf<List<File>>(emptyList())
        private set
    var pending by mutableStateOf<PendingOp?>(null)
        private set
    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)

    /** F3 : éléments sélectionnés. */
    val selected = mutableStateListOf<File>()

    val canGoUp: Boolean get() = currentDir != root

    // ---- F1 : navigation ----

    fun refresh() {
        items = FileOps.list(currentDir)
    }

    fun enter(dir: File) {
        if (!dir.isDirectory) return
        currentDir = dir
        selected.clear()
        refresh()
    }

    fun goUp() {
        if (canGoUp) currentDir.parentFile?.let { enter(it) }
    }

    /** Segments du fil d'Ariane, du stockage racine au dossier courant. */
    fun breadcrumb(): List<File> {
        val parts = mutableListOf<File>()
        var f: File? = currentDir
        while (f != null) {
            parts.add(0, f)
            if (f == root) break
            f = f.parentFile
        }
        return parts
    }

    // ---- F3 : sélection ----

    fun toggle(file: File) {
        if (!selected.remove(file)) selected.add(file)
    }

    fun clearSelection() = selected.clear()

    // ---- F4 / F5 : copier, déplacer ----

    fun startCopy() = startPending(OpType.COPY)
    fun startMove() = startPending(OpType.MOVE)

    private fun startPending(type: OpType) {
        if (selected.isEmpty()) return
        pending = PendingOp(type, selected.toList())
        selected.clear()
        message = "Ouvrez le dossier de destination puis touchez « Coller ici »"
    }

    fun cancelPending() {
        pending = null
    }

    fun pasteHere() {
        val op = pending ?: return
        pending = null
        val target = currentDir
        when (op.type) {
            OpType.COPY -> runOp("Copie terminée") { FileOps.copy(op.files, target) }
            OpType.MOVE -> runOp("Déplacement terminé") { FileOps.move(op.files, target) }
        }
    }

    /** F5 : glisser-déposer sur un dossier. */
    fun dropOnFolder(files: List<File>, folder: File) {
        selected.clear()
        runOp("Déplacé dans « ${folder.name} »") { FileOps.move(files, folder) }
    }

    // ---- F6 : supprimer ----

    fun deleteSelected() {
        val toDelete = selected.toList()
        selected.clear()
        runOp("Suppression terminée") { FileOps.delete(toDelete) }
    }

    // ---- F7 : créer un dossier ----

    fun createFolder(name: String) {
        val parent = currentDir
        runOp("Dossier créé") { FileOps.createFolder(parent, name).map { } }
    }

    private fun runOp(success: String, block: () -> Result<Unit>) {
        viewModelScope.launch {
            busy = true
            val result = withContext(Dispatchers.IO) { block() }
            busy = false
            message = result.fold({ success }, { it.message ?: "Erreur inattendue" })
            refresh()
        }
    }
}
