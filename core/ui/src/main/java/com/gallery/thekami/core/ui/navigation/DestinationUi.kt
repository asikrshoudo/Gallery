package com.gallery.thekami.core.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.gallery.thekami.core.domain.navigation.TopLevelDestination
import com.gallery.thekami.core.ui.R
import com.gallery.thekami.core.ui.icons.CollectionsIcon

internal fun TopLevelDestination.icon(): ImageVector = when (this) {
    TopLevelDestination.Home -> Icons.Filled.Home
    TopLevelDestination.Collections -> CollectionsIcon
}

@StringRes
internal fun TopLevelDestination.labelRes(): Int = when (this) {
    TopLevelDestination.Home -> R.string.nav_home
    TopLevelDestination.Collections -> R.string.nav_collections
}
