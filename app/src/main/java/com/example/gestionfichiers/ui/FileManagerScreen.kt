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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(if (selectionActive) "${vm.selected.size} sélectionné(s)" else "Fichiers")
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
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
            floatingActionButton = {
                if (!selectionActive) {
                    FloatingActionButton(onClick = { showNewFolder = true }) {
                        Icon(Icons.Filled.CreateNewFolder, contentDescription = "Nouveau dossier")
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbar) }
        ) { padding ->
            if (vm.items.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Dossier vide", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
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
                    IntOffset(drag.pointer.x.roundToInt() + 24, drag.pointer.y.roundToInt() - 48)
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    if (drag.dragging.size == 1) drag.dragging.first().name
                    else "${drag.dragging.size} éléments",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Medium
                )
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
            title = { Text("Supprimer ${vm.selected.size} élément(s) ?") },
            text = { Text("Cette action est définitive. Les dossiers sont supprimés avec leur contenu.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteSelected()
                    showConfirmDelete = false
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun Breadcrumb(vm: FileManagerViewModel) {
    val parts = vm.breadcrumb()
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(parts) { dir ->
            val label = if (dir == vm.root) "Stockage" else dir.name
            TextButton(onClick = { vm.enter(dir) }) {
                Text(label, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            if (dir != parts.last()) {
                Text("›", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
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

    // Les dossiers déclarent leur zone pour recevoir un dépôt
    DisposableEffect(file) {
        onDispose { drag.folderBounds.remove(file) }
    }

    val background = when {
        isDropTarget -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        selected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .onGloballyPositioned { coords ->
                if (file.isDirectory) drag.folderBounds[file] = coords.boundsInRoot()
            }
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (file.isDirectory) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(file.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (file.isFile) {
                    Text(
                        Formatter.formatShortFileSize(context, file.length()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActionBar(onCopy: () -> Unit, onMove: () -> Unit, onDelete: () -> Unit) {
    BottomAppBar {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ActionButton("Copier", onCopy) { Icon(Icons.Filled.ContentCopy, null) }
            ActionButton("Déplacer", onMove) { Icon(Icons.Filled.DriveFileMove, null) }
            ActionButton("Supprimer", onDelete) { Icon(Icons.Filled.Delete, null) }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ActionButton(
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    TextButton(onClick = onClick, modifier = Modifier.weight(1f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun PasteBar(label: String, onPaste: () -> Unit, onCancel: () -> Unit) {
    BottomAppBar {
        Text(label, modifier = Modifier.weight(1f).padding(start = 16.dp))
        TextButton(onClick = onCancel) { Text("Annuler") }
        TextButton(onClick = onPaste) {
            Icon(Icons.Filled.ContentPaste, contentDescription = null)
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
