package com.gallery.thekami.core.domain.media

/**
 * App-level favourite and pinned state, stored as media keys inside Gallery only.
 * Neither changes the file, its permissions or its location.
 */
data class MediaMarks(
    val favourites: Set<String> = emptySet(),
    val pinned: Set<String> = emptySet(),
) {
    fun isFavourite(key: String): Boolean = key in favourites
    fun isPinned(key: String): Boolean = key in pinned
}

/** Library items whose key is in [keys], each once, in the order of [items] (newest first). */
fun selectMarked(items: List<MediaItem>, keys: Set<String>): List<MediaItem> =
    if (keys.isEmpty()) emptyList() else distinctMedia(items).filter { it.key in keys }
