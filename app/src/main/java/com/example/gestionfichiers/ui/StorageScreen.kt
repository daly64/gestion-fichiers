package com.example.gestionfichiers.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gestionfichiers.FileManagerViewModel
import com.example.gestionfichiers.SizedFile
import com.example.gestionfichiers.SpaceInfo

/** Jauge d'espace : utilisé / libre. Orange au-delà de 90 % d'occupation. */
@Composable
internal fun SpaceGauge(label: String, space: SpaceInfo, modifier: Modifier = Modifier, compact: Boolean = true) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val warn = space.usedFraction > 0.9f
    val barColor = if (warn) androidx.compose.ui.graphics.Color(0xFFEF6C00) else colors.primary
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${formatSize(context, space.free)} libres sur ${formatSize(context, space.total)}",
                style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(if (compact) 4.dp else 8.dp))
        LinearProgressIndicator(
            progress = { space.usedFraction },
            modifier = Modifier.fillMaxWidth().height(if (compact) 4.dp else 10.dp).clip(RoundedCornerShape(5.dp)),
            color = barColor,
            trackColor = colors.surfaceContainerHighest,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
        if (!compact) {
            Spacer(Modifier.height(6.dp))
            Text(
                "${formatSize(context, space.used)} utilisés (${(space.usedFraction * 100).toInt()} %)",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

/** EF-20 : écran d'analyse du stockage. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnalysisScreen(vm: FileManagerViewModel, snackbar: SnackbarHostState) {
    val context = LocalContext.current
    val volume = vm.analysisVolume
    val result = vm.analysis

    BackHandler { vm.closeAnalysis() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Analyse du stockage", fontWeight = FontWeight.SemiBold)
                        if (volume != null) {
                            Text(
                                volume.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = vm::closeAnalysis) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (volume != null && !vm.analyzing) {
                        IconButton(onClick = { vm.analyze(volume) }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Recalculer")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val space = remember(volume, result) { volume?.let { vm.spaceOf(it) } }
        if (vm.analyzing || result == null) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Calcul de la taille des dossiers…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        val maxFolder = result.folders.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1
        val maxFile = result.files.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (space != null && volume != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        SpaceGauge(volume.label, space, Modifier.padding(16.dp), compact = false)
                    }
                }
            }
            if (result.trashSize > 0) {
                item {
                    AnalysisRow(
                        icon = { Icon(Icons.Outlined.Delete, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        title = "Corbeille",
                        subtitle = "Videz-la pour libérer de la place",
                        size = formatSize(context, result.trashSize),
                        fraction = null,
                        onClick = { vm.closeAnalysis(); vm.openTrash() }
                    )
                }
            }
            item { SectionHeader("Plus gros dossiers") }
            if (result.folders.isEmpty()) item { EmptyLine("Aucun dossier") }
            items(result.folders, key = { "d" + it.file.absolutePath }) { sized ->
                SizedRow(vm, sized, sized.size.toFloat() / maxFolder)
            }
            item { SectionHeader("Plus gros fichiers") }
            if (result.files.isEmpty()) item { EmptyLine("Aucun fichier") }
            items(result.files, key = { "f" + it.file.absolutePath }) { sized ->
                SizedRow(vm, sized, sized.size.toFloat() / maxFile, showPath = true)
            }
        }
    }
}

@Composable
private fun SizedRow(vm: FileManagerViewModel, sized: SizedFile, fraction: Float, showPath: Boolean = false) {
    val context = LocalContext.current
    AnalysisRow(
        icon = { FileThumb(sized.file, selected = false, size = 40.dp, iconSize = 32.dp) },
        title = sized.file.name,
        subtitle = if (showPath) vm.displayPath(sized.file.parentFile) else null,
        size = formatSize(context, sized.size),
        fraction = fraction,
        onClick = { vm.revealFromAnalysis(sized.file) }
    )
}

@Composable
private fun AnalysisRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String?,
    size: String,
    fraction: Float?,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { icon() }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(size, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (fraction != null) {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { fraction.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun EmptyLine(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
