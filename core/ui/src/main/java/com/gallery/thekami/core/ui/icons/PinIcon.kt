package com.gallery.thekami.core.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Simple thumbtack. Pinning is an in-app mark only. */
val PinIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Pin",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(8f, 3f)
            horizontalLineTo(16f)
            verticalLineTo(9f)
            lineTo(18f, 13f)
            horizontalLineTo(6f)
            lineTo(8f, 9f)
            close()
            moveTo(11f, 13f)
            horizontalLineTo(13f)
            verticalLineTo(21f)
            horizontalLineTo(11f)
            close()
        }
    }.build()
}
