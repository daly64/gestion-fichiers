package com.example.gestionfichiers.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gestionfichiers.FileManagerViewModel
import com.example.gestionfichiers.TrashItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** F10 : écran de la corbeille. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrashScreen(vm: FileManagerViewModel, snackbar: SnackbarHostState) {
    var confirmForever by remember { mutableStateOf(false) }
    var confirmEmpty by remember { mutableStateOf(false) }
    val selectionActive = vm.trashSelected.isNotEmpty()

    BackHandler {
        if (selectionActive) vm.trashSelected.clear() else vm.closeTrash()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                if (selectionActive) "${vm.trashSelected.size} sélectionné(s)" else "Corbeille",
                                fontWeight = FontWeight.SemiBold
                            )
                            if (!selectionActive) {
                                Text(
                                    "Éléments effacés automatiquement après 30 jours",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (selectionActive) vm.trashSelected.clear() else vm.closeTrash()
                        }) {
                            Icon(
                                if (selectionActive) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour"
                            )
                        }
                    },
                    actions = {
                        if (!selectionActive && vm.trashItems.isNotEmpty()) {
                            TextButton(onClick = { confirmEmpty = true }) {
                                Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Vider")
                            }
                        }
                    }
                )
                if (vm.busy && vm.progress == null) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        },
        bottomBar = {
            val p = vm.progress
            when {
                p != null -> ProgressBar(p, vm.progressLabel, onCancel = vm::cancelOperation)
                selectionActive -> BottomAppBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        ActionButton("Restaurer", vm::restoreSelected) {
                            Icon(Icons.Filled.RestoreFromTrash, null)
                        }
                        ActionButton("Supprimer définitivement", { confirmForever = true }, MaterialTheme.colorScheme.error) {
                            Icon(Icons.Filled.DeleteForever, null)
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (vm.trashItems.isEmpty()) {
            EmptyState(Icons.Outlined.Delete, "La corbeille est vide", Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(vm.trashItems, key = { it.file.absolutePath }) { item ->
                    TrashRow(
                        item,
                        selected = item in vm.trashSelected,
                        onClick = { vm.toggleTrash(item) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (confirmForever) {
        AlertDialog(
            onDismissRequest = { confirmForever = false },
            icon = { Icon(Icons.Filled.DeleteForever, contentDescription = null) },
            title = { Text("Supprimer définitivement ${vm.trashSelected.size} élément(s) ?") },
            text = { Text("Ces éléments ne pourront plus être récupérés.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteForeverSelected()
                    confirmForever = false
                }) { Text("Supprimer", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmForever = false }) { Text("Annuler") } }
        )
    }

    if (confirmEmpty) {
        AlertDialog(
            onDismissRequest = { confirmEmpty = false },
            icon = { Icon(Icons.Filled.DeleteSweep, contentDescription = null) },
            title = { Text("Vider la corbeille ?") },
            text = { Text("Les ${vm.trashItems.size} élément(s) seront supprimés définitivement.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.emptyTrash()
                    confirmEmpty = false
                }) { Text("Vider", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmEmpty = false }) { Text("Annuler") } }
        )
    }
}

private val trashDateFormat = SimpleDateFormat("d MMM yyyy 'à' HH:mm", Locale.FRENCH)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrashRow(item: TrashItem, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val origin = File(item.originalPath).parent.orEmpty()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) colors.secondaryContainer else colors.surface)
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileThumb(item.file, selected, size = 48.dp, iconSize = 38.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(item.displayName, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "Supprimé le " + trashDateFormat.format(Date(item.deletedAt)),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
            if (origin.isNotEmpty()) {
                Text(
                    "Depuis $origin",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
