package com.gallery.thekami.core.ui.media

import androidx.compose.runtime.compositionLocalOf
import com.gallery.thekami.core.domain.media.MediaMarks

/** Provided once at the app root so every thumbnail can show favourite and pinned badges. */
val LocalMediaMarks = compositionLocalOf { MediaMarks() }
