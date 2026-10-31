package com.example.gestionfichiers

import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class OpType { COPY, MOVE }

/** Copie ou déplacement en attente : l'utilisateur choisit le dossier puis « Coller ici ». */
data class PendingOp(val type: OpType, val files: List<File>)

class FileManagerViewModel : ViewModel() {

    val root: File = Environment.getExternalStorageDirectory()
    private val trash = Trash(root)

    var currentDir by mutableStateOf(root)
        private set
    var items by mutableStateOf<List<File>>(emptyList())
        private set
    var pending by mutableStateOf<PendingOp?>(null)
        private set
    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)

    /** Affichage en grille (par défaut) ou en liste. */
    var gridView by mutableStateOf(true)

    /** F3 : éléments sélectionnés. */
    val selected = mutableStateListOf<File>()

    // F11 : avancement (null = aucune opération longue en cours)
    var progress by mutableStateOf<Float?>(null)
        private set
    var progressLabel by mutableStateOf("")
        private set
    @Volatile private var cancelRequested = false

    // F9 : recherche (null = pas en mode recherche)
    var searchQuery by mutableStateOf<String?>(null)
        private set
    var searchResults by mutableStateOf<List<File>>(emptyList())
        private set
    var searching by mutableStateOf(false)
        private set
    private var searchJob: Job? = null

    // F10 : corbeille
    var showTrash by mutableStateOf(false)
        private set
    var trashItems by mutableStateOf<List<TrashItem>>(emptyList())
        private set
    val trashSelected = mutableStateListOf<TrashItem>()

    val canGoUp: Boolean get() = currentDir != root

    init {
        // F10 : vidage automatique des éléments de plus de 30 jours
        viewModelScope.launch {
            val purged = withContext(Dispatchers.IO) { runCatching { trash.purgeOlderThan(30) }.getOrDefault(0) }
            if (purged > 0) message = "$purged élément(s) de plus de 30 jours effacé(s) de la corbeille"
        }
    }

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

    /** Chemin lisible d'un dossier, relatif au stockage (pour les résultats de recherche). */
    fun displayPath(dir: File?): String {
        if (dir == null) return ""
        val rel = dir.absolutePath.removePrefix(root.absolutePath).trimStart('/')
        return if (rel.isEmpty()) "Stockage" else "Stockage/$rel"
    }

    // ---- F3 : sélection ----

    fun toggle(file: File) {
        if (!selected.remove(file)) selected.add(file)
    }

    fun clearSelection() = selected.clear()

    fun selectAll() {
        selected.clear()
        selected.addAll(if (searchQuery != null) searchResults else items)
    }

    // ---- F4 / F5 : copier, déplacer ----

    fun startCopy() = startPending(OpType.COPY)
    fun startMove() = startPending(OpType.MOVE)

    private fun startPending(type: OpType) {
        if (selected.isEmpty()) return
        pending = PendingOp(type, selected.toList())
        selected.clear()
        closeSearch()
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
            OpType.COPY -> runOp("Copie terminée", op.files) { p -> FileOps.copy(op.files, target, p) }
            OpType.MOVE -> runOp("Déplacement terminé", op.files) { p -> FileOps.move(op.files, target, p) }
        }
    }

    /** F5 : glisser-déposer sur un dossier. */
    fun dropOnFolder(files: List<File>, folder: File) {
        selected.clear()
        runOp("Déplacé dans « ${folder.name} »", files) { p -> FileOps.move(files, folder, p) }
    }

    // ---- F6 / F10 : supprimer = mettre à la corbeille ----

    fun deleteSelected() {
        val toDelete = selected.toList()
        selected.clear()
        runOp("${toDelete.size} élément(s) mis à la corbeille", toDelete) { p -> trash.moveToTrash(toDelete, p) }
    }

    // ---- F7 : créer un dossier ----

    fun createFolder(name: String) {
        val parent = currentDir
        runOp("Dossier créé", emptyList()) { FileOps.createFolder(parent, name).map { } }
    }

    // ---- F8 : renommer ----

    fun renameSelected(name: String) {
        val file = selected.singleOrNull() ?: return
        selected.clear()
        runOp("Renommé en « ${name.trim()} »", emptyList()) { FileOps.rename(file, name).map { } }
    }

    // ---- F9 : rechercher ----

    fun openSearch() {
        selected.clear()
        searchQuery = ""
        searchResults = emptyList()
    }

    fun closeSearch() {
        searchJob?.cancel()
        searchQuery = null
        searchResults = emptyList()
        searching = false
    }

    fun runSearch(query: String) {
        searchQuery = query
        searchJob?.cancel()
        if (query.isBlank()) {
            searchResults = emptyList()
            searching = false
            return
        }
        val base = currentDir
        searchJob = viewModelScope.launch {
            delay(300) // attendre que l'utilisateur finisse de taper
            searching = true
            val job = coroutineContext[Job]
            val results = withContext(Dispatchers.IO) {
                FileOps.search(base, query, limit = 500, isCancelled = { job?.isActive == false })
            }
            searchResults = results
            searching = false
        }
    }

    // ---- F10 : écran corbeille ----

    fun openTrash() {
        selected.clear()
        closeSearch()
        showTrash = true
        refreshTrash()
    }

    fun closeTrash() {
        showTrash = false
        trashSelected.clear()
        refresh()
    }

    fun refreshTrash() {
        trashItems = runCatching { trash.list() }.getOrDefault(emptyList())
    }

    fun toggleTrash(item: TrashItem) {
        if (!trashSelected.remove(item)) trashSelected.add(item)
    }

    fun restoreSelected() {
        val list = trashSelected.toList()
        trashSelected.clear()
        runOp("${list.size} élément(s) restauré(s)", list.map { it.file }) { p -> trash.restore(list, p) }
    }

    fun deleteForeverSelected() {
        val list = trashSelected.toList()
        trashSelected.clear()
        runOp("${list.size} élément(s) supprimé(s) définitivement", emptyList()) { trash.deleteForever(list) }
    }

    fun emptyTrash() {
        trashSelected.clear()
        runOp("Corbeille vidée", emptyList()) { trash.empty() }
    }

    // ---- F11 : exécution avec avancement ----

    fun cancelOperation() {
        cancelRequested = true
    }

    private fun runOp(success: String, sources: List<File>, block: (Progress) -> Result<Unit>) {
        viewModelScope.launch {
            busy = true
            cancelRequested = false
            progressLabel = ""
            progress = if (sources.isEmpty()) null else 0f
            val result = withContext(Dispatchers.IO) {
                val total = FileOps.sizeOf(sources)
                var lastPercent = -1
                val p = Progress(
                    total = total,
                    onUpdate = { done, tot, current ->
                        val percent = if (tot > 0) (done * 100 / tot).toInt().coerceIn(0, 100) else 100
                        if (percent != lastPercent) {
                            lastPercent = percent
                            viewModelScope.launch(Dispatchers.Main) {
                                if (progress != null) {
                                    progress = percent / 100f
                                    progressLabel = current
                                }
                            }
                        }
                    },
                    isCancelled = { cancelRequested }
                )
                block(p)
            }
            busy = false
            progress = null
            message = result.fold(
                { success },
                { if (it is CancelledException) "Opération annulée" else it.message ?: "Erreur inattendue" }
            )
            refresh()
            if (showTrash) refreshTrash()
            if (searchQuery != null) searchQuery?.let { runSearch(it) }
        }
    }
}
