package com.example.gestionfichiers.ui

import android.content.Context
import android.text.format.Formatter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =====================================================================
// Types de fichiers : icône et couleur (cahier § 4.3)
// =====================================================================

/** Couleur claire / couleur sombre : teintes plus claires en thème sombre pour garder le contraste. */
enum class FileKind(val icon: ImageVector, val light: Color, val dark: Color) {
    FOLDER(Icons.Filled.Folder, Color(0xFF2D5BFF), Color(0xFF5B7FFF)),
    IMAGE(Icons.Filled.Image, Color(0xFF12A37F), Color(0xFF4FD8B4)),
    VIDEO(Icons.Filled.Movie, Color(0xFFE8590C), Color(0xFFFF9B57)),
    AUDIO(Icons.Filled.MusicNote, Color(0xFF8B5CF6), Color(0xFFC08CFF)),
    PDF(Icons.Filled.PictureAsPdf, Color(0xFFD93838), Color(0xFFFF8A8A)),
    ARCHIVE(Icons.Filled.FolderZip, Color(0xFFA0703E), Color(0xFFD7A97E)),
    APK(Icons.Filled.Android, Color(0xFF3F8F3A), Color(0xFF8FD58A)),
    DOCUMENT(Icons.Filled.Description, Color(0xFF0284C7), Color(0xFF5CC8F5)),
    OTHER(Icons.AutoMirrored.Filled.InsertDriveFile, Color(0xFF64748B), Color(0xFF9AA6C0));

    @Composable
    fun color(): Color = if (isDark()) dark else light

    val hasThumbnail: Boolean get() = this == IMAGE || this == VIDEO

    companion object {
        fun of(file: File): FileKind = if (file.isDirectory) FOLDER else ofName(file.name)

        fun ofName(name: String): FileKind = when (name.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg", "png", "gif", "webp", "heic", "heif", "bmp" -> IMAGE
            "mp4", "mkv", "avi", "mov", "webm", "3gp", "m4v" -> VIDEO
            "mp3", "wav", "ogg", "m4a", "flac", "aac", "opus", "amr" -> AUDIO
            "pdf" -> PDF
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz" -> ARCHIVE
            "apk", "xapk", "apks" -> APK
            "doc", "docx", "odt", "rtf", "txt", "md", "xls", "xlsx", "ods", "csv",
            "ppt", "pptx", "odp", "json", "xml", "html", "htm" -> DOCUMENT
            else -> OTHER
        }
    }
}

/** Chargeur de miniatures (images et images extraites des vidéos), fourni par l'écran principal. */
val LocalThumbLoader = staticCompositionLocalOf<ImageLoader?> { null }

// =====================================================================
// Glisser-déposer (F5)
// =====================================================================

/** État du glisser-déposer. Positions en coordonnées de la racine de l'écran. */
class DragState {
    var dragging by mutableStateOf<List<File>>(emptyList())
    var pointer by mutableStateOf(Offset.Zero)
    val folderBounds = mutableMapOf<File, Rect>()
    /** EF-22 : volets du double volet ; déposer dans un volet déplace vers son dossier. */
    val paneBounds = mutableMapOf<File, Rect>()

    val isDragging: Boolean get() = dragging.isNotEmpty()

    /** Un dossier visé a priorité sur le volet qui le contient. */
    val targetIsPane: Boolean get() = folderTarget() == null && paneTarget() != null

    /** Dossier qui contient déjà les éléments déplacés : pas une cible valable. */
    private fun isSource(folder: File) = dragging.any { it.parentFile == folder }

    private fun folderTarget(): File? =
        folderBounds.entries.firstOrNull { (folder, rect) ->
            rect.contains(pointer) && folder !in dragging && !isSource(folder)
        }?.key

    private fun paneTarget(): File? =
        paneBounds.entries.firstOrNull { (dir, rect) ->
            rect.contains(pointer) && dir !in dragging && !isSource(dir)
        }?.key

    fun target(): File? = folderTarget() ?: paneTarget()

    fun reset() {
        dragging = emptyList()
    }
}

/**
 * Gestes communs à la grille et à la liste : appui = ouvrir (ou cocher en mode sélection),
 * appui long = sélectionner, appui long puis glisser = déplacer vers un dossier.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun Modifier.fileGestures(
    file: File,
    drag: DragState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDragStart: () -> Unit,
    onDrop: () -> Unit
): Modifier {
    var origin by remember(file) { mutableStateOf(Offset.Zero) }
    DisposableEffect(file) {
        onDispose { drag.folderBounds.remove(file) }
    }
    return this
        .onGloballyPositioned { coords ->
            origin = coords.positionInRoot()
            if (file.isDirectory) drag.folderBounds[file] = coords.boundsInRoot()
        }
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)
        // Placé après combinedClickable : reçoit les événements en premier
        .pointerInput(file) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    drag.pointer = origin + offset
                    onDragStart()
                },
                onDrag = { change, amount ->
                    change.consume()
                    drag.pointer += amount
                },
                onDragEnd = onDrop,
                onDragCancel = { drag.reset() }
            )
        }
}

// =====================================================================
// Miniature ou icône colorée
// =====================================================================

/**
 * Miniature pour les images et les vidéos, icône colorée selon le type sinon.
 * L'icône reste visible tant que la miniature charge, ou si elle ne peut pas être créée.
 * Une coche remplace le tout si l'élément est sélectionné.
 */
@Composable
internal fun FileThumb(file: File, selected: Boolean, size: Dp, iconSize: Dp, kind: FileKind = remember(file) { FileKind.of(file) }) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        if (selected) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(iconSize), tint = colors.primary)
        } else {
            Icon(kind.icon, contentDescription = null, modifier = Modifier.size(iconSize), tint = kind.color())
            val loader = LocalThumbLoader.current
            if (kind.hasThumbnail && loader != null) {
                val context = LocalContext.current
                val px = with(LocalDensity.current) { size.roundToPx() }
                var loaded by remember(file) { mutableStateOf(false) }
                val request = remember(file, px) {
                    ImageRequest.Builder(context)
                        .data(file)
                        .size(px)
                        .apply { if (kind == FileKind.VIDEO) videoFrameMillis(1000) }
                        .memoryCacheKey("${file.absolutePath}@${file.lastModified()}#$px")
                        .crossfade(true)
                        .build()
                }
                AsyncImage(
                    model = request,
                    imageLoader = loader,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onState = { loaded = it is AsyncImagePainter.State.Success },
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(10.dp))
                )
                if (kind == FileKind.VIDEO && loaded) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(size / 3)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(2.dp)
                    )
                }
            }
            // Badge de l'extension dans la couleur du type (cahier § 4.3)
            val ext = remember(file) { if (file.isDirectory) "" else file.extension.uppercase().take(4) }
            if (ext.isNotEmpty() && size >= 40.dp) {
                Text(
                    ext,
                    color = if (isDark()) Cobalt.Night900 else Color.White,
                    fontSize = if (size >= 60.dp) 10.sp else 8.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(RoundedCornerShape(4.dp))
                        .background(kind.color())
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

// =====================================================================
// Détails : taille et date (nombre d'éléments pour un dossier)
// =====================================================================

private val longDate = SimpleDateFormat("d MMM yyyy", Locale.FRENCH)
private val shortDate = SimpleDateFormat("d MMM yy", Locale.FRENCH)

internal fun formatSize(context: Context, bytes: Long): String = Formatter.formatShortFileSize(context, bytes)

private fun countLabel(n: Int) = if (n <= 1) "$n élément" else "$n éléments"

/** Détails lus en arrière-plan (le nombre d'éléments d'un gros dossier peut prendre du temps). */
@Composable
private fun rememberDetails(file: File): Triple<Boolean, String, Long>? {
    val context = LocalContext.current
    val state = produceState<Triple<Boolean, String, Long>?>(null, file, file.lastModified()) {
        value = withContext(Dispatchers.IO) {
            if (file.isDirectory) Triple(true, countLabel(file.list()?.size ?: 0), file.lastModified())
            else Triple(false, formatSize(context, file.length()), file.lastModified())
        }
    }
    return state.value
}

// =====================================================================
// Case de la grille et ligne de la liste
// =====================================================================

/** Case de la grille : grande miniature ou icône, nom, taille et date. */
@Composable
internal fun FileTile(
    file: File,
    selected: Boolean,
    isDropTarget: Boolean,
    gestures: Modifier,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val bg = when {
        isDropTarget -> colors.primaryContainer
        selected -> colors.secondaryContainer
        else -> Color.Transparent
    }
    val details = rememberDetails(file)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .then(if (selected || isDropTarget) Modifier.border(2.dp, colors.primary, RoundedCornerShape(14.dp)) else Modifier)
            .then(gestures)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FileThumb(file, selected, size = 64.dp, iconSize = 56.dp)
        Spacer(Modifier.height(6.dp))
        Text(
            file.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (file.isDirectory) FontWeight.Medium else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            details?.let { (_, main, date) -> "$main\n${shortDate.format(Date(date))}" } ?: "\n",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2
        )
    }
}

/** Ligne de la liste : miniature ou icône, nom, détails ; en recherche, le dossier où se trouve l'élément. */
@Composable
internal fun FileRow(
    file: File,
    selected: Boolean,
    isDropTarget: Boolean,
    gestures: Modifier,
    location: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val bg = when {
        isDropTarget -> colors.primaryContainer
        selected -> colors.secondaryContainer
        else -> colors.surfaceContainerLowest
    }
    val details = rememberDetails(file)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .then(if (selected || isDropTarget) Modifier.border(2.dp, colors.primary, RoundedCornerShape(16.dp)) else Modifier)
            .then(gestures)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileThumb(file, selected, size = 48.dp, iconSize = 38.dp)
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
                details?.let { (_, main, date) -> "$main  ·  ${longDate.format(Date(date))}" } ?: " ",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
            if (location != null) {
                Text(
                    location,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
