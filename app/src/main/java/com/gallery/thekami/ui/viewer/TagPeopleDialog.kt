package com.gallery.thekami.ui.viewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.people.Person
import com.gallery.thekami.core.domain.people.Relationship
import com.gallery.thekami.ui.collections.PersonDialog

/**
 * The user decides who is in the photo. Gallery does not recognise faces to fill this in, and
 * the tag is stored only in this app on this device.
 */
@Composable
fun TagPeopleDialog(
    item: MediaItem,
    people: List<Person>,
    onToggle: (personId: String, key: String, add: Boolean) -> Unit,
    onCreate: (name: String, relationship: Relationship?, key: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var creating by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.tag_people_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    stringResource(R.string.tag_people_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (people.isEmpty()) {
                    Text(
                        stringResource(R.string.tag_people_none),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                }
                people.forEach { person ->
                    val tagged = item.key in person.mediaKeys
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .toggleable(value = tagged, role = Role.Checkbox, onValueChange = { onToggle(person.id, item.key, it) }),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Checkbox(checked = tagged, onCheckedChange = null)
                        Text(person.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                TextButton(onClick = { creating = true }) { Text(stringResource(R.string.tag_people_new)) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.add_to_album_done)) } },
    )
    if (creating) {
        PersonDialog(
            title = stringResource(R.string.tag_people_new),
            confirmLabel = stringResource(R.string.person_create),
            initialName = "",
            initialRelationship = null,
            onConfirm = { name, relationship ->
                onCreate(name, relationship, item.key)
                creating = false
            },
            onDismiss = { creating = false },
        )
    }
}
