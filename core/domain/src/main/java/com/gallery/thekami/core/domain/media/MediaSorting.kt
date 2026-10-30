package com.gallery.thekami.core.domain.media

enum class MediaSortOrder { NewestFirst, OldestFirst, NameAscending, NameDescending, LargestFirst }

private fun <T : Comparable<T>> compareNullsLast(a: T?, b: T?, descending: Boolean): Int = when {
    a == null && b == null -> 0
    a == null -> 1
    b == null -> -1
    descending -> b.compareTo(a)
    else -> a.compareTo(b)
}

/** Returns a new list. Ties are broken by id then uri so ordering is deterministic. */
fun sortMedia(
    items: Collection<MediaItem>,
    order: MediaSortOrder = MediaSortOrder.NewestFirst,
): List<MediaItem> {
    val primary: Comparator<MediaItem> = when (order) {
        MediaSortOrder.NewestFirst ->
            Comparator { a, b -> compareNullsLast(a.effectiveDateMillis, b.effectiveDateMillis, true) }
        MediaSortOrder.OldestFirst ->
            Comparator { a, b -> compareNullsLast(a.effectiveDateMillis, b.effectiveDateMillis, false) }
        MediaSortOrder.NameAscending ->
            Comparator { a, b -> a.displayName.compareTo(b.displayName, ignoreCase = true) }
        MediaSortOrder.NameDescending ->
            Comparator { a, b -> b.displayName.compareTo(a.displayName, ignoreCase = true) }
        MediaSortOrder.LargestFirst ->
            Comparator { a, b -> compareNullsLast(a.sizeBytes, b.sizeBytes, true) }
    }
    return items.sortedWith(
        primary.thenComparator { a, b -> b.id.compareTo(a.id) }.thenBy { it.uri },
    )
}
