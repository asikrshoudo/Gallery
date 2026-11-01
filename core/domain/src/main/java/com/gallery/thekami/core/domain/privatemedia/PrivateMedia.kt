package com.gallery.thekami.core.domain.privatemedia

import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.MediaType
import com.gallery.thekami.core.domain.trash.hiddenTrashExpiresAt
import com.gallery.thekami.core.domain.trash.isExpired

/**
 * A file stored in Gallery's private storage. [trashedAtMillis] != null means it is in Hidden
 * Trash. Private entries never come from MediaStore, so they never reach Home, search,
 * albums, folders or widgets.
 */
data class PrivateEntry(
    val id: String,
    val fileName: String,
    val displayName: String,
    val mimeType: String,
    val type: MediaType,
    val width: Int?,
    val height: Int?,
    val sizeBytes: Long,
    val dateTakenMillis: Long?,
    val dateModifiedMillis: Long?,
    val durationMillis: Long?,
    val orientationDegrees: Int,
    val originalRelativePath: String?,
    val importedAtMillis: Long,
    val trashedAtMillis: Long?,
)

fun activePrivate(entries: List<PrivateEntry>): List<PrivateEntry> = entries.filter { it.trashedAtMillis == null }

fun hiddenTrashed(entries: List<PrivateEntry>): List<PrivateEntry> = entries.filter { it.trashedAtMillis != null }

fun PrivateEntry.hiddenTrashExpiresAt(): Long? = trashedAtMillis?.let { hiddenTrashExpiresAt(it) }

/** Items past the Hidden Trash retention. They are only listed; deleting them needs the user's say-so. */
fun expiredHidden(entries: List<PrivateEntry>, nowMillis: Long): List<PrivateEntry> =
    hiddenTrashed(entries).filter { isExpired(it.hiddenTrashExpiresAt(), nowMillis) }

/** [uri] is where the file lives (a file:// URI in app storage). */
fun PrivateEntry.toMediaItem(uri: String): MediaItem = MediaItem(
    id = uri.hashCode().toLong() and 0x7fffffffL,
    uri = uri,
    displayName = displayName,
    mimeType = mimeType,
    type = type,
    width = width,
    height = height,
    sizeBytes = sizeBytes,
    dateTakenMillis = dateTakenMillis,
    dateModifiedMillis = dateModifiedMillis,
    durationMillis = durationMillis,
    orientationDegrees = orientationDegrees,
)
