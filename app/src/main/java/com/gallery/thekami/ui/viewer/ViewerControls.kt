package com.gallery.thekami.ui.viewer

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.formatDuration
import com.gallery.thekami.core.ui.icons.PauseIcon
import com.gallery.thekami.core.ui.theme.GalleryColors

@Composable
fun ViewerTopBar(
    isFavourite: Boolean,
    isPinned: Boolean,
    isPrivate: Boolean,
    onClose: () -> Unit,
    onToggleFavourite: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onAddToAlbum: () -> Unit,
    onTogglePinned: () -> Unit,
    onTagPeople: () -> Unit,
    onMoveToPrivate: () -> Unit,
    onMoveOutOfPrivate: () -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BarIcon(Icons.Filled.Close, R.string.viewer_close, onClose)
        Spacer(Modifier.weight(1f))
        // Private items have no favourite or share: they must not leave Private by accident.
        if (!isPrivate) {
            IconButton(onClick = onToggleFavourite) {
                Icon(
                    imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isFavourite) R.string.viewer_favourite_remove else R.string.viewer_favourite_add,
                    ),
                    tint = if (isFavourite) GalleryColors.Lime else Color.White,
                )
            }
            BarIcon(Icons.Filled.Share, R.string.viewer_share, onShare)
        }
        BarIcon(Icons.Filled.Delete, R.string.viewer_delete, onDelete)
        Box {
            BarIcon(Icons.Filled.MoreVert, R.string.menu_more) { menuOpen = true }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                if (isPrivate) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.viewer_move_out)) },
                        onClick = { menuOpen = false; onMoveOutOfPrivate() },
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.viewer_add_to_album)) },
                        onClick = { menuOpen = false; onAddToAlbum() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.viewer_tag_people)) },
                        onClick = { menuOpen = false; onTagPeople() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(if (isPinned) R.string.viewer_unpin else R.string.viewer_pin)) },
                        onClick = { menuOpen = false; onTogglePinned() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.viewer_move_to_private)) },
                        onClick = { menuOpen = false; onMoveToPrivate() },
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.viewer_info)) },
                    onClick = { menuOpen = false; onInfo() },
                )
            }
        }
    }
}

@Composable
private fun BarIcon(icon: ImageVector, @StringRes label: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = stringResource(label), tint = Color.White)
    }
}

@Composable
fun CenterPlayButton(state: PlayerUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (icon, label) = when {
        state.isEnded -> Icons.Filled.Refresh to R.string.viewer_replay
        state.isPlaying -> PauseIcon to R.string.viewer_pause
        else -> Icons.Filled.PlayArrow to R.string.viewer_play
    }
    Surface(
        onClick = onClick,
        modifier = modifier.size(64.dp),
        shape = CircleShape,
        color = GalleryColors.Lime,
        contentColor = GalleryColors.OnLime,
        shadowElevation = 6.dp,
    ) {
        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = stringResource(label), modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun VideoSeekBar(state: PlayerUiState, onSeek: (Long) -> Unit, modifier: Modifier = Modifier) {
    var dragging by remember { mutableStateOf<Float?>(null) }
    val duration = state.durationMs.coerceAtLeast(1).toFloat()
    val position = (dragging ?: state.positionMs.toFloat()).coerceIn(0f, duration)
    val seekLabel = stringResource(R.string.viewer_seek)
    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(formatDuration(position.toLong()), style = MaterialTheme.typography.labelMedium, color = Color.White)
        Slider(
            value = position,
            onValueChange = { dragging = it },
            onValueChangeFinished = {
                dragging?.let { onSeek(it.toLong()) }
                dragging = null
            },
            valueRange = 0f..duration,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp).semantics { contentDescription = seekLabel },
            colors = SliderDefaults.colors(
                thumbColor = GalleryColors.Lime,
                activeTrackColor = GalleryColors.Lime,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f),
            ),
        )
        Text(formatDuration(state.durationMs), style = MaterialTheme.typography.labelMedium, color = Color.White)
    }
}

/** Shown only for a still that has a paired motion video (see buildLivePhotoPairs). */
@Composable
fun LiveToggle(active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(if (active) R.string.viewer_live_stop else R.string.viewer_live_play)
    Surface(
        onClick = onClick,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp).semantics { contentDescription = label },
        shape = RoundedCornerShape(16.dp),
        color = if (active) GalleryColors.Lime else Color.Black.copy(alpha = 0.55f),
        contentColor = if (active) GalleryColors.OnLime else Color.White,
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(stringResource(R.string.viewer_live), style = MaterialTheme.typography.labelLarge)
        }
    }
}
