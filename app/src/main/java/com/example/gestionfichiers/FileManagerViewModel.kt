package com.example.gestionfichiers

import android.app.Application
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
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

/** Contenu affiché d'un dossier. Garder le dossier avec ses fichiers permet d'animer le passage de l'un à l'autre. */
data class Listing(val dir: File, val files: List<File>)

enum class Screen { BROWSER, TRASH, ANALYSIS }

class FileManagerViewModel(app: Application) : AndroidViewModel(app) {

    /** Stockage interne : racine par défaut, et emplacement de la corbeille. */
    val root: File = Environment.getExternalStorageDirectory()
    private val trash = Trash(root)
    private val prefs = Prefs(app)

    var screen by mutableStateOf(Screen.BROWSER)
        private set

    // ---- EF-19 : onglets (chaque onglet garde son dossier) ----
    val tabs = mutableStateListOf(root)
    var tabIndex by mutableIntStateOf(0)
        private set

    val currentDir: File get() = tabs[tabIndex.coerceIn(0, tabs.lastIndex)]

    // ---- EF-22 : double volet (écran large). Le second volet affiche un autre onglet. ----
    /** Choix de l'utilisateur (conservé). */
    var dualPane by mutableStateOf(prefs.dualPane)
        private set
    /** Vrai quand l'écran est assez large et le double volet choisi : fixé par l'interface. */
    var dualActive by mutableStateOf(false)
        private set
    var secondaryIndex by mutableIntStateOf(-1)
        private set
    var secondaryListing by mutableStateOf<Listing?>(null)
        private set
    val secondaryDir: File? get() = if (dualActive) tabs.getOrNull(secondaryIndex) else null

    var listing by mutableStateOf(Listing(root, emptyList()))
        private set
    val items: List<File> get() = listing.files

    var pending by mutableStateOf<PendingOp?>(null)
        private set
    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)

    /** Affichage en grille ou en liste (conservé). */
    var gridView by mutableStateOf(prefs.gridView)
        private set

    // ---- EF-12 : tri (conservé) ----
    var sortBy by mutableStateOf(prefs.sortBy)
        private set
    var sortDescending by mutableStateOf(prefs.sortDescending)
        private set

    /** F3 : éléments sélectionnés. */
    val selected = mutableStateListOf<File>()

    // ---- EF-14 : stockages, et espace du stockage courant ----
    var volumes by mutableStateOf(listOf(Volume(root, "Stockage interne", removable = false)))
        private set
    var space by mutableStateOf<SpaceInfo?>(null)
        private set

    // ---- EF-15 / EF-16 : accès rapide et favoris ----
    var quickDirs by mutableStateOf<List<QuickDir>>(emptyList())
        private set
    val favorites = mutableStateListOf<String>().apply { addAll(prefs.favorites) }

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
    var trashItems by mutableStateOf<List<TrashItem>>(emptyList())
        private set
    val trashSelected = mutableStateListOf<TrashItem>()

    // EF-20 : analyse du stockage
    var analysis by mutableStateOf<StorageAnalysis?>(null)
        private set
    var analyzing by mutableStateOf(false)
        private set
    var analysisVolume by mutableStateOf<Volume?>(null)
        private set
    private var analysisJob: Job? = null

    // EF-23 : mises à jour
    @Suppress("DEPRECATION")
    val appVersion: String = runCatching {
        app.packageManager.getPackageInfo(app.packageName, 0).versionName
    }.getOrNull() ?: "?"
    /** Nouvelle version trouvée : la boîte de dialogue s'affiche. */
    var update by mutableStateOf<UpdateInfo?>(null)
        private set
    var checkingUpdate by mutableStateOf(false)
        private set

    /** Racine du stockage qui contient le dossier courant. */
    val volumeRoot: File get() = volumeOf(currentDir).root

    val canGoUp: Boolean get() = currentDir != volumeRoot

    init {
        refreshVolumes()
        checkForUpdate(manual = false)
        // F10 : vidage automatique des éléments de plus de 30 jours
        viewModelScope.launch {
            val purged = withContext(Dispatchers.IO) { runCatching { trash.purgeOlderThan(30) }.getOrDefault(0) }
            if (purged > 0) message = "$purged élément(s) de plus de 30 jours effacé(s) de la corbeille"
        }
    }

    // ---- F1 : navigation ----

    fun refresh() {
        var dir = currentDir
        if (!dir.isDirectory) {
            // Carte SD retirée, dossier supprimé ailleurs… on revient au stockage interne
            dir = root
            tabs[tabIndex] = root
            message = "Ce dossier n'est plus disponible"
        }
        listing = Listing(dir, FileOps.list(dir, sortBy, sortDescending))
        space = Storage.space(volumeOf(dir).root)
        refreshSecondary()
    }

    private fun refreshSecondary() {
        if (!dualActive) {
            secondaryListing = null
            return
        }
        ensureSecondary()
        var dir = tabs[secondaryIndex]
        if (!dir.isDirectory) {
            dir = root
            tabs[secondaryIndex] = root
        }
        secondaryListing = Listing(dir, FileOps.list(dir, sortBy, sortDescending))
    }

    /** Garantit un second onglet, différent de l'onglet actif, pour le second volet. */
    private fun ensureSecondary() {
        if (tabs.size < 2) tabs.add(currentDir)
        if (secondaryIndex !in tabs.indices || secondaryIndex == tabIndex) {
            secondaryIndex = tabs.indices.first { it != tabIndex }
        }
    }

    fun toggleDualPane() {
        dualPane = !dualPane
        prefs.dualPane = dualPane
    }

    /** Appelé par l'interface selon la largeur de l'écran. */
    fun updateDualActive(active: Boolean) {
        if (dualActive == active) return
        dualActive = active
        refreshSecondary()
    }

    /** Rend actif le volet qui affiche l'onglet [index] (appui dans l'autre volet). */
    fun activatePane(index: Int) {
        if (index != tabIndex) selectTab(index)
    }

    fun enter(dir: File) {
        if (!dir.isDirectory) return
        tabs[tabIndex] = dir
        selected.clear()
        refresh()
    }

    fun goUp() {
        if (canGoUp) currentDir.parentFile?.let { enter(it) }
    }

    fun volumeOf(dir: File): Volume {
        val path = dir.absolutePath
        return volumes
            .filter { path == it.root.absolutePath || path.startsWith(it.root.absolutePath + File.separator) }
            .maxByOrNull { it.root.absolutePath.length }
            ?: volumes.first()
    }

    /** Segments du fil d'Ariane, de la racine du stockage au dossier courant. */
    fun breadcrumb(): List<File> {
        val top = volumeRoot
        val parts = mutableListOf<File>()
        var f: File? = currentDir
        while (f != null) {
            parts.add(0, f)
            if (f == top) break
            f = f.parentFile
        }
        return parts
    }

    /** Nom affiché d'un dossier : le nom du stockage pour une racine. */
    fun titleOf(dir: File): String {
        val v = volumes.firstOrNull { it.root == dir }
        return v?.label ?: dir.name
    }

    /** Chemin lisible d'un dossier, relatif à son stockage (résultats de recherche, analyse). */
    fun displayPath(dir: File?): String {
        if (dir == null) return ""
        val v = volumeOf(dir)
        val rel = dir.absolutePath.removePrefix(v.root.absolutePath).trimStart('/')
        return if (rel.isEmpty()) v.label else "${v.label}/$rel"
    }

    // ---- EF-12 : tri et affichage ----

    fun setSort(by: SortBy, descending: Boolean) {
        sortBy = by
        sortDescending = descending
        prefs.sortBy = by
        prefs.sortDescending = descending
        refresh()
        searchQuery?.let { if (it.isNotBlank()) runSearch(it) }
    }

    /** EF-24 : plein écran, conservé entre deux lancements. */
    var fullScreen by mutableStateOf(prefs.fullScreen)
        private set

    fun toggleFullScreen() {
        fullScreen = !fullScreen
        prefs.fullScreen = fullScreen
        message = if (fullScreen) "Plein écran : glissez depuis un bord pour afficher les barres" else null
    }

    fun toggleView() {
        gridView = !gridView
        prefs.gridView = gridView
    }

    // ---- EF-14 / EF-15 : stockages et accès rapide ----

    fun refreshVolumes() {
        volumes = runCatching { Storage.volumes(getApplication()) }
            .getOrDefault(listOf(Volume(root, "Stockage interne", removable = false)))
        quickDirs = Storage.quickDirs()
    }

    fun spaceOf(volume: Volume): SpaceInfo? = Storage.space(volume.root)

    /** Ouvre un dossier depuis le menu latéral : on ferme la recherche et les autres écrans. */
    fun openFromDrawer(dir: File) {
        if (!dir.isDirectory) {
            message = "« ${dir.name} » est introuvable"
            return
        }
        closeSearch()
        if (screen == Screen.TRASH) closeTrash()
        if (screen == Screen.ANALYSIS) closeAnalysis()
        enter(dir)
    }

    // ---- EF-16 : favoris ----

    fun isFavorite(dir: File) = dir.absolutePath in favorites

    fun addFavorite(dir: File) {
        if (!dir.isDirectory || isFavorite(dir)) return
        favorites.add(dir.absolutePath)
        prefs.favorites = favorites.toList()
        message = "« ${titleOf(dir)} » ajouté aux favoris"
    }

    fun removeFavorite(path: String) {
        if (favorites.remove(path)) {
            prefs.favorites = favorites.toList()
            message = "« ${File(path).name} » retiré des favoris"
        }
    }

    fun toggleFavorite(dir: File) {
        if (isFavorite(dir)) removeFavorite(dir.absolutePath) else addFavorite(dir)
    }

    // ---- EF-19 : onglets ----

    fun newTab(dir: File = currentDir) {
        tabs.add(dir)
        selectTab(tabs.lastIndex)
    }

    fun selectTab(index: Int) {
        if (index !in tabs.indices) return
        // En double volet, choisir l'onglet du second volet échange les deux volets
        if (dualActive && index == secondaryIndex) secondaryIndex = tabIndex
        tabIndex = index
        selected.clear()
        closeSearch()
        refresh()
    }

    fun closeTab(index: Int) {
        if (tabs.size <= 1 || index !in tabs.indices) return
        tabs.removeAt(index)
        if (tabIndex >= tabs.size || tabIndex > index) tabIndex = (tabIndex - 1).coerceAtLeast(0)
        if (secondaryIndex > index) secondaryIndex-- else if (secondaryIndex == index) secondaryIndex = -1
        selected.clear()
        refresh()
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
        message = "Ouvrez le dossier de destination (ou un autre onglet) puis touchez « Coller ici »"
    }

    fun cancelPending() {
        pending = null
    }

    fun pasteHere() {
        val op = pending ?: return
        pending = null
        val target = currentDir
        when (op.type) {
            OpType.COPY -> runOp("Copie terminée", sizeOf(op.files)) { p -> FileOps.copy(op.files, target, p) }
            OpType.MOVE -> runOp("Déplacement terminé", sizeOf(op.files)) { p -> FileOps.move(op.files, target, p) }
        }
    }

    /** EF-22 : copier ou déplacer la sélection directement vers le dossier de l'autre volet. */
    fun copyToOtherPane() = toOtherPane(OpType.COPY)
    fun moveToOtherPane() = toOtherPane(OpType.MOVE)

    private fun toOtherPane(type: OpType) {
        val target = secondaryDir ?: return
        val files = selected.toList()
        if (files.isEmpty()) return
        selected.clear()
        when (type) {
            OpType.COPY -> runOp("Copié dans « ${titleOf(target)} »", sizeOf(files)) { p -> FileOps.copy(files, target, p) }
            OpType.MOVE -> runOp("Déplacé dans « ${titleOf(target)} »", sizeOf(files)) { p -> FileOps.move(files, target, p) }
        }
    }

    /** F5 : glisser-déposer sur un dossier. */
    fun dropOnFolder(files: List<File>, folder: File) {
        selected.clear()
        runOp("Déplacé dans « ${folder.name} »", sizeOf(files)) { p -> FileOps.move(files, folder, p) }
    }

    // ---- F6 / F10 : supprimer = mettre à la corbeille ----

    fun deleteSelected() {
        val toDelete = selected.toList()
        selected.clear()
        runOp("${toDelete.size} élément(s) mis à la corbeille", sizeOf(toDelete)) { p -> trash.moveToTrash(toDelete, p) }
    }

    // ---- F7 : créer un dossier ----

    fun createFolder(name: String) {
        val parent = currentDir
        runOp("Dossier créé", null) { FileOps.createFolder(parent, name).map { } }
    }

    // ---- F8 : renommer ----

    fun renameSelected(name: String) {
        val file = selected.singleOrNull() ?: return
        selected.clear()
        runOp("Renommé en « ${name.trim()} »", null) { FileOps.rename(file, name).map { } }
    }

    // ---- EF-17 / EF-18 / EF-21 : archives ----

    /** Archive en attente de mot de passe (la boîte de dialogue s'affiche), et si le précédent était faux. */
    var passwordRequest by mutableStateOf<File?>(null)
        private set
    var passwordWasWrong by mutableStateOf(false)
        private set

    /** Nom proposé pour l'archive : le nom de l'élément s'il est seul, sinon « Archive ». */
    fun suggestedArchiveName(): String {
        val single = selected.singleOrNull() ?: return "Archive"
        return if (single.isDirectory) single.name else single.nameWithoutExtension.ifEmpty { single.name }
    }

    fun compressSelected(rawName: String, format: ArchiveFormat, password: String?) {
        val sources = selected.toList()
        if (sources.isEmpty()) return
        val name = rawName.trim().let { n ->
            if (n.endsWith(".${format.extension}", ignoreCase = true)) n.dropLast(format.extension.length + 1) else n
        }
        if (name.isEmpty() || '/' in name) {
            message = if (name.isEmpty()) "Le nom ne peut pas être vide" else "Le nom ne peut pas contenir « / »"
            return
        }
        val dest = FileOps.freeName(currentDir, "$name.${format.extension}")
        val pwd = password?.takeIf { it.isNotEmpty() && format == ArchiveFormat.ZIP }
        selected.clear()
        closeSearch()
        val done = if (pwd != null) "Archive protégée « ${dest.name} » créée" else "Archive « ${dest.name} » créée"
        runOp(done, sizeOf(sources)) { p -> Archives.compress(sources, dest, format, pwd, p) }
    }

    val canExtract: Boolean get() = selected.singleOrNull()?.let { Archives.isArchive(it) } == true

    fun extractSelected() {
        val archive = selected.singleOrNull()?.takeIf { Archives.isArchive(it) } ?: return
        selected.clear()
        extract(archive, null)
    }

    fun submitPassword(password: String) {
        val archive = passwordRequest ?: return
        passwordRequest = null
        extract(archive, password)
    }

    fun cancelPassword() {
        passwordRequest = null
        passwordWasWrong = false
    }

    private fun extract(archive: File, password: String?) {
        val dest = FileOps.freeName(archive.parentFile ?: currentDir, archive.nameWithoutExtension.ifEmpty { "Archive" })
        runOp(
            "Extrait dans « ${dest.name} »",
            { Archives.uncompressedSize(archive, password) },
            onError = { e ->
                when (e) {
                    is PasswordNeededException, is WrongPasswordException -> {
                        passwordWasWrong = e is WrongPasswordException
                        passwordRequest = archive
                        true
                    }
                    else -> false
                }
            }
        ) { p -> Archives.extract(archive, dest, password, p) }
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
        val order = FileOps.comparator(sortBy, sortDescending)
        searchJob = viewModelScope.launch {
            delay(300) // attendre que l'utilisateur finisse de taper
            searching = true
            val job = coroutineContext[Job]
            val results = withContext(Dispatchers.IO) {
                FileOps.search(base, query, limit = 500, isCancelled = { job?.isActive == false })
                    .sortedWith(order)
            }
            searchResults = results
            searching = false
        }
    }

    // ---- F10 : écran corbeille ----

    fun openTrash() {
        selected.clear()
        closeSearch()
        screen = Screen.TRASH
        refreshTrash()
    }

    fun closeTrash() {
        screen = Screen.BROWSER
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
        runOp("${list.size} élément(s) restauré(s)", sizeOf(list.map { it.file })) { p -> trash.restore(list, p) }
    }

    fun deleteForeverSelected() {
        val list = trashSelected.toList()
        trashSelected.clear()
        runOp("${list.size} élément(s) supprimé(s) définitivement", null) { trash.deleteForever(list) }
    }

    fun emptyTrash() {
        trashSelected.clear()
        runOp("Corbeille vidée", null) { trash.empty() }
    }

    // ---- EF-20 : analyse du stockage ----

    fun openAnalysis() {
        selected.clear()
        closeSearch()
        screen = Screen.ANALYSIS
        analyze(volumeOf(currentDir))
    }

    fun analyze(volume: Volume) {
        analysisJob?.cancel()
        analysisVolume = volume
        analysis = null
        analyzing = true
        analysisJob = viewModelScope.launch {
            val job = coroutineContext[Job]
            val result = withContext(Dispatchers.IO) {
                Storage.analyze(volume.root, isCancelled = { job?.isActive == false })
            }
            analysis = result
            analyzing = false
        }
    }

    fun closeAnalysis() {
        analysisJob?.cancel()
        analyzing = false
        screen = Screen.BROWSER
        refresh()
    }

    /** Depuis l'analyse : ouvre un dossier, ou le dossier d'un fichier avec ce fichier sélectionné. */
    fun revealFromAnalysis(file: File) {
        closeAnalysis()
        if (file.isDirectory) {
            enter(file)
        } else {
            file.parentFile?.let { enter(it) }
            if (file.exists()) selected.add(file)
        }
    }

    // ---- EF-23 : mises à jour ----

    /**
     * Automatique : au plus une fois par jour, en silence si rien de neuf ou sans connexion,
     * et sans reproposer une version ignorée. Manuelle : toujours, avec un message.
     */
    fun checkForUpdate(manual: Boolean) {
        val now = System.currentTimeMillis()
        if (!manual && now - prefs.lastUpdateCheck < 24L * 60 * 60 * 1000) return
        if (checkingUpdate) return
        checkingUpdate = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { runCatching { Updates.fetchLatest() } }
            checkingUpdate = false
            val info = result.getOrNull()
            if (result.isSuccess) prefs.lastUpdateCheck = now
            when {
                result.isFailure -> if (manual) message = "Vérification impossible : vérifiez votre connexion internet"
                info != null && Updates.isNewer(info.version, appVersion) &&
                    (manual || info.version != prefs.skippedVersion) -> update = info
                manual -> message = "Vous avez la dernière version ($appVersion)"
            }
        }
    }

    /** [skip] : ne plus proposer cette version automatiquement. */
    fun dismissUpdate(skip: Boolean) {
        if (skip) prefs.skippedVersion = update?.version
        update = null
    }

    // ---- F11 : exécution avec avancement ----

    fun cancelOperation() {
        cancelRequested = true
    }

    private fun sizeOf(files: List<File>): () -> Long = { FileOps.sizeOf(files) }

    /**
     * Exécute [block] en arrière-plan. Si [total] est fourni (calculé en arrière-plan),
     * la barre d'avancement en pourcentage s'affiche ; sinon une barre indéterminée.
     */
    private fun runOp(
        success: String,
        total: (() -> Long)?,
        onError: (Throwable) -> Boolean = { false },
        block: (Progress) -> Result<Unit>
    ) {
        viewModelScope.launch {
            busy = true
            cancelRequested = false
            progressLabel = ""
            progress = if (total == null) null else 0f
            val result = withContext(Dispatchers.IO) {
                val totalBytes = runCatching { total?.invoke() ?: 0L }.getOrDefault(0L)
                var lastPercent = -1
                val p = Progress(
                    total = totalBytes,
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
            val error = result.exceptionOrNull()
            message = when {
                error == null -> {
                    passwordWasWrong = false
                    success
                }
                onError(error) -> if (error is WrongPasswordException) error.message else null
                error is CancelledException -> "Opération annulée"
                else -> error.message ?: "Erreur inattendue"
            }
            refresh()
            if (screen == Screen.TRASH) refreshTrash()
            searchQuery?.let { if (it.isNotBlank()) runSearch(it) }
        }
    }
}
