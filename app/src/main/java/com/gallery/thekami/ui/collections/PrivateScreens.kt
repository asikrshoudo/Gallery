package com.gallery.thekami.ui.collections

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.navigation.CollectionsRoute
import com.gallery.thekami.core.domain.trash.isExpired
import com.gallery.thekami.core.domain.trash.remainingDays
import com.gallery.thekami.core.ui.states.EmptyState
import com.gallery.thekami.core.ui.theme.GalleryTheme
import com.gallery.thekami.ui.PrimaryButton
import com.gallery.thekami.ui.privatemedia.PrivateTrashItem
import com.gallery.thekami.ui.quantityString

@Immutable
class PrivateAreaUi(
    val unlocked: Boolean,
    val deviceSecure: Boolean,
    val items: List<MediaItem>,
    val trashed: List<PrivateTrashItem>,
    val nowMillis: Long,
    val unlock: () -> Unit,
    val openMedia: (sequence: List<MediaItem>, item: MediaItem) -> Unit,
    val restore: (ids: List<String>) -> Unit,
    val deleteForever: (ids: List<String>) -> Unit,
)

@Composable
private fun LockedPanel(ui: PrivateAreaUi, bottomPadding: Dp, onBack: () -> Unit, title: String) {
    val spacing = GalleryTheme.spacing
    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(title = title, onBack = onBack)
        if (ui.deviceSecure) {
            EmptyState(
                title = stringResource(R.string.private_locked_title),
                message = stringResource(R.string.private_locked_message),
                modifier = Modifier.weight(1f),
            )
            Box(Modifier.fillMaxWidth().padding(start = spacing.lg, end = spacing.lg, top = spacing.md, bottom = spacing.md + bottomPadding)) {
                PrimaryButton(stringResource(R.string.private_unlock), ui.unlock)
            }
        } else {
            EmptyState(
                title = stringResource(R.string.private_no_lock_title),
                message = stringResource(R.string.private_no_lock_message),
                modifier = Modifier.weight(1f).padding(bottom = bottomPadding),
            )
        }
    }
}

@Composable
fun PrivateScreen(ui: PrivateAreaUi, bottomPadding: Dp, actions: CollectionsActions) {
    val title = stringResource(R.string.private_title)
    if (!ui.unlocked) {
        LockedPanel(ui, bottomPadding, actions.pop, title)
        return
    }
    val spacing = GalleryTheme.spacing
    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(title = title, onBack = actions.pop) {
            OverflowMenu(listOf(MenuEntry(stringResource(R.string.hidden_trash_title)) { actions.push(CollectionsRoute.HiddenTrash) }))
        }
        Text(
            stringResource(R.string.private_storage_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs),
        )
        if (ui.items.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.private_empty_title),
                message = stringResource(R.string.private_empty_message),
                modifier = Modifier.weight(1f).padding(bottom = bottomPadding),
            )
        } else {
            MediaGrid(
                items = ui.items,
                selected = emptySet(),
                onClick = { item -> ui.openMedia(ui.items, item) },
                onLongClick = null,
                contentPadding = PaddingValues(bottom = bottomPadding),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Items whose 2-day retention has passed are only flagged here. They are deleted only when the
 * user confirms, so a late or missed check never destroys anything.
 */
@Composable
fun HiddenTrashScreen(ui: PrivateAreaUi, bottomPadding: Dp, actions: CollectionsActions) {
    val title = stringResource(R.string.hidden_trash_title)
    if (!ui.unlocked) {
        LockedPanel(ui, bottomPadding, actions.pop, title)
        return
    }
    val spacing = GalleryTheme.spacing
    var selection by remember { mutableStateOf(listOf<String>()) }
    var pendingDelete by remember { mutableStateOf<List<String>?>(null) }
    val selecting = selection.isNotEmpty()
    BackHandler(enabled = selecting) { selection = emptyList() }
    val expired = ui.trashed.filter { isExpired(it.expiresAtMillis, ui.nowMillis) }

    Column(Modifier.fillMaxSize()) {
        if (selecting) {
            SelectionActionBar(
                count = selection.size,
                onCancel = { selection = emptyList() },
                actions = listOf(
                    MenuEntry(stringResource(R.string.hidden_trash_restore)) {
                        ui.restore(selection)
                        selection = emptyList()
                    },
                    MenuEntry(stringResource(R.string.trash_delete_forever)) { pendingDelete = selection },
                ),
            )
        } else {
            ScreenTopBar(title = title, onBack = actions.pop)
        }
        Text(
            stringResource(R.string.hidden_trash_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs),
        )
        if (expired.isNotEmpty()) {
            Column(Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs)) {
                Text(quantityString(R.plurals.hidden_trash_expired, expired.size), style = MaterialTheme.typography.titleSmall)
                TextButton(onClick = { pendingDelete = expired.map { it.id } }) {
                    Text(stringResource(R.string.hidden_trash_delete_expired))
                }
            }
        }
        if (ui.trashed.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.hidden_trash_empty_title),
                message = stringResource(R.string.hidden_trash_empty_message),
                modifier = Modifier.weight(1f).padding(bottom = bottomPadding),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(88.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = bottomPadding),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(ui.trashed, key = { it.id }) { entry ->
                    val toggle = { selection = if (entry.id in selection) selection - entry.id else selection + entry.id }
                    val days = remainingDays(entry.expiresAtMillis, ui.nowMillis)
                    DaysLeftBox(
                        item = entry.item,
                        label = if (days == null || days == 0) {
                            stringResource(R.string.trash_expired)
                        } else {
                            quantityString(R.plurals.trash_days_left, days)
                        },
                        selected = entry.id in selection,
                        onClick = toggle,
                        onLongClick = toggle,
                    )
                }
            }
        }
    }

    pendingDelete?.let { ids ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.hidden_delete_title)) },
            text = { Text(stringResource(R.string.hidden_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    ui.deleteForever(ids)
                    selection = selection - ids.toSet()
                    pendingDelete = null
                }) { Text(stringResource(R.string.trash_delete_forever)) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.dialog_cancel)) } },
        )
    }
}
