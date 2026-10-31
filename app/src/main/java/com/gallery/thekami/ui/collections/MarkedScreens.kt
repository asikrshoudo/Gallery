package com.gallery.thekami.ui.collections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.ui.states.EmptyState
import com.gallery.thekami.core.ui.theme.GalleryTheme
import com.gallery.thekami.ui.quantityString

@Composable
fun FavouritesScreen(state: CollectionsState.Ready, bottomPadding: Dp, actions: CollectionsActions) {
    MarkedScreen(
        title = stringResource(R.string.favourites_title),
        items = state.favourites,
        missing = state.favouriteMissing,
        emptyTitle = stringResource(R.string.favourites_empty_title),
        emptyMessage = stringResource(R.string.favourites_empty_message),
        bottomPadding = bottomPadding,
        actions = actions,
    )
}

@Composable
fun PinnedScreen(state: CollectionsState.Ready, bottomPadding: Dp, actions: CollectionsActions) {
    MarkedScreen(
        title = stringResource(R.string.pinned_title),
        items = state.pinned,
        missing = state.pinnedMissing,
        emptyTitle = stringResource(R.string.pinned_empty_title),
        emptyMessage = stringResource(R.string.pinned_empty_message),
        bottomPadding = bottomPadding,
        actions = actions,
    )
}

@Composable
private fun MarkedScreen(
    title: String,
    items: List<MediaItem>,
    missing: Int,
    emptyTitle: String,
    emptyMessage: String,
    bottomPadding: Dp,
    actions: CollectionsActions,
) {
    val spacing = GalleryTheme.spacing
    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(title = title, onBack = actions.pop)
        if (missing > 0) {
            Text(
                text = quantityString(R.plurals.album_missing_count, missing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs),
            )
        }
        if (items.isEmpty()) {
            EmptyState(
                title = emptyTitle,
                message = emptyMessage,
                modifier = Modifier.weight(1f).padding(bottom = bottomPadding),
            )
        } else {
            MediaGrid(
                items = items,
                selected = emptySet(),
                onClick = { item -> actions.openMedia(items, item) },
                onLongClick = null,
                contentPadding = PaddingValues(bottom = bottomPadding),
                modifier = Modifier.weight(1f),
            )
        }
    }
}
