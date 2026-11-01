package com.gallery.thekami.core.domain.trash

import com.gallery.thekami.core.domain.media.MediaItem

/** Android's own MediaStore Trash keeps items about this long, then deletes them itself. */
const val NORMAL_TRASH_RETENTION_DAYS = 30

/** Gallery's Hidden Trash for private items. Expiry only makes an item ELIGIBLE for deletion. */
const val HIDDEN_TRASH_RETENTION_DAYS = 2

private const val DAY_MILLIS = 24L * 60 * 60 * 1000

/** [expiresAtMillis] is null when Android did not report an expiry. */
data class TrashedMedia(val item: MediaItem, val expiresAtMillis: Long?)

fun hiddenTrashExpiresAt(trashedAtMillis: Long): Long = trashedAtMillis + HIDDEN_TRASH_RETENTION_DAYS * DAY_MILLIS

fun isExpired(expiresAtMillis: Long?, nowMillis: Long): Boolean =
    expiresAtMillis != null && nowMillis >= expiresAtMillis

/** Whole days left, rounded up (1 = less than a day). 0 means expired. Null = unknown. */
fun remainingDays(expiresAtMillis: Long?, nowMillis: Long): Int? = when {
    expiresAtMillis == null -> null
    nowMillis >= expiresAtMillis -> 0
    else -> ((expiresAtMillis - nowMillis + DAY_MILLIS - 1) / DAY_MILLIS).toInt()
}
