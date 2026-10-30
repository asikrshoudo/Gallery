package com.gallery.thekami.core.domain.media

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class MediaGrouping { Day, Month, Year }

/** [start] is the first day of the period; null means items with no date at all. */
data class MediaGroup(
    val grouping: MediaGrouping,
    val start: LocalDate?,
    val items: List<MediaItem>,
)

/**
 * Groups by effective date in [zone]. Group order follows first appearance, so pass a sorted
 * list. Undated items form one trailing group; they are never given a made-up date.
 */
fun groupMediaByDate(
    items: List<MediaItem>,
    grouping: MediaGrouping,
    zone: ZoneId = ZoneId.systemDefault(),
): List<MediaGroup> {
    val dated = LinkedHashMap<LocalDate, MutableList<MediaItem>>()
    val undated = mutableListOf<MediaItem>()
    for (item in items) {
        val millis = item.effectiveDateMillis
        if (millis == null) {
            undated += item
            continue
        }
        val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        val start = when (grouping) {
            MediaGrouping.Day -> date
            MediaGrouping.Month -> date.withDayOfMonth(1)
            MediaGrouping.Year -> date.withDayOfYear(1)
        }
        dated.getOrPut(start) { mutableListOf() } += item
    }
    val groups = dated.map { (start, list) -> MediaGroup(grouping, start, list) }
    return if (undated.isEmpty()) groups else groups + MediaGroup(grouping, null, undated)
}
