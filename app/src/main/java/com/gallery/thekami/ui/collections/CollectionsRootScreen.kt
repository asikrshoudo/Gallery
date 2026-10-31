package com.gallery.thekami.ui.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.navigation.CollectionsRoute
import com.gallery.thekami.core.ui.icons.CollectionsIcon
import com.gallery.thekami.core.ui.icons.FolderIcon
import com.gallery.thekami.core.ui.theme.GalleryTheme
import com.gallery.thekami.ui.ScreenTitle
import com.gallery.thekami.ui.quantityString

/** Order: Search, People, Albums, Folders, Moments, Places. */
@Composable
fun CollectionsRootScreen(state: CollectionsState.Ready, bottomPadding: Dp, actions: CollectionsActions) {
    val spacing = GalleryTheme.spacing
    val unavailable = stringResource(R.string.not_available_yet)
    LazyColumn(
        contentPadding = PaddingValues(bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        item { ScreenTitle(stringResource(R.string.collections_title)) }
        item { SearchEntry(actions.openSearch, Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm)) }
        item { CollectionRow(Icons.Filled.Person, stringResource(R.string.collections_people), unavailable, null) }
        item {
            CollectionRow(
                icon = CollectionsIcon,
                title = stringResource(R.string.collections_albums),
                subtitle = quantityString(R.plurals.album_count, state.albums.size),
                onClick = { actions.push(CollectionsRoute.Albums) },
            )
        }
        item {
            CollectionRow(
                icon = FolderIcon,
                title = stringResource(R.string.collections_folders),
                subtitle = quantityString(R.plurals.folder_count, state.folders.totalCount),
                onClick = { actions.push(CollectionsRoute.Folders) },
            )
        }
        item { CollectionRow(Icons.Filled.DateRange, stringResource(R.string.collections_moments), unavailable, null) }
        item { CollectionRow(Icons.Filled.Place, stringResource(R.string.collections_places), unavailable, null) }
    }
}

@Composable
private fun SearchEntry(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = GalleryTheme.spacing
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp).semantics { role = Role.Button },
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            Modifier.padding(horizontal = spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = stringResource(R.string.collections_search_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = spacing.md),
            )
        }
    }
}

/** [onClick] = null marks a destination that is not available yet: dimmed, not clickable. */
@Composable
private fun CollectionRow(icon: ImageVector, title: String, subtitle: String, onClick: (() -> Unit)?) {
    val spacing = GalleryTheme.spacing
    val base = Modifier.fillMaxWidth().heightIn(min = 64.dp)
    val interactive = if (onClick != null) base.clickable(role = Role.Button, onClick = onClick) else base.alpha(0.6f)
    Row(
        interactive.padding(horizontal = spacing.lg, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
