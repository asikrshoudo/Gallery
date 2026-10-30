package com.gallery.thekami.core.domain.media

enum class MediaType { Image, Video }

/** Coordinates taken from MediaStore only when it provides them. Never guessed. */
data class MediaLocation(val latitude: Double, val longitude: Double)

/**
 * One photo or video known to MediaStore. Missing metadata is null, never invented.
 * [uri] is the stable identity ([key]); it is a content:// URI string.
 */
data class MediaItem(
    val id: Long,
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val type: MediaType,
    val width: Int? = null,
    val height: Int? = null,
    val sizeBytes: Long? = null,
    val dateTakenMillis: Long? = null,
    val dateModifiedMillis: Long? = null,
    val dateAddedMillis: Long? = null,
    val durationMillis: Long? = null,
    val orientationDegrees: Int = 0,
    val location: MediaLocation? = null,
    val bucketId: Long? = null,
    val bucketName: String? = null,
    /** Folder relative to the storage volume root, e.g. "DCIM/Camera/". */
    val relativePath: String? = null,
) {
    val key: String get() = uri
    val isVideo: Boolean get() = type == MediaType.Video

    /** Capture date when known, otherwise last-modified. Null when neither exists. */
    val effectiveDateMillis: Long? get() = dateTakenMillis ?: dateModifiedMillis
}
