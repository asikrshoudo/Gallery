package com.gallery.thekami.core.domain.media

internal fun testMedia(
    id: Long,
    name: String = "IMG_$id.jpg",
    type: MediaType = MediaType.Image,
    taken: Long? = null,
    modified: Long? = null,
    size: Long? = null,
    bucketId: Long? = null,
    path: String? = null,
    uri: String = "content://media/external/${if (type == MediaType.Image) "images" else "video"}/media/$id",
) = MediaItem(
    id = id,
    uri = uri,
    displayName = name,
    mimeType = if (type == MediaType.Image) "image/jpeg" else "video/mp4",
    type = type,
    sizeBytes = size,
    dateTakenMillis = taken,
    dateModifiedMillis = modified,
    bucketId = bucketId,
    relativePath = path,
)
