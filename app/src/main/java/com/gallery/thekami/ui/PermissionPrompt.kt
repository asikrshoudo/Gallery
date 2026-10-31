package com.gallery.thekami.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gallery.thekami.R
import com.gallery.thekami.core.ui.states.EmptyState
import com.gallery.thekami.core.ui.theme.GalleryTheme

/** Shown by Home and Collections when media permission is missing or was revoked. */
@Composable
fun PermissionPrompt(onRequestAccess: () -> Unit, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
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
