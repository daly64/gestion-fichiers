package com.example.gestionfichiers.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.gestionfichiers.ArchiveFormat
import com.example.gestionfichiers.UpdateInfo
import com.example.gestionfichiers.R
import com.example.gestionfichiers.MainActivity
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.example.gestionfichiers.FileManagerViewModel
import com.example.gestionfichiers.Listing
import com.example.gestionfichiers.OpType
import com.example.gestionfichiers.QuickDir
import com.example.gestionfichiers.Screen
import com.example.gestionfichiers.SortBy
import com.example.gestionfichiers.Volume
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.grid.items as gridItems

@Composable
fun FileManagerScreen(vm: FileManagerViewModel = viewModel()) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    // Miniatures : images, et première seconde des vidéos
    val thumbLoader = remember {
        ImageLoader.Builder(context.applicationContext)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
    }

    LaunchedEffect(Unit) { vm.refresh() }
    // EF-24 : plein écran
    LaunchedEffect(vm.fullScreen) { (context as? MainActivity)?.applyFullScreen(vm.fullScreen) }
    LaunchedEffect(vm.message) {
        vm.message?.let {
            snackbar.showSnackbar(it)
            vm.message = null
        }
    }

    CompositionLocalProvider(LocalThumbLoader provides thumbLoader) {
        when (vm.screen) {
            Screen.TRASH -> TrashScreen(vm, snackbar)
            Screen.ANALYSIS -> AnalysisScreen(vm, snackbar)
            Screen.BROWSER -> BrowserScreen(vm, snackbar)
        }
    }

    vm.update?.let { info ->
        UpdateDialog(
            current = vm.appVersion,
            info = info,
            onDownload = {
                openUrl(context, info.apkUrl)
                vm.dismissUpdate(skip = false)
            },
            onLater = { vm.dismissUpdate(skip = false) },
            onSkip = { vm.dismissUpdate(skip = true) }
        )
    }
}

// =====================================================================
// Écran principal : menu latéral, onglets, navigation, sélection, recherche
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowserScreen(vm: FileManagerViewModel, snackbar: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val drag = remember { DragState() }
    var showNewFolder by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }
    var showCompress by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val selectionActive = vm.selected.isNotEmpty()
    val searchMode = vm.searchQuery != null
    // EF-22 : écran large (tablette ou téléphone en paysage)
    val wide = LocalConfiguration.current.screenWidthDp >= 600
    LaunchedEffect(wide, vm.dualPane) { vm.updateDualActive(wide && vm.dualPane) }

    BackHandler(enabled = selectionActive || searchMode || vm.canGoUp) {
        when {
            selectionActive -> vm.clearSelection()
            searchMode -> vm.closeSearch()
            else -> vm.goUp()
        }
    }
    // Déclaré après : le menu latéral ouvert se ferme en priorité
    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }

    LaunchedEffect(drawerState.targetValue) {
        if (drawerState.targetValue == DrawerValue.Open) vm.refreshVolumes()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen || (!drag.isDragging && !selectionActive),
        drawerContent = {
            DrawerContent(vm, onDone = { scope.launch { drawerState.close() } })
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.surface,
                topBar = {
                    Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                        TopAppBar(
                            title = {
                                when {
                                    selectionActive -> Text("${vm.selected.size} sélectionné(s)")
                                    searchMode -> SearchField(
                                        query = vm.searchQuery.orEmpty(),
                                        onQueryChange = vm::runSearch,
                                        placeholder = "Rechercher dans « ${vm.titleOf(vm.currentDir)} »"
                                    )
                                    else -> Column {
                                        Text(
                                            vm.titleOf(vm.currentDir),
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "${vm.items.size} élément(s)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            navigationIcon = {
                                when {
                                    selectionActive -> IconButton(onClick = vm::clearSelection) {
                                        Icon(Icons.Filled.Close, contentDescription = "Annuler la sélection")
                                    }
                                    searchMode -> IconButton(onClick = vm::closeSearch) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Fermer la recherche")
                                    }
                                    else -> IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Filled.Menu, contentDescription = "Menu : stockages, accès rapide, favoris")
                                    }
                                }
                            },
                            actions = {
                                when {
                                    selectionActive -> {
                                        val single = vm.selected.singleOrNull()
                                        if (single != null) {
                                            IconButton(onClick = { showRename = true }) {
                                                Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = "Renommer")
                                            }
                                        }
                                        if (single != null && single.isDirectory) {
                                            val fav = vm.isFavorite(single)
                                            IconButton(onClick = { vm.toggleFavorite(single) }) {
                                                Icon(
                                                    if (fav) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                                    contentDescription = if (fav) "Retirer des favoris" else "Ajouter aux favoris"
                                                )
                                            }
                                        }
                                        IconButton(onClick = { vm.selectAll() }) {
                                            Icon(Icons.Filled.SelectAll, contentDescription = "Tout sélectionner")
                                        }
                                    }
                                    searchMode -> Unit
                                    else -> {
                                        IconButton(onClick = vm::openSearch) {
                                            Icon(Icons.Filled.Search, contentDescription = "Rechercher")
                                        }
                                        Button(
                                            onClick = { showNewFolder = true },
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp)
                                        ) {
                                            Icon(Icons.Filled.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Dossier")
                                        }
                                        Box {
                                            IconButton(onClick = { showMenu = true }) {
                                                Icon(Icons.Filled.MoreVert, contentDescription = "Plus d'options")
                                            }
                                            MainMenu(
                                                vm = vm,
                                                expanded = showMenu,
                                                wide = wide,
                                                onDismiss = { showMenu = false },
                                                onSort = { showSort = true }
                                            )
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = if (selectionActive)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                        )
                        if (vm.tabs.size > 1) TabsRow(vm)
                        if (!searchMode) {
                            Breadcrumb(vm)
                            vm.space?.let { space ->
                                SpaceGauge(
                                    label = vm.volumeOf(vm.currentDir).label,
                                    space = space,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (vm.searching || (vm.busy && vm.progress == null)) {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                        }
                    }
                },
                bottomBar = {
                    val op = vm.pending
                    val p = vm.progress
                    when {
                        p != null -> ProgressBar(p, vm.progressLabel, onCancel = vm::cancelOperation)
                        op != null -> PasteBar(
                            label = (if (op.type == OpType.COPY) "Copier " else "Déplacer ") +
                                "${op.files.size} élément(s)",
                            onPaste = vm::pasteHere,
                            onCancel = vm::cancelPending
                        )
                        selectionActive -> ActionBar(
                            canExtract = vm.canExtract,
                            // Double volet : copie et déplacement directs vers le volet d'en face
                            otherPaneArrow = if (vm.secondaryDir == null) null
                                else if (vm.secondaryIndex > vm.tabIndex) "→" else "←",
                            onCopy = { if (vm.secondaryDir != null) vm.copyToOtherPane() else vm.startCopy() },
                            onMove = { if (vm.secondaryDir != null) vm.moveToOtherPane() else vm.startMove() },
                            onShare = {
                                val files = vm.selected.toList()
                                if (files.any { it.isDirectory }) {
                                    vm.message = "Les dossiers ne peuvent pas être partagés directement : compressez-les d'abord en ZIP"
                                } else {
                                    shareFiles(context, files)
                                }
                            },
                            onZip = { if (vm.canExtract) vm.extractSelected() else showCompress = true },
                            onDelete = { showConfirmDelete = true }
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbar) }
            ) { padding ->
                // Convention Android : un appui ouvre (ou coche en mode sélection),
                // un appui long sélectionne ; appui long puis glisser déplace vers un dossier.
                // [pane] : onglet affiché par le volet ; un geste dans l'autre volet le rend d'abord actif.
                val gestures: @Composable (File, Int) -> Modifier = { file, pane ->
                    Modifier.fileGestures(
                        file = file,
                        drag = drag,
                        onClick = {
                            if (pane != vm.tabIndex) vm.activatePane(pane)
                            when {
                                vm.selected.isNotEmpty() -> vm.toggle(file)
                                file.isDirectory -> {
                                    vm.closeSearch()
                                    vm.enter(file)
                                }
                                else -> openFile(context, file)
                            }
                        },
                        onLongClick = {
                            if (pane != vm.tabIndex) vm.activatePane(pane)
                            if (file !in vm.selected) vm.toggle(file)
                        },
                        onDragStart = {
                            if (pane != vm.tabIndex) vm.activatePane(pane)
                            drag.dragging =
                                if (file in vm.selected) vm.selected.toList() else listOf(file)
                        },
                        onDrop = {
                            val target = drag.target()
                            val files = drag.dragging
                            drag.reset()
                            if (target != null) vm.dropOnFolder(files, target)
                        }
                    )
                }
                val contentModifier = Modifier.fillMaxSize().padding(padding)
                val secondary = vm.secondaryListing

                when {
                    searchMode -> SearchResults(vm, drag, { f -> gestures(f, vm.tabIndex) }, contentModifier)
                    vm.dualActive && secondary != null -> {
                        // EF-22 : deux volets côte à côte, à position fixe (l'onglet le plus à gauche à gauche)
                        val leftIsActive = vm.tabIndex < vm.secondaryIndex
                        Row(contentModifier) {
                            val panes = if (leftIsActive)
                                listOf(Triple(vm.tabIndex, vm.listing, true), Triple(vm.secondaryIndex, secondary, false))
                            else
                                listOf(Triple(vm.secondaryIndex, secondary, false), Triple(vm.tabIndex, vm.listing, true))
                            panes.forEachIndexed { i, (index, listing, active) ->
                                if (i == 1) VerticalDivider()
                                Pane(
                                    vm = vm,
                                    dir = listing.dir,
                                    active = active,
                                    drag = drag,
                                    onActivate = { vm.activatePane(index) },
                                    modifier = Modifier.weight(1f).fillMaxSize()
                                ) {
                                    AnimatedFolder(vm, listing, drag, { f -> gestures(f, index) }, Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                    else -> AnimatedFolder(vm, vm.listing, drag, { f -> gestures(f, vm.tabIndex) }, contentModifier)
                }
            }

            // Étiquette qui suit le doigt pendant le glisser-déposer
            if (drag.isDragging) {
                Card(
                    modifier = Modifier.offset {
                        IntOffset(drag.pointer.x.roundToInt() - 200, drag.pointer.y.roundToInt() - 90)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.DriveFileMove, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (drag.dragging.size == 1) drag.dragging.first().name
                            else "${drag.dragging.size} éléments",
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    if (showNewFolder) {
        NameDialog(
            title = "Nouveau dossier",
            icon = Icons.Filled.CreateNewFolder,
            initial = "",
            confirmLabel = "Créer",
            onConfirm = { name ->
                vm.createFolder(name)
                showNewFolder = false
            },
            onDismiss = { showNewFolder = false }
        )
    }

    if (showCompress) {
        CompressDialog(
            initial = remember { vm.suggestedArchiveName() },
            onConfirm = { name, format, password ->
                vm.compressSelected(name, format, password)
                showCompress = false
            },
            onDismiss = { showCompress = false }
        )
    }

    vm.passwordRequest?.let { archive ->
        PasswordDialog(
            archiveName = archive.name,
            wrong = vm.passwordWasWrong,
            onConfirm = vm::submitPassword,
            onDismiss = vm::cancelPassword
        )
    }

    if (showSort) {
        SortDialog(
            current = vm.sortBy,
            descending = vm.sortDescending,
            onApply = { by, desc ->
                vm.setSort(by, desc)
                showSort = false
            },
            onDismiss = { showSort = false }
        )
    }

    if (showRename) {
        val file = vm.selected.singleOrNull()
        if (file == null) {
            showRename = false
        } else {
            RenameDialog(
                file = file,
                onRename = { name ->
                    vm.renameSelected(name)
                    showRename = false
                },
                onDismiss = { showRename = false }
            )
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("Mettre ${vm.selected.size} élément(s) à la corbeille ?") },
            text = { Text("Vous pourrez les restaurer depuis la corbeille pendant 30 jours.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteSelected()
                    showConfirmDelete = false
                }) { Text("Mettre à la corbeille") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) { Text("Annuler") }
            }
        )
    }
}

/** Contenu d'un dossier, avec une animation douce au passage d'un dossier à l'autre (cahier § 4.5). */
@Composable
private fun AnimatedFolder(
    vm: FileManagerViewModel,
    listing: Listing,
    drag: DragState,
    gestures: @Composable (File) -> Modifier,
    modifier: Modifier
) {
    AnimatedContent(
        targetState = listing,
        contentKey = { it.dir },
        transitionSpec = {
            val deeper = targetState.dir.absolutePath
                .startsWith(initialState.dir.absolutePath + File.separator)
            val sign = if (deeper) 1 else -1
            (slideInHorizontally(tween(260)) { w -> sign * w / 4 } + fadeIn(tween(260)))
                .togetherWith(slideOutHorizontally(tween(200)) { w -> -sign * w / 4 } + fadeOut(tween(200)))
        },
        modifier = modifier,
        label = "dossier"
    ) { shown ->
        FolderContent(vm, shown, drag, gestures)
    }
}

/**
 * EF-22 : un volet du double volet. En-tête avec le nom du dossier, mis en évidence
 * pour le volet actif ; tout le volet sert de cible au glisser-déposer.
 */
@Composable
private fun Pane(
    vm: FileManagerViewModel,
    dir: File,
    active: Boolean,
    drag: DragState,
    onActivate: () -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val isDropTarget = drag.isDragging && drag.target() == dir && drag.targetIsPane
    DisposableEffect(dir) {
        onDispose { drag.paneBounds.remove(dir) }
    }
    Column(
        modifier
            .onGloballyPositioned { drag.paneBounds[dir] = it.boundsInRoot() }
            .background(if (isDropTarget) colors.primaryContainer.copy(alpha = 0.4f) else Color.Transparent)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (active) colors.secondaryContainer else colors.surfaceContainer)
                .clickable(onClick = onActivate)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (active) colors.primary else colors.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    vm.titleOf(dir),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    vm.displayPath(dir),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) { content() }
    }
}

/** Contenu d'un dossier en grille ou en liste, avec déplacement animé des éléments. */
@Composable
private fun FolderContent(
    vm: FileManagerViewModel,
    listing: Listing,
    drag: DragState,
    gestures: @Composable (File) -> Modifier
) {
    val files = listing.files
    if (files.isEmpty()) {
        EmptyState(Icons.Filled.FolderOpen, "Ce dossier est vide", Modifier.fillMaxSize())
        return
    }
    if (vm.gridView) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            gridItems(files, key = { it.absolutePath }) { file ->
                FileTile(
                    file = file,
                    selected = file in vm.selected,
                    isDropTarget = drag.isDragging && drag.target() == file,
                    gestures = gestures(file),
                    modifier = Modifier.animateItem()
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(files, key = { it.absolutePath }) { file ->
                FileRow(
                    file = file,
                    selected = file in vm.selected,
                    isDropTarget = drag.isDragging && drag.target() == file,
                    gestures = gestures(file),
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

/** F9 : résultats de recherche, toujours en liste avec le dossier de chaque élément. */
@Composable
private fun SearchResults(
    vm: FileManagerViewModel,
    drag: DragState,
    gestures: @Composable (File) -> Modifier,
    modifier: Modifier
) {
    val shown = vm.searchResults
    when {
        shown.isEmpty() && vm.searchQuery.isNullOrBlank() ->
            EmptyState(Icons.Filled.Search, "Tapez un nom pour chercher dans ce dossier et ses sous-dossiers", modifier)
        shown.isEmpty() && !vm.searching ->
            EmptyState(Icons.Filled.SearchOff, "Aucun résultat", modifier)
        shown.isEmpty() -> Box(modifier)
        else -> LazyColumn(modifier = modifier, contentPadding = PaddingValues(vertical = 8.dp)) {
            item {
                Text(
                    if (shown.size >= 500) "500 premiers résultats" else "${shown.size} résultat(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            items(shown, key = { it.absolutePath }) { file ->
                FileRow(
                    file = file,
                    selected = file in vm.selected,
                    isDropTarget = drag.isDragging && drag.target() == file,
                    gestures = gestures(file),
                    location = vm.displayPath(file.parentFile),
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

/** Menu « ⋮ » de l'écran principal. */
@Composable
private fun MainMenu(vm: FileManagerViewModel, expanded: Boolean, wide: Boolean, onDismiss: () -> Unit, onSort: () -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Trier : ${vm.sortBy.label.lowercase()}${if (vm.sortDescending) " ↓" else " ↑"}") },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null) },
            onClick = { onDismiss(); onSort() }
        )
        DropdownMenuItem(
            text = { Text(if (vm.gridView) "Affichage en liste" else "Affichage en grille") },
            leadingIcon = {
                Icon(
                    if (vm.gridView) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                    contentDescription = null
                )
            },
            onClick = { onDismiss(); vm.toggleView() }
        )
        if (wide) {
            DropdownMenuItem(
                text = { Text(if (vm.dualPane) "Un seul volet" else "Double volet") },
                leadingIcon = { Icon(Icons.Filled.VerticalSplit, contentDescription = null) },
                onClick = { onDismiss(); vm.toggleDualPane() }
            )
        }
        DropdownMenuItem(
            text = { Text(if (vm.fullScreen) "Quitter le plein écran" else "Plein écran") },
            leadingIcon = {
                Icon(if (vm.fullScreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen, contentDescription = null)
            },
            onClick = { onDismiss(); vm.toggleFullScreen() }
        )
        DropdownMenuItem(
            text = { Text("Nouvel onglet") },
            leadingIcon = { Icon(Icons.Filled.Tab, contentDescription = null) },
            onClick = { onDismiss(); vm.newTab() }
        )
        val fav = vm.isFavorite(vm.currentDir)
        DropdownMenuItem(
            text = { Text(if (fav) "Retirer des favoris" else "Ajouter aux favoris") },
            leadingIcon = { Icon(if (fav) Icons.Filled.Star else Icons.Outlined.StarOutline, contentDescription = null) },
            onClick = { onDismiss(); vm.toggleFavorite(vm.currentDir) }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Analyse du stockage") },
            leadingIcon = { Icon(Icons.Filled.Analytics, contentDescription = null) },
            onClick = { onDismiss(); vm.openAnalysis() }
        )
        DropdownMenuItem(
            text = { Text("Corbeille") },
            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
            onClick = { onDismiss(); vm.openTrash() }
        )
    }
}

// =====================================================================
// EF-19 : onglets
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabsRow(vm: FileManagerViewModel) {
    PrimaryScrollableTabRow(
        selectedTabIndex = vm.tabIndex,
        edgePadding = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        vm.tabs.forEachIndexed { index, dir ->
            val selected = index == vm.tabIndex
            Tab(
                selected = selected,
                onClick = { vm.selectTab(index) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            vm.titleOf(dir),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 140.dp)
                        )
                        if (selected) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Fermer l'onglet",
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .clickable { vm.closeTab(index) }
                            )
                        }
                    }
                }
            )
        }
    }
}

// =====================================================================
// Menu latéral : stockages, accès rapide, favoris, outils
// =====================================================================

@Composable
private fun DrawerContent(vm: FileManagerViewModel, onDone: () -> Unit) {
    val context = LocalContext.current
    ModalDrawerSheet(Modifier.widthIn(max = 320.dp)) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
            // Logo : icône et mot-symbole « gestionfichiers » (cahier § 4.8)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, top = 20.dp, bottom = 8.dp)
            ) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Cobalt.C500),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    buildAnnotatedString {
                        append("gestion")
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("fichiers") }
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            DrawerSection("Stockages")
            vm.volumes.forEach { volume ->
                val space = remember(volume) { vm.spaceOf(volume) }
                NavigationDrawerItem(
                    icon = { Icon(volumeIcon(volume), contentDescription = null) },
                    label = {
                        Column(Modifier.padding(vertical = 4.dp)) {
                            Text(volume.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (space != null) {
                                Text(
                                    "${formatSize(context, space.free)} libres sur ${formatSize(context, space.total)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { space.usedFraction },
                                    modifier = Modifier.fillMaxWidth().height(3.dp),
                                    gapSize = 0.dp,
                                    drawStopIndicator = {}
                                )
                            }
                        }
                    },
                    selected = vm.currentDir == volume.root,
                    onClick = { vm.openFromDrawer(volume.root); onDone() }
                )
            }

            if (vm.quickDirs.isNotEmpty()) {
                DrawerSection("Accès rapide")
                vm.quickDirs.forEach { quick ->
                    NavigationDrawerItem(
                        icon = { Icon(quickIcon(quick), contentDescription = null) },
                        label = { Text(quick.label) },
                        selected = vm.currentDir == quick.dir,
                        onClick = { vm.openFromDrawer(quick.dir); onDone() }
                    )
                }
            }

            DrawerSection("Favoris")
            if (vm.favorites.isEmpty()) {
                Text(
                    "Ajoutez un dossier depuis le menu ⋮ ou en le sélectionnant puis en touchant l'étoile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            vm.favorites.toList().forEach { path ->
                val dir = File(path)
                val exists = remember(path) { dir.isDirectory }
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (exists) Cobalt.Warning else MaterialTheme.colorScheme.outline
                        )
                    },
                    label = {
                        Column {
                            Text(vm.titleOf(dir), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                if (exists) vm.displayPath(dir.parentFile) else "Introuvable",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (exists) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    badge = {
                        IconButton(onClick = { vm.removeFavorite(path) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Retirer des favoris", modifier = Modifier.size(18.dp))
                        }
                    },
                    selected = vm.currentDir == dir,
                    onClick = { vm.openFromDrawer(dir); onDone() }
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            DrawerSection("Outils")
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Analytics, contentDescription = null) },
                label = { Text("Analyse du stockage") },
                selected = false,
                onClick = { onDone(); vm.openAnalysis() }
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                label = { Text("Corbeille") },
                selected = false,
                onClick = { onDone(); vm.openTrash() }
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.SystemUpdate, contentDescription = null) },
                label = {
                    Column {
                        Text(if (vm.checkingUpdate) "Vérification…" else "Rechercher une mise à jour")
                        Text(
                            "Version ${vm.appVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                selected = false,
                onClick = { vm.checkForUpdate(manual = true) }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DrawerSection(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 6.dp)
    )
}

private fun volumeIcon(volume: Volume): ImageVector = when {
    !volume.removable -> Icons.Filled.PhoneAndroid
    volume.isUsb -> Icons.Filled.Usb
    else -> Icons.Filled.SdCard
}

private fun quickIcon(quick: QuickDir): ImageVector = when (quick) {
    QuickDir.DOWNLOADS -> Icons.Filled.Download
    QuickDir.DCIM -> Icons.Filled.PhotoCamera
    QuickDir.PICTURES -> Icons.Filled.Image
    QuickDir.DOCUMENTS -> Icons.Filled.Description
    QuickDir.MUSIC -> Icons.Filled.MusicNote
    QuickDir.MOVIES -> Icons.Filled.VideoLibrary
}

// =====================================================================
// EF-12 : tri
// =====================================================================

@Composable
private fun SortDialog(
    current: SortBy,
    descending: Boolean,
    onApply: (SortBy, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var by by remember { mutableStateOf(current) }
    var desc by remember { mutableStateOf(descending) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null) },
        title = { Text("Trier par") },
        text = {
            Column {
                SortBy.entries.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .selectable(selected = by == option, role = Role.RadioButton, onClick = { by = option })
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = by == option, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(option.label)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !desc, onClick = { desc = false }, label = { Text(ascendingLabel(by)) })
                    FilterChip(selected = desc, onClick = { desc = true }, label = { Text(descendingLabel(by)) })
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Les dossiers restent toujours avant les fichiers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = { onApply(by, desc) }) { Text("Appliquer") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

private fun ascendingLabel(by: SortBy) = when (by) {
    SortBy.NAME, SortBy.TYPE -> "A → Z"
    SortBy.DATE -> "Plus anciens"
    SortBy.SIZE -> "Plus petits"
}

private fun descendingLabel(by: SortBy) = when (by) {
    SortBy.NAME, SortBy.TYPE -> "Z → A"
    SortBy.DATE -> "Plus récents"
    SortBy.SIZE -> "Plus gros"
}

// =====================================================================
// Composants communs
// =====================================================================

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, placeholder: String) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        singleLine = true,
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Effacer")
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth().focusRequester(focus)
    )
}

@Composable
private fun Breadcrumb(vm: FileManagerViewModel) {
    val parts = vm.breadcrumb()
    val top = vm.volumeRoot
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(parts, key = { it.absolutePath }) { dir ->
            val isLast = dir == parts.last()
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.animateItem()) {
                FilterChip(
                    selected = isLast,
                    onClick = { vm.enter(dir) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    label = { Text(if (dir == top) vm.titleOf(dir) else dir.name) },
                    leadingIcon = if (dir == top) {
                        { Icon(Icons.Filled.SdStorage, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                if (!isLast) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
internal fun EmptyState(icon: ImageVector, text: String, modifier: Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(12.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

/** Barre de sélection flottante, sombre dans les deux thèmes (cahier § 4.8). */
@Composable
private fun ActionBar(
    canExtract: Boolean,
    otherPaneArrow: String?,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit,
    onZip: () -> Unit,
    onDelete: () -> Unit
) {
    val bg = if (isDark()) Cobalt.Night800 else Cobalt.Ink
    val fg = Cobalt.Snow
    Surface(
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            val arrow = otherPaneArrow?.let { " $it" }.orEmpty()
            ActionButton("Copier$arrow", onCopy, fg) { Icon(Icons.Filled.ContentCopy, null) }
            ActionButton("Déplacer$arrow", onMove, fg) { Icon(Icons.Filled.DriveFileMove, null) }
            ActionButton("Partager", onShare, fg) { Icon(Icons.Filled.Share, null) }
            if (canExtract) {
                ActionButton("Extraire", onZip, fg) { Icon(Icons.Filled.Unarchive, null) }
            } else {
                ActionButton("Archiver", onZip, fg) { Icon(Icons.Filled.FolderZip, null) }
            }
            ActionButton("Supprimer", onDelete, Cobalt.DangerLight) { Icon(Icons.Filled.Delete, null) }
        }
    }
}

@Composable
internal fun RowScope.ActionButton(
    label: String,
    onClick: () -> Unit,
    contentColor: Color = Color.Unspecified,
    icon: @Composable () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = if (contentColor != Color.Unspecified) ButtonDefaults.textButtonColors(contentColor = contentColor)
            else ButtonDefaults.textButtonColors(),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun PasteBar(label: String, onPaste: () -> Unit, onCancel: () -> Unit) {
    BottomAppBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Text(
            label,
            modifier = Modifier.weight(1f).padding(start = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        TextButton(onClick = onCancel) { Text("Annuler") }
        FilledTonalButton(onClick = onPaste, modifier = Modifier.padding(end = 8.dp)) {
            Icon(Icons.Filled.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Coller ici")
        }
    }
}

/** F11 : barre d'avancement avec pourcentage, fichier en cours et bouton Annuler. */
@Composable
internal fun ProgressBar(progress: Float, label: String, onCancel: () -> Unit) {
    BottomAppBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.weight(1f).padding(start = 16.dp, end = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label.ifEmpty { "Préparation…" },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${(progress * 100).roundToInt()} %",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
        TextButton(onClick = onCancel) { Text("Annuler") }
    }
}

@Composable
private fun NameDialog(
    title: String,
    icon: ImageVector,
    initial: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    label: String = "Nom",
    suffix: String? = null
) {
    var value by remember { mutableStateOf(TextFieldValue(initial, selection = TextRange(0, initial.length))) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, contentDescription = null) },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                label = { Text(label) },
                suffix = suffix?.let { s -> { Text(s) } },
                modifier = Modifier.focusRequester(focus)
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(value.text) }) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

/** EF-17 / EF-21 : nom, format (ZIP ou 7Z) et mot de passe facultatif (ZIP seulement). */
@Composable
private fun CompressDialog(
    initial: String,
    onConfirm: (String, ArchiveFormat, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(initial, selection = TextRange(0, initial.length))) }
    var format by remember { mutableStateOf(ArchiveFormat.ZIP) }
    var protect by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val passwordOk = !protect || password.length >= 4
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.FolderZip, contentDescription = null) },
        title = { Text("Créer une archive") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("Nom de l'archive") },
                    suffix = { Text(".${format.extension}") },
                    modifier = Modifier.focusRequester(focus)
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ArchiveFormat.ZIP, ArchiveFormat.SEVEN_Z).forEach { f ->
                        FilterChip(selected = format == f, onClick = { format = f }, label = { Text(f.label) })
                    }
                }
                Spacer(Modifier.height(4.dp))
                if (format == ArchiveFormat.ZIP) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .toggleable(value = protect, role = Role.Checkbox, onValueChange = { protect = it })
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = protect, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Protéger par un mot de passe")
                    }
                    if (protect) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            singleLine = true,
                            label = { Text("Mot de passe") },
                            supportingText = { Text("4 caractères minimum · chiffrement AES-256") },
                            isError = password.isNotEmpty() && !passwordOk,
                            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { visible = !visible }) {
                                    Icon(
                                        if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (visible) "Masquer le mot de passe" else "Afficher le mot de passe"
                                    )
                                }
                            }
                        )
                    }
                } else {
                    Text(
                        "Le 7Z compresse mieux ; le mot de passe n'est proposé qu'en ZIP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.text, format, if (protect && format == ArchiveFormat.ZIP) password else null) },
                enabled = passwordOk
            ) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

/** EF-21 : mot de passe d'une archive protégée, redemandé s'il est faux. */
@Composable
private fun PasswordDialog(archiveName: String, wrong: Boolean, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        title = { Text("Archive protégée") },
        text = {
            Column {
                Text("Entrez le mot de passe de « $archiveName ».")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    label = { Text("Mot de passe") },
                    isError = wrong,
                    supportingText = if (wrong) {
                        { Text("Mot de passe incorrect, réessayez") }
                    } else null,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (visible) "Masquer le mot de passe" else "Afficher le mot de passe"
                            )
                        }
                    },
                    modifier = Modifier.focusRequester(focus)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password) }, enabled = password.isNotEmpty()) { Text("Extraire") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

/** EF-23 : nouvelle version disponible. Le téléchargement se fait dans le navigateur. */
@Composable
private fun UpdateDialog(
    current: String,
    info: UpdateInfo,
    onDownload: () -> Unit,
    onLater: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onLater,
        icon = { Icon(Icons.Filled.SystemUpdate, contentDescription = null) },
        title = { Text("Mise à jour disponible") },
        text = {
            Column {
                Text("La version ${info.version} est disponible (vous avez la $current).")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Une fois le fichier téléchargé, ouvrez-le et touchez « Mettre à jour » : vos favoris et réglages sont conservés.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = onDownload) { Text("Télécharger") } },
        dismissButton = {
            Row {
                TextButton(onClick = onSkip) { Text("Ignorer") }
                TextButton(onClick = onLater) { Text("Plus tard") }
            }
        }
    )
}

/** F8 : renommer. Le nom est présélectionné sans l'extension, pour la garder facilement. */
@Composable
private fun RenameDialog(file: File, onRename: (String) -> Unit, onDismiss: () -> Unit) {
    val end = if (file.isFile && file.extension.isNotEmpty()) file.nameWithoutExtension.length else file.name.length
    var value by remember { mutableStateOf(TextFieldValue(file.name, selection = TextRange(0, end))) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = null) },
        title = { Text("Renommer") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                label = { Text("Nouveau nom") },
                modifier = Modifier.focusRequester(focus)
            )
        },
        confirmButton = { TextButton(onClick = { onRename(value.text) }) { Text("Renommer") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

// =====================================================================
// Ouvrir et partager (F2, EF-13)
// =====================================================================

private fun uriFor(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

private fun mimeOf(file: File): String =
    MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: "*/*"

private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Aucun navigateur pour ouvrir le lien", Toast.LENGTH_SHORT).show()
    }
}

/** F2 : ouvrir un fichier avec une application du téléphone. */
private fun openFile(context: Context, file: File) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uriFor(context, file), mimeOf(file))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Aucune application pour ouvrir ce fichier", Toast.LENGTH_SHORT).show()
    } catch (e: IllegalArgumentException) {
        Toast.makeText(context, "Ce fichier ne peut pas être ouvert depuis cet emplacement", Toast.LENGTH_SHORT).show()
    }
}

/** EF-13 : envoyer un ou plusieurs fichiers vers une autre application. */
private fun shareFiles(context: Context, files: List<File>) {
    try {
        val uris = files.map { uriFor(context, it) }
        val mimes = files.map(::mimeOf).distinct()
        // Type commun : « image/* » pour plusieurs images, « */* » pour un mélange
        val type = when {
            mimes.size == 1 -> mimes.first()
            mimes.map { it.substringBefore('/') }.distinct().size == 1 -> mimes.first().substringBefore('/') + "/*"
            else -> "*/*"
        }
        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply { putExtra(Intent.EXTRA_STREAM, uris.first()) }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply { putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris)) }
        }.apply {
            this.type = type
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Partager ${if (files.size == 1) files.first().name else "${files.size} fichiers"}"))
    } catch (e: Exception) {
        Toast.makeText(context, "Partage impossible : ${e.message ?: "erreur inattendue"}", Toast.LENGTH_SHORT).show()
    }
}
