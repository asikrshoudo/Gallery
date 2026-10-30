package com.gallery.thekami.ui.viewer

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.ui.media.MediaThumbnail
import com.gallery.thekami.core.ui.theme.GalleryColors
import kotlin.math.abs
import kotlinx.coroutines.flow.first

private val ThumbSize = 52.dp
private val ThumbGap = 4.dp

/**
 * Thumbnail strip driven by [currentIndex] (the pager's page). Whenever the pager moves, the
 * strip scrolls to centre the same item and highlights it; tapping a thumbnail calls [onSelect].
 */
@Composable
fun Filmstrip(
    items: List<MediaItem>,
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (currentIndex - 3).coerceAtLeast(0))

    LaunchedEffect(currentIndex, items.size) {
        val viewport = snapshotFlow {
            listState.layoutInfo.let { it.viewportEndOffset - it.viewportStartOffset }
        }.first { it > 0 }
        val itemPx = with(density) { (ThumbSize + ThumbGap).toPx() }
        val offset = -((viewport - itemPx) / 2f).toInt()
        val nearby = listState.layoutInfo.visibleItemsInfo.any { abs(it.index - currentIndex) <= 8 }
        if (nearby) listState.animateScrollToItem(currentIndex, offset) else listState.scrollToItem(currentIndex, offset)
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth().height(ThumbSize + 16.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(ThumbGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(items, key = { _, item -> item.key }) { index, item ->
            FilmstripThumb(item, index, items.size, selected = index == currentIndex, size = ThumbSize) {
                onSelect(index)
            }
        }
    }
}

@Composable
private fun FilmstripThumb(
    item: MediaItem,
    index: Int,
    count: Int,
    selected: Boolean,
    size: Dp,
    onClick: () -> Unit,
) {
    val label = stringResource(R.string.viewer_go_to, index + 1, count)
    val shape = RoundedCornerShape(6.dp)
    Box(
        Modifier
            .size(size)
            .clip(shape)
            .graphicsLayer { alpha = if (selected) 1f else 0.7f }
            .then(if (selected) Modifier.border(2.dp, GalleryColors.Lime, shape) else Modifier)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = label },
    ) {
        MediaThumbnail(item, Modifier.fillMaxSize())
    }
}
