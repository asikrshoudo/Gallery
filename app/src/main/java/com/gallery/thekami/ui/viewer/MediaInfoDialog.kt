package com.gallery.thekami.ui.viewer

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.formatDuration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Shows only metadata MediaStore actually reported. Missing fields are left out, not invented. */
@Composable
fun MediaInfoDialog(item: MediaItem, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale)
    fun date(millis: Long) = formatter.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))

    val rows = buildList {
        add(stringResource(R.string.info_name) to item.displayName)
        add(stringResource(R.string.info_type) to item.mimeType)
        val width = item.width
        val height = item.height
        if (width != null && height != null) add(stringResource(R.string.info_dimensions) to "$width × $height")
        item.sizeBytes?.let { add(stringResource(R.string.info_size) to Formatter.formatFileSize(context, it)) }
        item.dateTakenMillis?.let { add(stringResource(R.string.info_taken) to date(it)) }
        item.dateModifiedMillis?.let { add(stringResource(R.string.info_modified) to date(it)) }
        val duration = item.durationMillis
        if (item.isVideo && duration != null) add(stringResource(R.string.info_duration) to formatDuration(duration))
        (item.relativePath?.takeIf { it.isNotEmpty() } ?: item.bucketName)?.let {
            add(stringResource(R.string.info_folder) to it)
        }
        item.location?.let {
            add(stringResource(R.string.info_location) to String.format(Locale.ROOT, "%.5f, %.5f", it.latitude, it.longitude))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.viewer_ok)) } },
        title = { Text(stringResource(R.string.info_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { (label, value) ->
                    Column {
                        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(value, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
    )
}
