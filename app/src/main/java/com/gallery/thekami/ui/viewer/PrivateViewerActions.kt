package com.gallery.thekami.ui.viewer

import androidx.compose.runtime.Immutable
import com.gallery.thekami.core.domain.media.MediaItem

@Immutable
class PrivateViewerActions(
    val moveToPrivate: (MediaItem) -> Unit,
    val moveOut: (MediaItem) -> Unit,
    val deleteFromPrivate: (MediaItem) -> Unit,
)
