package com.gallery.thekami.core.domain.media

import java.time.LocalDate
import java.time.ZoneId

/** Flat, lazily renderable list: a header followed by its media. Keys are unique. */
sealed interface TimelineEntry {
    val key: String

    data class Header(
        val grouping: MediaGrouping,
        val start: LocalDate?,
        val count: Int,
    ) : TimelineEntry {
        override val key: String get() = "header:${grouping.name}:${start ?: "none"}"
    }

    data class Media(val item: MediaItem) : TimelineEntry {
        override val key: String get() = item.key
    }
}

/** [items] should already be sorted. Each item appears once. */
fun buildTimeline(
    items: List<MediaItem>,
    grouping: MediaGrouping,
    zone: ZoneId = ZoneId.systemDefault(),
): List<TimelineEntry> {
    val entries = ArrayList<TimelineEntry>(items.size + 32)
    for (group in groupMediaByDate(distinctMedia(items), grouping, zone)) {
        entries += TimelineEntry.Header(group.grouping, group.start, group.items.size)
        group.items.forEach { entries += TimelineEntry.Media(it) }
    }
    return entries
}
