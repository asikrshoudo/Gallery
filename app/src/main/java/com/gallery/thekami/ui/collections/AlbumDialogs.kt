package com.gallery.thekami.ui.collections

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.album.MAX_ALBUM_NAME_LENGTH
import com.gallery.thekami.core.domain.album.normalizeAlbumName

@Composable
fun AlbumNameDialog(
    title: String,
    confirmLabel: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    val normalized = normalizeAlbumName(text)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(MAX_ALBUM_NAME_LENGTH) },
                singleLine = true,
                label = { Text(stringResource(R.string.albums_name_label)) },
            )
        },
        confirmButton = {
            TextButton(enabled = normalized != null, onClick = { normalized?.let(onConfirm) }) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
    )
}

@Composable
fun ConfirmDeleteAlbumDialog(name: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.albums_delete_title)) },
        text = { Text(stringResource(R.string.albums_delete_message, name)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.albums_delete_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
    )
}
