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

/** "JPEG image", "QuickTime video"... built from the MIME type only. */
fun describeMediaType(mimeType: String): String {
    val parts = mimeType.lowercase().split('/', limit = 2)
    val kind = parts[0]
    val subtype = parts.getOrElse(1) { "" }
    val label = when (subtype) {
        "jpeg", "jpg" -> "JPEG"
        "png" -> "PNG"
        "gif" -> "GIF"
        "webp" -> "WebP"
        "heic" -> "HEIC"
        "heif" -> "HEIF"
        "avif" -> "AVIF"
        "bmp", "x-ms-bmp" -> "BMP"
        "mp4" -> "MP4"
        "quicktime" -> "QuickTime"
        "webm" -> "WebM"
        "x-matroska" -> "Matroska"
        "3gpp" -> "3GP"
        else -> subtype.removePrefix("x-").uppercase()
    }
    val noun = when (kind) {
        "image" -> "image"
        "video" -> "video"
        else -> "file"
    }
    return if (label.isEmpty()) noun else "$label $noun"
}
