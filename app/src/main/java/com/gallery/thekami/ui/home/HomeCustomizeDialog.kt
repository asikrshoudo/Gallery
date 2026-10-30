package com.gallery.thekami.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.MediaGrouping
import com.gallery.thekami.core.domain.settings.HomeSettings
import com.gallery.thekami.core.domain.settings.HomeViewMode

@Composable
fun HomeCustomizeDialog(
    settings: HomeSettings,
    onSettingsChange: (HomeSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val allMedia = settings.viewMode == HomeViewMode.AllMedia
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.home_customize_done)) } },
        title = { Text(stringResource(R.string.home_customize)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SectionLabel(stringResource(R.string.home_view_section))
                RadioRow(stringResource(R.string.home_view_camera_roll), settings.viewMode == HomeViewMode.CameraRoll) {
                    onSettingsChange(settings.copy(viewMode = HomeViewMode.CameraRoll))
                }
                RadioRow(stringResource(R.string.home_view_all_media), allMedia) {
                    onSettingsChange(settings.copy(viewMode = HomeViewMode.AllMedia))
                }

                SectionLabel(stringResource(R.string.home_group_section))
                RadioRow(stringResource(R.string.group_day), settings.grouping == MediaGrouping.Day) {
                    onSettingsChange(settings.copy(grouping = MediaGrouping.Day))
                }
                RadioRow(stringResource(R.string.group_month), settings.grouping == MediaGrouping.Month) {
                    onSettingsChange(settings.copy(grouping = MediaGrouping.Month))
                }
                RadioRow(stringResource(R.string.group_year), settings.grouping == MediaGrouping.Year) {
                    onSettingsChange(settings.copy(grouping = MediaGrouping.Year))
                }

                SectionLabel(stringResource(R.string.home_include_section))
                CheckRow(stringResource(R.string.include_photos), settings.includePhotos, true) {
                    onSettingsChange(settings.copy(includePhotos = it))
                }
                CheckRow(stringResource(R.string.include_videos), settings.includeVideos, true) {
                    onSettingsChange(settings.copy(includeVideos = it))
                }
                CheckRow(stringResource(R.string.include_screenshots), settings.includeScreenshots, allMedia) {
                    onSettingsChange(settings.copy(includeScreenshots = it))
                }
                CheckRow(stringResource(R.string.include_other_apps), settings.includeOtherApps, allMedia) {
                    onSettingsChange(settings.copy(includeOtherApps = it))
                }
                Text(
                    text = stringResource(R.string.home_include_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        )
    }
}
