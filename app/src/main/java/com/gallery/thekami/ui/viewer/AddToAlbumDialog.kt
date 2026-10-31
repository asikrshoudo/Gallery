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
import com.gallery.thekami.core.domain.album.Album
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.ui.collections.AlbumNameDialog

/** Toggles membership immediately. Albums only store a reference; the file is untouched. */
@Composable
fun AddToAlbumDialog(
    item: MediaItem,
    albums: List<Album>,
    onToggle: (albumId: String, key: String, add: Boolean) -> Unit,
    onCreate: (name: String, key: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var creating by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_to_album_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (albums.isEmpty()) {
                    Text(
                        stringResource(R.string.add_to_album_none),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                albums.forEach { album ->
                    val inAlbum = item.key in album.mediaKeys
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .toggleable(
                                value = inAlbum,
                                role = Role.Checkbox,
                                onValueChange = { onToggle(album.id, item.key, it) },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Checkbox(checked = inAlbum, onCheckedChange = null)
                        Text(
                            album.name,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                TextButton(onClick = { creating = true }) { Text(stringResource(R.string.add_to_album_new)) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.add_to_album_done)) } },
    )
    if (creating) {
        AlbumNameDialog(
            title = stringResource(R.string.add_to_album_new),
            confirmLabel = stringResource(R.string.albums_create),
            initial = "",
            onConfirm = {
                onCreate(it, item.key)
                creating = false
            },
            onDismiss = { creating = false },
        )
    }
}
