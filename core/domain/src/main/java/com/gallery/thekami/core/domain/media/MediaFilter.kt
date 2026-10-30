package com.gallery.thekami.core.domain.media

/**
 * One category. Every non-null field must match (AND). A null field means "any".
 * [relativePathPrefixes] are compared case-insensitively, e.g. "DCIM/" or "Pictures/Screenshots/".
 */
data class MediaFilter(
    val types: Set<MediaType>? = null,
    val bucketIds: Set<Long>? = null,
    val relativePathPrefixes: Set<String>? = null,
) {
    fun matches(item: MediaItem): Boolean {
        if (types != null && item.type !in types) return false
        if (bucketIds != null && (item.bucketId == null || item.bucketId !in bucketIds)) return false
        if (relativePathPrefixes != null) {
            val path = item.relativePath ?: return false
            if (relativePathPrefixes.none { path.startsWith(it, ignoreCase = true) }) return false
        }
        return true
    }
}

/** Removes repeated items (same [MediaItem.key]), keeping the first occurrence and order. */
fun distinctMedia(items: Iterable<MediaItem>): List<MediaItem> = items.distinctBy { it.key }

/**
 * Items matching ANY of [filters] (no filters = everything). An item that matches several
 * filters, or appears several times in [items], is returned once.
 */
fun filterMedia(items: Iterable<MediaItem>, filters: Collection<MediaFilter> = emptyList()): List<MediaItem> {
    val unique = distinctMedia(items)
    if (filters.isEmpty()) return unique
    return unique.filter { item -> filters.any { it.matches(item) } }
}
