package com.gallery.thekami.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class GallerySpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

/** Sizes shared by interactive chrome. Touch targets stay at or above 48dp. */
object GalleryDimens {
    val MinTouchTarget = 48.dp
    val SearchButtonSize = 56.dp
    val NavItemWidth = 64.dp
    val NavItemHeight = 48.dp
}

internal val LocalSpacing = staticCompositionLocalOf { GallerySpacing() }
