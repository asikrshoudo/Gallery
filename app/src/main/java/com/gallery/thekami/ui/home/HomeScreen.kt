package com.gallery.thekami.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.MediaAccess
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.settings.HomeSettings
import com.gallery.thekami.core.ui.states.EmptyState
import com.gallery.thekami.core.ui.states.ErrorState
import com.gallery.thekami.core.ui.states.LoadingState
import com.gallery.thekami.core.ui.theme.GalleryTheme
import com.gallery.thekami.ui.PermissionPrompt
import com.gallery.thekami.ui.PrimaryButton
import com.gallery.thekami.ui.SecondaryButton

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    uiState: HomeUiState,
    settings: HomeSettings,
    isRefreshing: Boolean,
    onRequestAccess: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    onSettingsChange: (HomeSettings) -> Unit,
    onOpenMedia: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCustomize by rememberSaveable { mutableStateOf(false) }
    val bottom = contentPadding.calculateBottomPadding()

    Column(modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding())) {
        HomeTopBar(isRefreshing, onRefresh, onCustomize = { showCustomize = true })
        Box(Modifier.weight(1f)) {
            when (uiState) {
                HomeUiState.Loading -> LoadingState(Modifier.padding(bottom = bottom))
                HomeUiState.PermissionRequired ->
                    PermissionPrompt(onRequestAccess, onOpenSettings, Modifier.padding(bottom = bottom))
                is HomeUiState.Failed -> ErrorState(
                    title = stringResource(R.string.home_error_title),
                    message = uiState.message ?: stringResource(R.string.home_error_message),
                    onRetry = onRefresh,
                    modifier = Modifier.padding(bottom = bottom),
                )
                is HomeUiState.Ready -> ReadyContent(
                    state = uiState,
                    bottomPadding = bottom,
                    onRequestAccess = onRequestAccess,
                    onCustomize = { showCustomize = true },
                    onOpenMedia = onOpenMedia,
                )
            }
        }
    }

    if (showCustomize) {
        HomeCustomizeDialog(
            settings = settings,
            onSettingsChange = onSettingsChange,
            onDismiss = { showCustomize = false },
        )
    }
}

@Composable
private fun HomeTopBar(isRefreshing: Boolean, onRefresh: () -> Unit, onCustomize: () -> Unit) {
    val spacing = GalleryTheme.spacing
    Column {
        Row(
            Modifier.fillMaxWidth().padding(start = spacing.lg, end = spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.home_refresh))
            }
            IconButton(onClick = onCustomize) {
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.home_customize))
            }
        }
        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainer,
            )
        }
    }
}

@Composable
private fun ReadyContent(
    state: HomeUiState.Ready,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onRequestAccess: () -> Unit,
    onCustomize: () -> Unit,
    onOpenMedia: (MediaItem) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        if (state.access == MediaAccess.Partial) PartialAccessBanner(onRequestAccess)
        when {
            state.libraryCount == 0 -> EmptyWithAction(
                title = stringResource(R.string.home_empty_title),
                message = stringResource(R.string.home_empty_message),
                actionLabel = null,
                onAction = {},
                modifier = Modifier.weight(1f).padding(bottom = bottomPadding),
            )
            state.shownCount == 0 -> EmptyWithAction(
                title = stringResource(R.string.home_filtered_title),
                message = stringResource(R.string.home_filtered_message),
                actionLabel = stringResource(R.string.home_customize),
                onAction = onCustomize,
                modifier = Modifier.weight(1f).padding(bottom = bottomPadding),
            )
            else -> MediaTimelineGrid(
                entries = state.timeline,
                contentPadding = PaddingValues(bottom = bottomPadding),
                onOpenMedia = onOpenMedia,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PartialAccessBanner(onRequestAccess: () -> Unit) {
    val spacing = GalleryTheme.spacing
    Row(
        Modifier.fillMaxWidth().padding(horizontal = spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.home_limited_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRequestAccess) { Text(stringResource(R.string.home_limited_action)) }
    }
}

@Composable
private fun EmptyWithAction(
    title: String,
    message: String,
    actionLabel: String?,
    onAction: () -> Unit,
    modifier: Modifier,
) {
    val spacing = GalleryTheme.spacing
    Column(modifier) {
        EmptyState(title = title, message = message, modifier = Modifier.weight(1f))
        if (actionLabel != null) {
            Box(Modifier.fillMaxWidth().padding(horizontal = spacing.lg, vertical = spacing.md)) {
                PrimaryButton(actionLabel, onAction)
            }
        }
    }
}
