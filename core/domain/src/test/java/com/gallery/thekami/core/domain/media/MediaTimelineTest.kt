package com.gallery.thekami.core.domain.media

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaTimelineTest {
    private val utc = ZoneId.of("UTC")
    private fun at(iso: String) = Instant.parse(iso).toEpochMilli()

    private val items = listOf(
        testMedia(1, taken = at("2024-03-10T10:00:00Z")),
        testMedia(2, taken = at("2024-03-10T08:00:00Z")),
        testMedia(3, taken = at("2024-03-02T08:00:00Z")),
    )

    @Test
    fun headerThenItsMedia() {
        val timeline = buildTimeline(items, MediaGrouping.Day, utc)
        assertEquals(5, timeline.size)
        val first = timeline[0] as TimelineEntry.Header
        assertEquals(LocalDate.of(2024, 3, 10), first.start)
        assertEquals(2, first.count)
        assertEquals(listOf(1L, 2L), timeline.drop(1).take(2).map { (it as TimelineEntry.Media).item.id })
        assertTrue(timeline[3] is TimelineEntry.Header)
    }

    @Test
    fun monthGroupingMergesDays() {
        val timeline = buildTimeline(items, MediaGrouping.Month, utc)
        assertEquals(1, timeline.count { it is TimelineEntry.Header })
        assertEquals(3, (timeline[0] as TimelineEntry.Header).count)
    }

    @Test
    fun emptyInputGivesEmptyTimeline() {
        assertTrue(buildTimeline(emptyList(), MediaGrouping.Day, utc).isEmpty())
    }

    @Test
    fun keysAreUniqueEvenWithDuplicateInput() {
        val timeline = buildTimeline(items + items, MediaGrouping.Day, utc)
        assertEquals(timeline.size, timeline.map { it.key }.toSet().size)
    }
}
