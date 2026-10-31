package com.gallery.thekami.ui.collections

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.album.AlbumView
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.navigation.CollectionsRoute
import com.gallery.thekami.core.ui.media.MediaThumbnail
import com.gallery.thekami.core.ui.states.EmptyState
import com.gallery.thekami.core.ui.theme.GalleryTheme
import com.gallery.thekami.ui.PrimaryButton
import com.gallery.thekami.ui.quantityString

@Composable
fun AlbumsScreen(state: CollectionsState.Ready, bottomPadding: Dp, actions: CollectionsActions) {
    val spacing = GalleryTheme.spacing
    var creating by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<AlbumView?>(null) }
    var deleting by remember { mutableStateOf<AlbumView?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(title = stringResource(R.string.albums_title), onBack = actions.pop) {
            IconButton(onClick = { creating = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.albums_new))
            }
        }
        if (state.albums.isEmpty()) {
            Column(Modifier.weight(1f).padding(bottom = bottomPadding)) {
                EmptyState(
                    title = stringResource(R.string.albums_empty_title),
                    message = stringResource(R.string.albums_empty_message),
                    modifier = Modifier.weight(1f),
                )
                Box(Modifier.fillMaxWidth().padding(horizontal = spacing.lg, vertical = spacing.md)) {
                    PrimaryButton(stringResource(R.string.albums_new), { creating = true })
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = bottomPadding)) {
                items(state.albums, key = { it.id }) { view ->
                    AlbumRow(
                        view = view,
                        onOpen = { actions.push(CollectionsRoute.AlbumDetail(view.id)) },
                        onRename = { renaming = view },
                        onDelete = { deleting = view },
                    )
                }
            }
        }
    }

    if (creating) {
        AlbumNameDialog(
            title = stringResource(R.string.albums_new),
            confirmLabel = stringResource(R.string.albums_create),
            initial = "",
            onConfirm = {
                actions.createAlbum(it)
                creating = false
            },
            onDismiss = { creating = false },
        )
    }
    renaming?.let { view ->
        AlbumNameDialog(
            title = stringResource(R.string.albums_rename_title),
            confirmLabel = stringResource(R.string.albums_rename_confirm),
            initial = view.name,
            onConfirm = {
                actions.renameAlbum(view.id, it)
                renaming = null
            },
            onDismiss = { renaming = null },
        )
    }
    deleting?.let { view ->
        ConfirmDeleteAlbumDialog(
            name = view.name,
            onConfirm = {
                actions.deleteAlbum(view.id)
                deleting = null
            },
            onDismiss = { deleting = null },
        )
    }
}

@Composable
private fun AlbumRow(view: AlbumView, onOpen: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit) {
    val spacing = GalleryTheme.spacing
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(start = spacing.lg, top = spacing.sm, bottom = spacing.sm, end = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumCover(view.cover, Modifier.size(56.dp))
        Column(Modifier.weight(1f).padding(horizontal = spacing.md)) {
            Text(view.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                quantityString(R.plurals.item_count, view.items.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OverflowMenu(
            listOf(
                MenuEntry(stringResource(R.string.album_menu_rename), onRename),
                MenuEntry(stringResource(R.string.album_menu_delete), onDelete),
            ),
        )
    }
}

@Composable
private fun AlbumCover(cover: MediaItem?, modifier: Modifier) {
    val shape = RoundedCornerShape(8.dp)
    if (cover != null) {
        MediaThumbnail(cover, modifier.clip(shape))
    } else {
        Box(modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceContainer))
    }
}

@Composable
fun AlbumDetailScreen(state: CollectionsState.Ready, albumId: String, bottomPadding: Dp, actions: CollectionsActions) {
    val view = state.albums.firstOrNull { it.id == albumId }
    if (view == null) {
        NotFound(actions.pop)
        return
    }
    val spacing = GalleryTheme.spacing
    var selection by remember(albumId) { mutableStateOf(listOf<String>()) }
    var renaming by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    val selecting = selection.isNotEmpty()
    BackHandler(enabled = selecting) { selection = emptyList() }

    fun toggle(item: MediaItem) {
        selection = if (item.key in selection) selection - item.key else selection + item.key
    }

    Column(Modifier.fillMaxSize()) {
        if (selecting) {
            SelectionBar(
                count = selection.size,
                canSetCover = selection.size == 1,
                onCancel = { selection = emptyList() },
                onSetCover = {
                    actions.setCover(albumId, selection.single())
                    selection = emptyList()
                },
                onRemove = {
                    actions.removeMedia(albumId, selection)
                    selection = emptyList()
                },
            )
        } else {
            ScreenTopBar(title = view.name, onBack = actions.pop) {
                IconButton(onClick = { actions.push(CollectionsRoute.AlbumPicker(albumId)) }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.album_add_photos))
                }
                OverflowMenu(
                    listOf(
                        MenuEntry(stringResource(R.string.album_menu_rename)) { renaming = true },
                        MenuEntry(stringResource(R.string.album_menu_delete)) { deleting = true },
                    ),
                )
            }
        }
        if (view.missingCount > 0) {
            Text(
                text = quantityString(R.plurals.album_missing_count, view.missingCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs),
            )
        }
        if (view.items.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.album_empty_title),
                message = stringResource(R.string.album_empty_message),
                modifier = Modifier.weight(1f).padding(bottom = bottomPadding),
            )
        } else {
            MediaGrid(
                items = view.items,
                selected = selection.toSet(),
                onClick = { item -> if (selecting) toggle(item) else actions.openMedia(view.items, item) },
                onLongClick = { item -> if (item.key !in selection) toggle(item) },
                contentPadding = PaddingValues(bottom = bottomPadding),
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (renaming) {
        AlbumNameDialog(
            title = stringResource(R.string.albums_rename_title),
            confirmLabel = stringResource(R.string.albums_rename_confirm),
            initial = view.name,
            onConfirm = {
                actions.renameAlbum(albumId, it)
                renaming = false
            },
            onDismiss = { renaming = false },
        )
    }
    if (deleting) {
        ConfirmDeleteAlbumDialog(
            name = view.name,
            onConfirm = {
                deleting = false
                actions.pop()
                actions.deleteAlbum(albumId)
            },
            onDismiss = { deleting = false },
        )
    }
}

@Composable
private fun SelectionBar(
    count: Int,
    canSetCover: Boolean,
    onCancel: () -> Unit,
    onSetCover: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCancel) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.select_cancel))
        }
        Text(
            stringResource(R.string.album_selected_count, count),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        if (canSetCover) TextButton(onClick = onSetCover) { Text(stringResource(R.string.album_set_cover)) }
        TextButton(onClick = onRemove) { Text(stringResource(R.string.album_remove)) }
    }
}

@Composable
fun AlbumPickerScreen(state: CollectionsState.Ready, albumId: String, bottomPadding: Dp, actions: CollectionsActions) {
    val view = state.albums.firstOrNull { it.id == albumId }
    if (view == null) {
        NotFound(actions.pop)
        return
    }
    val candidates = remember(state.items, view.album.mediaKeys) {
        state.items.filter { it.key !in view.album.mediaKeys }
    }
    var selection by remember(albumId) { mutableStateOf(listOf<String>()) }

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(title = stringResource(R.string.picker_title, view.name), onBack = actions.pop) {
            TextButton(
                enabled = selection.isNotEmpty(),
                onClick = {
                    actions.addMedia(albumId, selection)
                    actions.pop()
                },
            ) { Text(stringResource(R.string.picker_add_count, selection.size)) }
        }
        if (candidates.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.picker_empty_title),
                message = stringResource(R.string.picker_empty_message),
                modifier = Modifier.weight(1f).padding(bottom = bottomPadding),
            )
        } else {
            MediaGrid(
                items = candidates,
                selected = selection.toSet(),
                onClick = { item -> selection = if (item.key in selection) selection - item.key else selection + item.key },
                onLongClick = null,
                contentPadding = PaddingValues(bottom = bottomPadding),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun NotFound(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(title = stringResource(R.string.album_not_found), onBack = onBack)
        EmptyState(
            title = stringResource(R.string.album_not_found),
            message = stringResource(R.string.album_not_found_message),
            modifier = Modifier.weight(1f),
        )
    }
}
