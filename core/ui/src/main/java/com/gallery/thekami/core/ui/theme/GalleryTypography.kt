package com.gallery.thekami.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gallery.thekami.core.domain.settings.FontChoice
import com.gallery.thekami.core.ui.R

/**
 * Ubuntu Mono is bundled (Regular + Bold) and is the default. There is no silent fallback to
 * another font: if the font resources were missing the build would fail. Android itself still
 * draws individual glyphs that Ubuntu Mono does not contain (for example Bengali or CJK
 * filenames) with system fonts. That is per-glyph platform behaviour, not a font preference.
 */
private val UbuntuMonoFamily = FontFamily(
    Font(R.font.ubuntu_mono_regular, FontWeight.Normal),
    Font(R.font.ubuntu_mono_bold, FontWeight.Bold),
)

fun FontChoice.toFontFamily(): FontFamily = when (this) {
    FontChoice.UbuntuMono -> UbuntuMonoFamily
    FontChoice.SystemDefault -> FontFamily.Default
}

/** Compact type scale applied on top of Material 3 defaults. */
fun galleryTypography(family: FontFamily): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = family),
        displayMedium = base.displayMedium.copy(fontFamily = family),
        displaySmall = base.displaySmall.copy(fontFamily = family),
        headlineLarge = base.headlineLarge.copy(fontFamily = family),
        headlineMedium = base.headlineMedium.copy(fontFamily = family),
        headlineSmall = base.headlineSmall.copy(fontFamily = family),
        titleLarge = base.titleLarge.copy(fontFamily = family, fontSize = 20.sp, lineHeight = 26.sp),
        titleMedium = base.titleMedium.copy(fontFamily = family, fontSize = 15.sp, lineHeight = 22.sp),
        titleSmall = base.titleSmall.copy(fontFamily = family),
        bodyLarge = base.bodyLarge.copy(fontFamily = family, fontSize = 15.sp, lineHeight = 22.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = family, fontSize = 13.sp, lineHeight = 19.sp),
        bodySmall = base.bodySmall.copy(fontFamily = family),
        labelLarge = base.labelLarge.copy(fontFamily = family, fontSize = 13.sp, lineHeight = 18.sp),
        labelMedium = base.labelMedium.copy(fontFamily = family),
        labelSmall = base.labelSmall.copy(fontFamily = family),
    )
}
