package com.gallery.thekami.ui.collections

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.navigation.CollectionsRoute
import com.gallery.thekami.core.domain.trash.TrashedMedia
import com.gallery.thekami.core.domain.trash.remainingDays
import com.gallery.thekami.core.ui.media.MediaThumbnail
import com.gallery.thekami.core.ui.motion.bouncyClickable
import com.gallery.thekami.core.ui.states.EmptyState
import com.gallery.thekami.core.ui.states.ErrorState
import com.gallery.thekami.core.ui.states.LoadingState
import com.gallery.thekami.core.ui.theme.GalleryColors
import com.gallery.thekami.core.ui.theme.GalleryTheme
import com.gallery.thekami.ui.quantityString
import com.gallery.thekami.ui.trash.TrashUiState

@Immutable
class TrashUi(
    val state: TrashUiState,
    val refresh: () -> Unit,
    val restore: (List<TrashedMedia>) -> Unit,
    val deleteForever: (List<TrashedMedia>) -> Unit,
)

/** Android's own Trash (Android 11+). Restore and delete are confirmed by Android itself. */
@Composable
fun TrashScreen(trash: TrashUi, bottomPadding: Dp, actions: CollectionsActions) {
    val spacing = GalleryTheme.spacing
    var selection by remember { mutableStateOf(listOf<String>()) }
    var confirmDelete by remember { mutableStateOf(false) }
    val selecting = selection.isNotEmpty()
    BackHandler(enabled = selecting) { selection = emptyList() }
    val ready = trash.state as? TrashUiState.Ready
    val chosen = ready?.items.orEmpty().filter { it.item.key in selection }

    Column(Modifier.fillMaxSize()) {
        if (selecting) {
            SelectionActionBar(
                count = selection.size,
                onCancel = { selection = emptyList() },
                actions = listOf(
                    MenuEntry(stringResource(R.string.trash_restore)) {
                        trash.restore(chosen)
                        selection = emptyList()
                    },
                    MenuEntry(stringResource(R.string.trash_delete_forever)) { confirmDelete = true },
                ),
            )
        } else {
            ScreenTopBar(title = stringResource(R.string.trash_title), onBack = actions.pop)
        }
        when (val s = trash.state) {
            TrashUiState.Loading -> LoadingState(Modifier.weight(1f))
            TrashUiState.Unsupported -> EmptyState(
                title = stringResource(R.string.trash_unsupported_title),
                message = stringResource(R.string.trash_unsupported_message),
                modifier = Modifier.weight(1f).padding(bottom = bottomPadding),
            )
            is TrashUiState.Failed -> ErrorState(
                title = stringResource(R.string.home_error_title),
                message = s.message ?: stringResource(R.string.trash_failed_message),
                onRetry = trash.refresh,
                modifier = Modifier.weight(1f),
            )
            is TrashUiState.Ready -> Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.trash_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs),
                )
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable(role = Role.Button) { actions.push(CollectionsRoute.HiddenTrash) }
                        .padding(horizontal = spacing.lg, vertical = spacing.sm),
                ) {
                    Text(stringResource(R.string.trash_hidden_row), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.trash_hidden_row_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (s.items.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.trash_empty_title),
                        message = stringResource(R.string.trash_empty_message),
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
                        items(s.items, key = { it.item.key }) { trashed ->
                            val key = trashed.item.key
                            val toggle = { selection = if (key in selection) selection - key else selection + key }
                            DaysLeftCell(trashed, s.nowMillis, key in selection, onClick = toggle, onLongClick = toggle)
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.trash_delete_title)) },
            text = { Text(stringResource(R.string.trash_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    trash.deleteForever(chosen)
                    selection = emptyList()
                }) { Text(stringResource(R.string.trash_delete_forever)) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.dialog_cancel)) } },
        )
    }
}

@Composable
internal fun DaysLeftCell(
    trashed: TrashedMedia,
    nowMillis: Long,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val days = remainingDays(trashed.expiresAtMillis, nowMillis)
    DaysLeftBox(
        item = trashed.item,
        label = when (days) {
            null -> stringResource(R.string.trash_unknown_expiry)
            0 -> stringResource(R.string.trash_expired)
            else -> quantityString(R.plurals.trash_days_left, days)
        },
        selected = selected,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}

@Composable
internal fun DaysLeftBox(
    item: com.gallery.thekami.core.domain.media.MediaItem,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Box(Modifier.aspectRatio(1f).bouncyClickable(onClick = onClick, onLongClick = onLongClick)) {
        MediaThumbnail(item, Modifier.fillMaxSize())
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
        )
        if (selected) Box(Modifier.fillMaxSize().border(3.dp, GalleryColors.Lime))
    }
}
