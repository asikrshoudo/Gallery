package com.gallery.thekami.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gallery.thekami.core.ui.theme.GalleryTheme

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    val spacing = GalleryTheme.spacing
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
    )
}
