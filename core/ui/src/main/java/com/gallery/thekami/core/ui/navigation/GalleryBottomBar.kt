package com.gallery.thekami.core.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.gallery.thekami.core.domain.navigation.TopLevelDestination
import com.gallery.thekami.core.domain.settings.NavBarStyle
import com.gallery.thekami.core.ui.theme.GalleryColors
import com.gallery.thekami.core.ui.theme.GalleryDimens
import com.gallery.thekami.core.ui.theme.GalleryTheme

/**
 * Icon-only bottom navigation with accessibility labels. [showSearch] must come from
 * NavigationState.showsBottomSearch so visibility has one source of truth.
 */
@Composable
fun GalleryBottomBar(
    selected: TopLevelDestination,
    showSearch: Boolean,
    style: NavBarStyle,
    onSelect: (TopLevelDestination) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = GalleryTheme.spacing
    val rowModifier = Modifier
        .windowInsetsPadding(WindowInsets.navigationBars)
        .padding(horizontal = spacing.lg, vertical = spacing.sm)
    when (style) {
        NavBarStyle.Standard -> Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            BarRow(selected, showSearch, floating = false, onSelect, onSearch, rowModifier)
        }
        NavBarStyle.Floating -> BarRow(
            selected, showSearch, floating = true, onSelect, onSearch,
            modifier.fillMaxWidth().then(rowModifier),
        )
    }
}

@Composable
private fun BarRow(
    selected: TopLevelDestination,
    showSearch: Boolean,
    floating: Boolean,
    onSelect: (TopLevelDestination) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier,
) {
    val spacing = GalleryTheme.spacing
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (floating) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 6.dp,
                ) {
                    NavItems(selected, onSelect, Modifier.padding(spacing.xs))
                }
            } else {
                NavItems(selected, onSelect, Modifier.fillMaxWidth())
            }
        }
        AnimatedVisibility(
            visible = showSearch,
            enter = fadeIn(tween(150)) + scaleIn(tween(150), initialScale = 0.8f),
            exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.8f),
        ) {
            SearchActionButton(onClick = onSearch)
        }
    }
}

@Composable
private fun NavItems(
    selected: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier,
) {
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(GalleryTheme.spacing.sm, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopLevelDestination.entries.forEach { destination ->
            NavItem(destination, destination == selected) { onSelect(destination) }
        }
    }
}

@Composable
private fun NavItem(destination: TopLevelDestination, selected: Boolean, onClick: () -> Unit) {
    val indicator by animateColorAsState(
        targetValue = if (selected) GalleryColors.Lime else Color.Transparent,
        animationSpec = tween(150),
        label = "navIndicator",
    )
    val tint = if (selected) GalleryColors.OnLime else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .size(GalleryDimens.NavItemWidth, GalleryDimens.NavItemHeight)
            .clip(RoundedCornerShape(24.dp))
            .background(indicator)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = destination.icon(),
            contentDescription = stringResource(destination.labelRes()),
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
    }
}
