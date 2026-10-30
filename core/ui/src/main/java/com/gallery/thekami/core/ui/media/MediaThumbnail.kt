package com.gallery.thekami.core.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.formatDuration
import com.gallery.thekami.core.ui.R

/**
 * Grid thumbnail. Coil decodes at the cell size (never full resolution), caches in a bounded
 * memory cache, and cancels the request when the cell leaves the screen. A missing or
 * undecodable file just leaves the neutral background; it does not crash.
 */
@Composable
fun MediaThumbnail(item: MediaItem, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val request = remember(item.key) { ImageRequest.Builder(context).data(item.uri).build() }
    val duration = item.durationMillis?.let { formatDuration(it) }
    val description = when {
        !item.isVideo -> stringResource(R.string.media_photo)
        duration != null -> stringResource(R.string.media_video_duration, duration)
        else -> stringResource(R.string.media_video)
    }
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainer)) {
        AsyncImage(
            model = request,
            contentDescription = description,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (item.isVideo) {
            VideoBadge(duration, Modifier.align(Alignment.BottomStart).padding(4.dp))
        }
    }
}

@Composable
private fun VideoBadge(duration: String?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clearAndSetSemantics {}
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        if (duration != null) {
            Text(duration, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}
