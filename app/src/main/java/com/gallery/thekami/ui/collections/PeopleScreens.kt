package com.gallery.thekami.ui.collections

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.faces.itemsWithFaces
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.navigation.CollectionsRoute
import com.gallery.thekami.core.domain.people.PersonView
import com.gallery.thekami.core.ui.media.MediaThumbnail
import com.gallery.thekami.core.ui.states.EmptyState
import com.gallery.thekami.core.ui.theme.GalleryColors
import com.gallery.thekami.core.ui.theme.GalleryTheme
import com.gallery.thekami.ui.PrimaryButton
import com.gallery.thekami.ui.quantityString

@Composable
fun PeopleScreen(state: CollectionsState.Ready, bottomPadding: Dp, actions: CollectionsActions) {
    val spacing = GalleryTheme.spacing
    var creating by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(title = stringResource(R.string.collections_people), onBack = actions.pop) {
            IconButton(onClick = { creating = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.people_add))
            }
        }
        if (state.people.isEmpty()) {
            Column(Modifier.weight(1f).padding(bottom = bottomPadding)) {
                EmptyState(
                    title = stringResource(R.string.people_empty_title),
                    message = stringResource(R.string.people_empty_message),
                    modifier = Modifier.weight(1f),
                )
                Box(Modifier.fillMaxWidth().padding(horizontal = spacing.lg, vertical = spacing.md)) {
                    PrimaryButton(stringResource(R.string.people_add), { creating = true })
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(104.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = bottomPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(state.people, key = { it.id }) { view ->
                    PersonCell(view) { actions.push(CollectionsRoute.PersonDetail(view.id)) }
                }
            }
        }
    }
    if (creating) {
        PersonDialog(
            title = stringResource(R.string.people_add),
            confirmLabel = stringResource(R.string.person_create),
            initialName = "",
            initialRelationship = null,
            onConfirm = { name, relationship ->
                actions.createPerson(name, relationship)
                creating = false
            },
            onDismiss = { creating = false },
        )
    }
}

@Composable
private fun PersonCell(view: PersonView, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Avatar(view, Modifier.size(88.dp))
        Text(
            view.name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
        view.person.relationship?.let {
            Text(
                relationshipLabel(it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Avatar(view: PersonView, modifier: Modifier) {
    val cover = view.cover
    if (cover != null) {
        MediaThumbnail(cover, modifier.clip(CircleShape))
    } else {
        Box(modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainer), contentAlignment = Alignment.Center) {
            Text(view.name.take(1).uppercase(), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun PersonDetailScreen(state: CollectionsState.Ready, personId: String, bottomPadding: Dp, actions: CollectionsActions) {
    val view = state.people.firstOrNull { it.id == personId }
    if (view == null) {
        NotFound(actions.pop)
        return
    }
    val spacing = GalleryTheme.spacing
    var selection by remember(personId) { mutableStateOf(listOf<String>()) }
    var editing by remember { mutableStateOf(false) }
    var merging by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    val selecting = selection.isNotEmpty()
    BackHandler(enabled = selecting) { selection = emptyList() }

    fun toggle(item: MediaItem) {
        selection = if (item.key in selection) selection - item.key else selection + item.key
    }

    Column(Modifier.fillMaxSize()) {
        if (selecting) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { selection = emptyList() }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.select_cancel))
                }
                Text(
                    stringResource(R.string.album_selected_count, selection.size),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (selection.size == 1) {
                    TextButton(onClick = {
                        actions.setPersonCover(personId, selection.single())
                        selection = emptyList()
                    }) { Text(stringResource(R.string.album_set_cover)) }
                }
                TextButton(onClick = {
                    actions.removePersonMedia(personId, selection)
                    selection = emptyList()
                }) { Text(stringResource(R.string.person_remove)) }
            }
        } else {
            ScreenTopBar(title = view.name, onBack = actions.pop) {
                IconButton(onClick = { actions.push(CollectionsRoute.PersonPicker(personId)) }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.person_add_photos))
                }
                OverflowMenu(
                    listOf(
                        MenuEntry(stringResource(R.string.person_edit)) { editing = true },
                        MenuEntry(stringResource(R.string.person_merge)) { merging = true },
                        MenuEntry(stringResource(R.string.person_delete_confirm)) { deleting = true },
                    ),
                )
            }
        }
        view.person.relationship?.let {
            Text(
                relationshipLabel(it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs),
            )
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
            Column(Modifier.weight(1f).padding(bottom = bottomPadding)) {
                EmptyState(
                    title = stringResource(R.string.person_empty_title),
                    message = stringResource(R.string.person_empty_message),
                    modifier = Modifier.weight(1f),
                )
                Box(Modifier.fillMaxWidth().padding(horizontal = spacing.lg, vertical = spacing.md)) {
                    PrimaryButton(stringResource(R.string.person_add_photos), { actions.push(CollectionsRoute.PersonPicker(personId)) })
                }
            }
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

    if (editing) {
        PersonDialog(
            title = stringResource(R.string.person_edit),
            confirmLabel = stringResource(R.string.person_save),
            initialName = view.name,
            initialRelationship = view.person.relationship,
            onConfirm = { name, relationship ->
                actions.editPerson(personId, name, relationship)
                editing = false
            },
            onDismiss = { editing = false },
        )
    }
    if (merging) {
        MergeDialog(
            self = view,
            others = state.people.filter { it.id != personId },
            onMerge = { targetId ->
                merging = false
                actions.pop()
                actions.mergePerson(targetId, personId)
            },
            onDismiss = { merging = false },
        )
    }
    if (deleting) {
        AlertDialog(
            onDismissRequest = { deleting = false },
            title = { Text(stringResource(R.string.person_delete_title)) },
            text = { Text(stringResource(R.string.person_delete_message, view.name)) },
            confirmButton = {
                TextButton(onClick = {
                    deleting = false
                    actions.pop()
                    actions.deletePerson(personId)
                }) { Text(stringResource(R.string.person_delete_confirm)) }
            },
            dismissButton = { TextButton(onClick = { deleting = false }) { Text(stringResource(R.string.dialog_cancel)) } },
        )
    }
}

@Composable
private fun MergeDialog(self: PersonView, others: List<PersonView>, onMerge: (String) -> Unit, onDismiss: () -> Unit) {
    var chosen by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.person_merge_title, self.name)) },
        text = {
            if (others.isEmpty()) {
                Text(stringResource(R.string.person_merge_none))
            } else {
                Column {
                    Text(
                        stringResource(R.string.person_merge_note, self.name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    LazyColumn {
                        items(others, key = { it.id }) { other ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(selected = chosen == other.id, role = Role.RadioButton) { chosen = other.id }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                RadioButton(selected = chosen == other.id, onClick = null)
                                Text(other.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = chosen != null, onClick = { chosen?.let(onMerge) }) {
                Text(stringResource(R.string.person_merge_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
    )
}

@Composable
fun PersonPickerScreen(
    state: CollectionsState.Ready,
    personId: String,
    faceCounts: Map<String, Int>,
    bottomPadding: Dp,
    actions: CollectionsActions,
) {
    val view = state.people.firstOrNull { it.id == personId }
    if (view == null) {
        NotFound(actions.pop)
        return
    }
    val spacing = GalleryTheme.spacing
    var onlyFaces by remember { mutableStateOf(false) }
    val candidates = remember(state.items, view.person.mediaKeys, onlyFaces, faceCounts) {
        val notYet = state.items.filter { it.key !in view.person.mediaKeys }
        if (onlyFaces) itemsWithFaces(notYet, faceCounts) else notYet
    }
    var selection by remember(personId) { mutableStateOf(listOf<String>()) }

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(title = stringResource(R.string.person_picker_title, view.name), onBack = actions.pop) {
            TextButton(
                enabled = selection.isNotEmpty(),
                onClick = {
                    actions.addPersonMedia(personId, selection)
                    actions.pop()
                },
            ) { Text(stringResource(R.string.picker_add_count, selection.size)) }
        }
        if (faceCounts.isNotEmpty()) {
            Column(Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs)) {
                FilterChip(
                    selected = onlyFaces,
                    onClick = { onlyFaces = !onlyFaces },
                    label = { Text(stringResource(R.string.person_only_faces)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GalleryColors.Lime,
                        selectedLabelColor = GalleryColors.OnLime,
                    ),
                )
                if (onlyFaces) {
                    Text(
                        stringResource(R.string.person_faces_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = spacing.xs),
                    )
                }
            }
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
