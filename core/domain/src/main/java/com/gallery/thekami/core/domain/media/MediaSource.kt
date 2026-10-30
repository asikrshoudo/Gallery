package com.gallery.thekami.core.domain.media

enum class MediaSource { Camera, Screenshots, OtherApps }

/**
 * Classifies by the folder MediaStore reports (relative path), not by file name.
 * - A "Screenshots" folder anywhere in the path (Pictures/Screenshots, DCIM/Screenshots).
 * - Otherwise anything under DCIM is treated as camera media.
 * - Everything else (Pictures/*, Download, Movies, chat apps...) is "other apps".
 * Limits: this is a folder convention, not proof of origin. A camera app that saves outside
 * DCIM is classified as other apps, and screen recordings are not treated as screenshots.
 */
fun classifySource(item: MediaItem): MediaSource {
    val segments = item.relativePath.orEmpty().split('/').filter { it.isNotEmpty() }
    if (segments.any { it.equals("Screenshots", ignoreCase = true) || it.equals("Screenshot", ignoreCase = true) }) {
        return MediaSource.Screenshots
    }
    if (segments.firstOrNull()?.equals("DCIM", ignoreCase = true) == true) return MediaSource.Camera
    return MediaSource.OtherApps
}
