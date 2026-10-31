package com.gallery.thekami.ui.collections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.FolderInfo
import com.gallery.thekami.core.domain.media.folderItems
import com.gallery.thekami.core.domain.navigation.CollectionsRoute
import com.gallery.thekami.core.ui.icons.FolderIcon
import com.gallery.thekami.core.ui.media.MediaThumbnail
import com.gallery.thekami.core.ui.states.EmptyState
import com.gallery.thekami.core.ui.theme.GalleryTheme
import com.gallery.thekami.ui.quantityString

@Composable
private fun folderTitle(info: FolderInfo): String = info.name.ifBlank { stringResource(R.string.folder_unknown) }

/** Camera and screenshot folders, plus one "Other folders" tile for everything else. */
@Composable
fun FoldersScreen(state: CollectionsState.Ready, bottomPadding: Dp, actions: CollectionsActions) {
    val folders = state.folders
    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(title = stringResource(R.string.folders_title), onBack = actions.pop)
        if (folders.totalCount == 0) {
            EmptyState(
                title = stringResource(R.string.folders_empty_title),
                message = stringResource(R.string.folders_empty_message),
                modifier = Modifier.weight(1f).padding(bottom = bottomPadding),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = bottomPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(folders.featured, key = { it.id }) { info -> FolderCard(info) { actions.push(CollectionsRoute.FolderDetail(info.id)) } }
                if (folders.others.isNotEmpty()) {
                    item(key = "other-folders") {
                        FolderTile(
                            title = stringResource(R.string.folders_other),
                            subtitle = quantityString(R.plurals.other_folder_count, folders.others.size),
                            onClick = { actions.push(CollectionsRoute.OtherFolders) },
                        ) {
                            Icon(
                                FolderIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Center).size(48.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OtherFoldersScreen(state: CollectionsState.Ready, bottomPadding: Dp, actions: CollectionsActions) {
    val spacing = GalleryTheme.spacing
    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(title = stringResource(R.string.other_folders_title), onBack = actions.pop)
        Text(
            text = stringResource(R.string.other_folders_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs),
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = bottomPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.folders.others, key = { it.id }) { info ->
                FolderCard(info) { actions.push(CollectionsRoute.FolderDetail(info.id)) }
            }
        }
    }
}

@Composable
fun FolderDetailScreen(state: CollectionsState.Ready, folderId: String, bottomPadding: Dp, actions: CollectionsActions) {
    val items = remember(state.items, folderId) { folderItems(state.items, folderId) }
    val info = (state.folders.featured + state.folders.others).firstOrNull { it.id == folderId }
    val title = if (info != null) folderTitle(info) else stringResource(R.string.folder_unknown)
    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(title = title, onBack = actions.pop)
        if (items.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.folder_empty_title),
                message = stringResource(R.string.folder_empty_message),
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

@Composable
private fun FolderCard(info: FolderInfo, onClick: () -> Unit) {
    FolderTile(
        title = folderTitle(info),
        subtitle = quantityString(R.plurals.item_count, info.itemCount),
        onClick = onClick,
    ) {
        MediaThumbnail(info.cover, Modifier.fillMaxSize())
    }
}

@Composable
private fun FolderTile(title: String, subtitle: String, onClick: () -> Unit, tile: @Composable BoxScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
            content = tile,
        )
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
