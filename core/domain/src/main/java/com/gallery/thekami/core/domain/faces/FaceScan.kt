package com.gallery.thekami.core.domain.faces

import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.MediaType

/** Stored when an image could not be read or decoded, so it is not retried every time. */
const val FACE_SCAN_FAILED = -1

private val SCANNABLE_MIMES = setOf("image/jpeg", "image/png", "image/webp", "image/heic", "image/heif")

fun isScannable(item: MediaItem): Boolean =
    item.type == MediaType.Image && item.mimeType.lowercase() in SCANNABLE_MIMES

data class ScanProgress(val scanned: Int, val total: Int)

/** Face DETECTION progress only. Nothing here recognises or groups people. */
data class FaceScanState(
    val enabled: Boolean = false,
    val supported: Boolean = true,
    val scanned: Int = 0,
    val total: Int = 0,
    val running: Boolean = false,
    val pausedForBattery: Boolean = false,
)

fun scanProgress(items: List<MediaItem>, counts: Map<String, Int>): ScanProgress {
    var total = 0
    var scanned = 0
    for (item in items) {
        if (!isScannable(item)) continue
        total++
        if (item.key in counts) scanned++
    }
    return ScanProgress(scanned, total)
}

/** Next images to check, in library order (newest first), skipping ones already checked. */
fun pendingForScan(items: List<MediaItem>, scanned: Set<String>, limit: Int): List<MediaItem> =
    items.asSequence().filter { isScannable(it) && it.key !in scanned }.take(limit).toList()

fun itemsWithFaces(items: List<MediaItem>, counts: Map<String, Int>): List<MediaItem> =
    items.filter { (counts[it.key] ?: 0) > 0 }
