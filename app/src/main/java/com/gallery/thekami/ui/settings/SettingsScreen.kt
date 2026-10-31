package com.gallery.thekami.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.MediaGrouping
import com.gallery.thekami.core.domain.settings.FontChoice
import com.gallery.thekami.core.domain.settings.HomeViewMode
import com.gallery.thekami.core.domain.settings.NavBarStyle
import com.gallery.thekami.core.domain.settings.ThemeMode
import com.gallery.thekami.core.ui.theme.GalleryColors

/** Every control here is wired to the shared settings store, so changes apply app-wide at once. */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val face by viewModel.faceState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val home = settings.home
    val allMedia = home.viewMode == HomeViewMode.AllMedia
    val version = remember { appVersion(context) }
    var showLicences by remember { mutableStateOf(false) }

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.windowInsetsPadding(WindowInsets.systemBars)) {
            Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
                Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)
            }
            LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
                item { SectionHeader(stringResource(R.string.settings_appearance)) }
                item {
                    SettingsCard {
                        SegmentedRow(
                            title = stringResource(R.string.settings_theme),
                            options = ThemeMode.entries,
                            selected = settings.themeMode,
                            label = { stringResource(if (it == ThemeMode.Dark) R.string.theme_dark else R.string.theme_light) },
                            onSelect = viewModel::setTheme,
                        )
                        CardDivider()
                        SegmentedRow(
                            title = stringResource(R.string.settings_font),
                            options = FontChoice.entries,
                            selected = settings.fontChoice,
                            label = {
                                stringResource(if (it == FontChoice.UbuntuMono) R.string.font_ubuntu_mono else R.string.font_system)
                            },
                            onSelect = viewModel::setFont,
                        )
                        CardDivider()
                        SegmentedRow(
                            title = stringResource(R.string.settings_nav_style),
                            options = NavBarStyle.entries,
                            selected = settings.navBarStyle,
                            label = { stringResource(if (it == NavBarStyle.Standard) R.string.nav_standard else R.string.nav_floating) },
                            onSelect = viewModel::setNavBarStyle,
                        )
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_home)) }
                item {
                    SettingsCard {
                        SegmentedRow(
                            title = stringResource(R.string.home_view_section),
                            options = HomeViewMode.entries,
                            selected = home.viewMode,
                            label = {
                                stringResource(if (it == HomeViewMode.CameraRoll) R.string.home_view_camera_roll else R.string.home_view_all_media)
                            },
                            onSelect = { viewModel.setHome(home.copy(viewMode = it)) },
                        )
                        CardDivider()
                        SegmentedRow(
                            title = stringResource(R.string.home_group_section),
                            options = MediaGrouping.entries,
                            selected = home.grouping,
                            label = {
                                stringResource(
                                    when (it) {
                                        MediaGrouping.Day -> R.string.group_day
                                        MediaGrouping.Month -> R.string.group_month
                                        MediaGrouping.Year -> R.string.group_year
                                    },
                                )
                            },
                            onSelect = { viewModel.setHome(home.copy(grouping = it)) },
                        )
                        CardDivider()
                        ToggleRow(stringResource(R.string.include_photos), null, home.includePhotos, true) {
                            viewModel.setHome(home.copy(includePhotos = it))
                        }
                        CardDivider()
                        ToggleRow(stringResource(R.string.include_videos), null, home.includeVideos, true) {
                            viewModel.setHome(home.copy(includeVideos = it))
                        }
                        CardDivider()
                        ToggleRow(stringResource(R.string.include_screenshots), null, home.includeScreenshots, allMedia) {
                            viewModel.setHome(home.copy(includeScreenshots = it))
                        }
                        CardDivider()
                        ToggleRow(stringResource(R.string.include_other_apps), null, home.includeOtherApps, allMedia) {
                            viewModel.setHome(home.copy(includeOtherApps = it))
                        }
                    }
                }
                item { Footnote(stringResource(R.string.home_include_note)) }

                item { SectionHeader(stringResource(R.string.settings_people)) }
                item {
                    SettingsCard {
                        ToggleRow(
                            title = stringResource(R.string.settings_detect_faces),
                            subtitle = stringResource(R.string.settings_detect_faces_desc),
                            checked = settings.people.detectFaces && face.supported,
                            enabled = face.supported,
                            onChange = viewModel::setDetectFaces,
                        )
                        if (!face.supported) {
                            Text(
                                stringResource(R.string.settings_detect_unsupported),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                            )
                        } else if (face.enabled || face.scanned > 0) {
                            Text(
                                text = when {
                                    face.pausedForBattery -> stringResource(R.string.settings_detect_paused)
                                    else -> stringResource(R.string.settings_detect_status, face.scanned, face.total)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                            )
                            TextButton(
                                onClick = viewModel::turnOffAndClearFaceData,
                                modifier = Modifier.padding(start = 8.dp),
                            ) { Text(stringResource(R.string.settings_clear_faces)) }
                        }
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_about)) }
                item {
                    SettingsCard {
                        InfoRow(stringResource(R.string.settings_version), version)
                        CardDivider()
                        ClickRow(stringResource(R.string.settings_licences)) { showLicences = true }
                        CardDivider()
                        Column(Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.settings_privacy), style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(R.string.settings_privacy_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
    if (showLicences) LicencesDialog { showLicences = false }
}

private fun appVersion(context: android.content.Context): String =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
        .getOrNull().orEmpty().ifEmpty { "-" }

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 28.dp, end = 16.dp, top = 20.dp, bottom = 6.dp),
    )
}

@Composable
private fun Footnote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 6.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) { Column(content = content) }
}

@Composable
private fun CardDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun <T> SegmentedRow(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = GalleryColors.Lime,
        activeContentColor = GalleryColors.OnLime,
        activeBorderColor = GalleryColors.Lime,
        inactiveContainerColor = Color.Transparent,
        inactiveContentColor = MaterialTheme.colorScheme.onSurface,
    )
    Column(Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    colors = colors,
                    icon = {},
                ) { Text(label(option), maxLines = 1) }
            }
        }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String?, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    val contentAlpha = if (enabled) 1f else 0.38f
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha))
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = GalleryColors.OnLime,
                checkedTrackColor = GalleryColors.Lime,
                checkedBorderColor = GalleryColors.Lime,
            ),
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ClickRow(label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LicencesDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val fontLicence = remember {
        runCatching { context.assets.open("licenses/ubuntu-mono-UFL.txt").bufferedReader().use { it.readText() } }
            .getOrDefault("")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_licences)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.settings_licences_gallery), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.settings_licences_font), style = MaterialTheme.typography.bodyLarge)
                if (fontLicence.isNotEmpty()) {
                    Text(fontLicence, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.viewer_ok)) } },
    )
}
