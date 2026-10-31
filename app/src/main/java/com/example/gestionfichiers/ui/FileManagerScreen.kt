package com.example.gestionfichiers.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionfichiers.FileManagerViewModel
import com.example.gestionfichiers.OpType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/** État du glisser-déposer (F5). Positions en coordonnées de la racine de l'écran. */
class DragState {
    var dragging by mutableStateOf<List<File>>(emptyList())
    var pointer by mutableStateOf(Offset.Zero)
    val folderBounds = mutableMapOf<File, Rect>()

    val isDragging: Boolean get() = dragging.isNotEmpty()

    fun target(): File? =
        folderBounds.entries.firstOrNull { (folder, rect) ->
            rect.contains(pointer) && folder !in dragging
        }?.key

    fun reset() {
        dragging = emptyList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(vm: FileManagerViewModel = viewModel()) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val drag = remember { DragState() }
    var showNewFolder by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(vm.message) {
        vm.message?.let {
            snackbar.showSnackbar(it)
            vm.message = null
        }
    }

    val selectionActive = vm.selected.isNotEmpty()

    BackHandler(enabled = selectionActive || vm.canGoUp) {
        if (selectionActive) vm.clearSelection() else vm.goUp()
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                    TopAppBar(
                        title = {
                            if (selectionActive) {
                                Text("${vm.selected.size} sélectionné(s)")
                            } else {
                                Column {
                                    Text(
                                        if (vm.canGoUp) vm.currentDir.name else "Fichiers",
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
                                vm.canGoUp -> IconButton(onClick = vm::goUp) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Dossier parent")
                                }
                            }
                        },
                        actions = {
                            if (selectionActive) {
                                IconButton(onClick = { vm.selectAll() }) {
                                    Icon(Icons.Filled.SelectAll, contentDescription = "Tout sélectionner")
                                }
                            } else {
                                // Création de dossier, dans la section du haut
                                FilledTonalButton(
                                    onClick = { showNewFolder = true },
                                    contentPadding = PaddingValues(horizontal = 14.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.CreateNewFolder,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Dossier")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = if (selectionActive)
                                MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                    )
                    Breadcrumb(vm)
                    if (vm.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            },
            bottomBar = {
                val op = vm.pending
                when {
                    op != null -> PasteBar(
                        label = (if (op.type == OpType.COPY) "Copier " else "Déplacer ") +
                            "${op.files.size} élément(s)",
                        onPaste = vm::pasteHere,
                        onCancel = vm::cancelPending
                    )
                    selectionActive -> ActionBar(
                        onCopy = vm::startCopy,
                        onMove = vm::startMove,
                        onDelete = { showConfirmDelete = true }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbar) }
        ) { padding ->
            if (vm.items.isEmpty()) {
                EmptyFolder(Modifier.fillMaxSize().padding(padding))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(vm.items, key = { it.absolutePath }) { file ->
                        FileRow(
                            file = file,
                            selected = file in vm.selected,
                            isDropTarget = drag.isDragging && drag.target() == file,
                            drag = drag,
                            onClick = {
                                when {
                                    selectionActive -> vm.toggle(file)
                                    file.isDirectory -> vm.enter(file)
                                    else -> openFile(context, file)
                                }
                            },
                            onLongClick = { vm.toggle(file) },
                            onDragStart = {
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
                }
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

    if (showNewFolder) {
        NewFolderDialog(
            onCreate = { name ->
                vm.createFolder(name)
                showNewFolder = false
            },
            onDismiss = { showNewFolder = false }
        )
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("Supprimer ${vm.selected.size} élément(s) ?") },
            text = { Text("Cette action est définitive. Les dossiers sont supprimés avec leur contenu.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteSelected()
                    showConfirmDelete = false
                }) { Text("Supprimer", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) { Text("Annuler") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Breadcrumb(vm: FileManagerViewModel) {
    val parts = vm.breadcrumb()
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(parts) { dir ->
            val isLast = dir == parts.last()
            FilterChip(
                selected = isLast,
                onClick = { vm.enter(dir) },
                label = { Text(if (dir == vm.root) "Stockage" else dir.name) },
                leadingIcon = if (dir == vm.root) {
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

@Composable
private fun EmptyFolder(modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(12.dp))
        Text("Ce dossier est vide", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.FRENCH)

private fun iconFor(file: File): ImageVector = when {
    file.isDirectory -> Icons.Filled.Folder
    else -> when (file.extension.lowercase()) {
        "jpg", "jpeg", "png", "gif", "webp", "heic", "bmp" -> Icons.Filled.Image
        "mp4", "mkv", "avi", "mov", "webm", "3gp" -> Icons.Filled.Movie
        "mp3", "wav", "ogg", "m4a", "flac", "aac" -> Icons.Filled.MusicNote
        "pdf" -> Icons.Filled.PictureAsPdf
        else -> Icons.Filled.Description
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileRow(
    file: File,
    selected: Boolean,
    isDropTarget: Boolean,
    drag: DragState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDragStart: () -> Unit,
    onDrop: () -> Unit
) {
    val context = LocalContext.current
    var handleOrigin by remember { mutableStateOf(Offset.Zero) }
    val colors = MaterialTheme.colorScheme

    DisposableEffect(file) {
        onDispose { drag.folderBounds.remove(file) }
    }

    val rowColor = when {
        isDropTarget -> colors.primaryContainer
        selected -> colors.secondaryContainer
        else -> colors.surface
    }

    val details = if (file.isDirectory) {
        "${file.list()?.size ?: 0} élément(s)"
    } else {
        Formatter.formatShortFileSize(context, file.length()) + "  ·  " + dateFormat.format(Date(file.lastModified()))
    }

    Surface(
        color = rowColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .onGloballyPositioned { coords ->
                if (file.isDirectory) drag.folderBounds[file] = coords.boundsInRoot()
            }
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pastille d'icône (coche quand l'élément est sélectionné)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            selected -> colors.primary
                            file.isDirectory -> colors.primaryContainer
                            else -> colors.surfaceContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (selected) Icons.Filled.Check else iconFor(file),
                    contentDescription = null,
                    tint = when {
                        selected -> colors.onPrimary
                        file.isDirectory -> colors.onPrimaryContainer
                        else -> colors.onSurfaceVariant
                    }
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (file.isDirectory) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    details,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            // Poignée de glisser-déposer
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .onGloballyPositioned { handleOrigin = it.positionInRoot() }
                    .pointerInput(file) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                drag.pointer = handleOrigin + offset
                                onDragStart()
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                drag.pointer += amount
                            },
                            onDragEnd = onDrop,
                            onDragCancel = { drag.reset() }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.DragIndicator,
                    contentDescription = "Glisser pour déplacer",
                    tint = colors.outline
                )
            }
        }
    }
}

@Composable
private fun ActionBar(onCopy: () -> Unit, onMove: () -> Unit, onDelete: () -> Unit) {
    BottomAppBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ActionButton("Copier", onCopy) { Icon(Icons.Filled.ContentCopy, null) }
            ActionButton("Déplacer", onMove) { Icon(Icons.Filled.DriveFileMove, null) }
            ActionButton("Supprimer", onDelete) {
                Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun RowScope.ActionButton(label: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.weight(1f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            Text(label, style = MaterialTheme.typography.labelMedium)
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

@Composable
private fun NewFolderDialog(onCreate: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.CreateNewFolder, contentDescription = null) },
        title = { Text("Nouveau dossier") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Nom du dossier") }
            )
        },
        confirmButton = { TextButton(onClick = { onCreate(name) }) { Text("Créer") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

/** F2 : ouvrir un fichier avec une application du téléphone. */
private fun openFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val mime = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(file.extension.lowercase()) ?: "*/*"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mime)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Aucune application pour ouvrir ce fichier", Toast.LENGTH_SHORT).show()
    }
}
