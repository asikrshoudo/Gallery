package com.gallery.thekami.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val GalleryDarkColors: ColorScheme = darkColorScheme(
    primary = GalleryColors.Lime,
    onPrimary = GalleryColors.NearBlack,
    primaryContainer = GalleryColors.Lime,
    onPrimaryContainer = GalleryColors.NearBlack,
    secondary = Color(0xFFD0C9C6),
    onSecondary = GalleryColors.NearBlack,
    secondaryContainer = Color(0xFF2B2321),
    onSecondaryContainer = Color(0xFFEFEAE8),
    tertiary = Color(0xFFD0C9C6),
    onTertiary = GalleryColors.NearBlack,
    background = GalleryColors.NearBlack,
    onBackground = Color(0xFFEFEAE8),
    surface = Color(0xFF161110),
    onSurface = Color(0xFFEFEAE8),
    surfaceVariant = Color(0xFF2B2321),
    onSurfaceVariant = Color(0xFFB5ADAA),
    surfaceContainer = Color(0xFF1E1716),
    surfaceContainerHigh = Color(0xFF271F1D),
    outline = Color(0xFF7A716E),
    outlineVariant = Color(0xFF3A322F),
)

internal val GalleryLightColors: ColorScheme = lightColorScheme(
    primary = GalleryColors.NearBlack,
    onPrimary = GalleryColors.White,
    primaryContainer = GalleryColors.Lime,
    onPrimaryContainer = GalleryColors.NearBlack,
    secondary = Color(0xFF5E5654),
    onSecondary = GalleryColors.White,
    secondaryContainer = Color(0xFFE9E4E1),
    onSecondaryContainer = GalleryColors.NearBlack,
    tertiary = Color(0xFF5E5654),
    onTertiary = GalleryColors.White,
    background = GalleryColors.OffWhite,
    onBackground = GalleryColors.NearBlack,
    surface = GalleryColors.White,
    onSurface = GalleryColors.NearBlack,
    surfaceVariant = Color(0xFFE9E5E2),
    onSurfaceVariant = Color(0xFF5E5654),
    surfaceContainer = Color(0xFFF1EFEC),
    surfaceContainerHigh = Color(0xFFEAE7E4),
    outline = Color(0xFF8A817E),
    outlineVariant = Color(0xFFD6D0CC),
)
