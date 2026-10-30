package com.gallery.thekami.core.domain.media

import kotlin.math.abs

private val LIVE_IMAGE_MIMES = setOf("image/heic", "image/heif", "image/jpeg")
private const val LIVE_VIDEO_MIME = "video/quicktime"
private const val MAX_LIVE_DURATION_MS = 6_000L

private fun baseName(name: String): String = name.substringBeforeLast('.', name).lowercase()

/**
 * iPhone Live Photos exported to Android arrive as a still (HEIC/JPEG) plus a short QuickTime
 * (.MOV) file with the same base name in the same folder. Android has no metadata that links
 * them, so a pair is only reported when ALL of these hold: same folder, same base name, still
 * image type, QuickTime video no longer than a few seconds. Returns image key -> paired video.
 * Embedded Motion Photos (a video appended inside a JPEG) are NOT detected here.
 */
fun buildLivePhotoPairs(items: Collection<MediaItem>): Map<String, MediaItem> {
    val videosByName = items.filter { video ->
        val duration = video.durationMillis
        video.type == MediaType.Video &&
            video.mimeType.equals(LIVE_VIDEO_MIME, ignoreCase = true) &&
            video.relativePath != null &&
            (duration == null || duration <= MAX_LIVE_DURATION_MS)
    }.groupBy { (it.relativePath ?: "").lowercase() to baseName(it.displayName) }
    if (videosByName.isEmpty()) return emptyMap()

    val pairs = HashMap<String, MediaItem>()
    for (image in items) {
        if (image.type != MediaType.Image || image.mimeType.lowercase() !in LIVE_IMAGE_MIMES) continue
        val path = image.relativePath ?: continue
        val candidates = videosByName[path.lowercase() to baseName(image.displayName)] ?: continue
        val imageDate = image.effectiveDateMillis
        val best = candidates.minByOrNull { video ->
            val videoDate = video.effectiveDateMillis
            if (imageDate == null || videoDate == null) 0L else abs(videoDate - imageDate)
        } ?: continue
        pairs[image.key] = best
    }
    return pairs
}
