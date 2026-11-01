package com.gallery.thekami.ui.privatemedia

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.MediaItem

/**
 * Progress and result dialogs for moving items in and out of Private. The "Copied" dialog is
 * the explicit confirmation step: the original is only deleted if the user chooses to.
 */
@Composable
fun PrivateOperationDialogs(
    moveState: MoveState,
    message: PrivateMessage?,
    onCancelCopy: () -> Unit,
    onDeleteOriginal: (MediaItem) -> Unit,
    onKeepOriginal: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    when (moveState) {
        MoveState.Idle -> Unit
        is MoveState.Copying -> AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.private_copying_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.private_copying_message, moveState.name))
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            },
            confirmButton = { TextButton(onClick = onCancelCopy) { Text(stringResource(R.string.viewer_cancel)) } },
        )
        is MoveState.Copied -> AlertDialog(
            onDismissRequest = onKeepOriginal,
            title = { Text(stringResource(R.string.private_copied_title)) },
            text = {
                Text(
                    stringResource(
                        if (moveState.canDeleteOriginal) R.string.private_copied_message else R.string.private_copied_message_no_delete,
                        moveState.original.displayName,
                    ),
                )
            },
            confirmButton = {
                if (moveState.canDeleteOriginal) {
                    TextButton(onClick = { onDeleteOriginal(moveState.original) }) {
                        Text(stringResource(R.string.private_delete_original))
                    }
                } else {
                    TextButton(onClick = onKeepOriginal) { Text(stringResource(R.string.viewer_ok)) }
                }
            },
            dismissButton = {
                if (moveState.canDeleteOriginal) {
                    TextButton(onClick = onKeepOriginal) { Text(stringResource(R.string.private_keep_original)) }
                }
            },
        )
    }
    if (message != null) {
        val (title, body) = when (message) {
            PrivateMessage.CopyReadFailed -> R.string.private_err_read_title to R.string.private_err_read
            PrivateMessage.CopyNoSpace -> R.string.private_err_read_title to R.string.private_err_space
            PrivateMessage.CopyVerifyFailed -> R.string.private_err_read_title to R.string.private_err_verify
            PrivateMessage.ExportDone -> R.string.private_export_done_title to R.string.private_export_done
            PrivateMessage.ExportUnsupported -> R.string.private_export_title to R.string.private_export_unsupported
            PrivateMessage.ExportFailed -> R.string.private_export_title to R.string.private_export_failed
            PrivateMessage.OriginalKept -> R.string.private_copied_title to R.string.private_original_kept
        }
        AlertDialog(
            onDismissRequest = onDismissMessage,
            title = { Text(stringResource(title)) },
            text = { Text(stringResource(body)) },
            confirmButton = { TextButton(onClick = onDismissMessage) { Text(stringResource(R.string.viewer_ok)) } },
        )
    }
}
