@file:OptIn(ExperimentalFoundationApi::class)

package com.gallery.thekami.ui.collections

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.ui.media.MediaThumbnail
import com.gallery.thekami.core.ui.theme.GalleryColors

/** Flat media grid. [selected] items get a lime border and a check mark. */
@Composable
fun MediaGrid(
    items: List<MediaItem>,
    selected: Set<String>,
    onClick: (MediaItem) -> Unit,
    onLongClick: ((MediaItem) -> Unit)?,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(88.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(items = items, key = { it.key }, contentType = { "media" }) { item ->
            val isSelected = item.key in selected
            Box(
                Modifier
                    .aspectRatio(1f)
                    .semantics { this.selected = isSelected }
                    .combinedClickable(
                        onClick = { onClick(item) },
                        onLongClick = onLongClick?.let { handler -> { handler(item) } },
                    ),
            ) {
                MediaThumbnail(item, Modifier.fillMaxSize())
                if (isSelected) {
                    Box(Modifier.fillMaxSize().border(3.dp, GalleryColors.Lime))
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = GalleryColors.Lime,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp),
                    )
                }
            }
        }
    }
}
