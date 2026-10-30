package com.gallery.thekami.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.MediaAccess
import com.gallery.thekami.core.domain.media.MediaLibraryState
import com.gallery.thekami.core.ui.states.EmptyState
import com.gallery.thekami.core.ui.states.ErrorState
import com.gallery.thekami.core.ui.states.LoadingState
import com.gallery.thekami.core.ui.theme.GalleryTheme
import com.gallery.thekami.ui.PrimaryButton
import com.gallery.thekami.ui.ScreenTitle
import com.gallery.thekami.ui.SecondaryButton

/**
 * Interim Home: real library state (permission, loading, error, counts). The photo timeline
 * grid replaces the summary in the next phase.
 */
@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    state: MediaLibraryState,
    isRefreshing: Boolean,
    onRequestAccess: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().padding(contentPadding)) {
        ScreenTitle(stringResource(R.string.app_name))
        when (state) {
            MediaLibraryState.Loading -> LoadingState(Modifier.weight(1f))
            is MediaLibraryState.PermissionRequired -> PermissionPrompt(onRequestAccess, onOpenSettings, Modifier.weight(1f))
            is MediaLibraryState.Failed -> ErrorState(
                title = stringResource(R.string.home_error_title),
                message = state.message ?: stringResource(R.string.home_error_message),
                onRetry = onRefresh,
                modifier = Modifier.weight(1f),
            )
            is MediaLibraryState.Ready -> LibrarySummary(state, isRefreshing, onRequestAccess, onRefresh, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PermissionPrompt(onRequestAccess: () -> Unit, onOpenSettings: () -> Unit, modifier: Modifier) {
    val spacing = GalleryTheme.spacing
    Column(modifier) {
        EmptyState(
            title = stringResource(R.string.home_permission_title),
            message = stringResource(R.string.home_permission_message),
            modifier = Modifier.weight(1f),
        )
        Column(
            Modifier.fillMaxWidth().padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            PrimaryButton(stringResource(R.string.home_permission_allow), onRequestAccess)
            SecondaryButton(stringResource(R.string.home_permission_settings), onOpenSettings)
        }
    }
}

@Composable
private fun LibrarySummary(
    state: MediaLibraryState.Ready,
    isRefreshing: Boolean,
    onRequestAccess: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier,
) {
    val spacing = GalleryTheme.spacing
    val resources = LocalContext.current.resources
    val counts = remember(state.items) {
        state.items.count { !it.isVideo } to state.items.count { it.isVideo }
    }
    val title = if (state.items.isEmpty()) {
        stringResource(R.string.home_empty_title)
    } else {
        stringResource(
            R.string.home_found_title,
            resources.getQuantityString(R.plurals.home_photo_count, counts.first, counts.first),
            resources.getQuantityString(R.plurals.home_video_count, counts.second, counts.second),
        )
    }
    val message = stringResource(
        if (state.items.isEmpty()) R.string.home_empty_message else R.string.home_ready_message,
    )
    Column(modifier) {
        EmptyState(title = title, message = message, modifier = Modifier.weight(1f))
        Column(
            Modifier.fillMaxWidth().padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            if (state.access == MediaAccess.Partial) {
                Text(
                    text = stringResource(R.string.home_limited_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SecondaryButton(stringResource(R.string.home_limited_action), onRequestAccess)
            }
            PrimaryButton(
                text = stringResource(if (isRefreshing) R.string.home_refreshing else R.string.home_refresh),
                onClick = onRefresh,
                enabled = !isRefreshing,
            )
        }
    }
}
