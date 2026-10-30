package com.gallery.thekami.core.domain.media

import java.util.Locale

/** "0:07", "12:34", "1:02:03". Negative values are treated as zero. */
fun formatDuration(millis: Long): String {
    val total = millis.coerceAtLeast(0) / 1000
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
    }
}
