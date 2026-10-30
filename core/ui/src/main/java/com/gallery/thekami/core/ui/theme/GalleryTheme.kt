package com.gallery.thekami.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import com.gallery.thekami.core.domain.settings.FontChoice
import com.gallery.thekami.core.domain.settings.ThemeMode

object GalleryTheme {
    val spacing: GallerySpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current
}

@Composable
fun GalleryTheme(
    themeMode: ThemeMode = ThemeMode.Dark,
    fontChoice: FontChoice = FontChoice.UbuntuMono,
    content: @Composable () -> Unit,
) {
    val colors = when (themeMode) {
        ThemeMode.Dark -> GalleryDarkColors
        ThemeMode.Light -> GalleryLightColors
    }
    val typography = remember(fontChoice) { galleryTypography(fontChoice.toFontFamily()) }
    CompositionLocalProvider(LocalSpacing provides GallerySpacing()) {
        MaterialTheme(colorScheme = colors, typography = typography, content = content)
    }
}
