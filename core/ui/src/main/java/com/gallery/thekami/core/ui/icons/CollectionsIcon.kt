package com.gallery.thekami.core.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private fun PathBuilder.square(x: Float, y: Float, size: Float) {
    moveTo(x, y)
    horizontalLineTo(x + size)
    verticalLineTo(y + size)
    horizontalLineTo(x)
    close()
}

/** Four-tile grid icon for the Collections destination. */
val CollectionsIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Collections",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            square(3f, 3f, 7f)
            square(14f, 3f, 7f)
            square(3f, 14f, 7f)
            square(14f, 14f, 7f)
        }
    }.build()
}
