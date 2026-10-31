package com.gallery.thekami.core.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FolderIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Folder",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 6f)
            horizontalLineTo(10f)
            lineTo(12f, 8f)
            horizontalLineTo(21f)
            verticalLineTo(19f)
            horizontalLineTo(3f)
            close()
        }
    }.build()
}
