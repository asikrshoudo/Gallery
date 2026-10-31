package com.gallery.thekami.core.domain.album

import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.sortMedia

const val MAX_ALBUM_NAME_LENGTH = 60

/**
 * A Gallery-owned (logical) collection. It stores only media keys, so files are never moved,
 * copied or deleted. A set of keys means a media item is in an album at most once, and the
 * same item can be in any number of albums.
 */
data class Album(
    val id: String,
    val name: String,
    val createdAtMillis: Long,
    val coverKey: String?,
    val mediaKeys: Set<String>,
)

/** An album resolved against the current library. [missingCount] keys are not found right now. */
data class AlbumView(
    val album: Album,
    val items: List<MediaItem>,
    val cover: MediaItem?,
    val missingCount: Int,
) {
    val id: String get() = album.id
    val name: String get() = album.name
}

/** Trims, collapses whitespace and limits length. Returns null when nothing is left. */
fun normalizeAlbumName(raw: String): String? {
    val cleaned = raw.trim().replace(Regex("\\s+"), " ").take(MAX_ALBUM_NAME_LENGTH).trim()
    return cleaned.ifEmpty { null }
}

/** Items newest first. The cover is the chosen one if it is still in the album, else the newest. */
fun resolveAlbum(album: Album, byKey: Map<String, MediaItem>): AlbumView {
    val items = sortMedia(album.mediaKeys.mapNotNull { byKey[it] })
    val chosen = album.coverKey?.takeIf { it in album.mediaKeys }?.let { byKey[it] }
    return AlbumView(album, items, chosen ?: items.firstOrNull(), album.mediaKeys.size - items.size)
}
