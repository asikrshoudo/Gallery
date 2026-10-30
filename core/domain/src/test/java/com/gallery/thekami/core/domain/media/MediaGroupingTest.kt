package com.gallery.thekami.core.domain.media

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaGroupingTest {
    private val utc = ZoneId.of("UTC")

    private fun at(iso: String) = Instant.parse(iso).toEpochMilli()

    private val items = listOf(
        testMedia(1, taken = at("2024-03-10T10:00:00Z")),
        testMedia(2, taken = at("2024-03-10T08:00:00Z")),
        testMedia(3, taken = at("2024-03-02T08:00:00Z")),
        testMedia(4, taken = at("2023-12-31T08:00:00Z")),
        testMedia(5),
    )

    @Test
    fun groupsByDay() {
        val groups = groupMediaByDate(items, MediaGrouping.Day, utc)
        assertEquals(
            listOf(LocalDate.of(2024, 3, 10), LocalDate.of(2024, 3, 2), LocalDate.of(2023, 12, 31), null),
            groups.map { it.start },
        )
        assertEquals(listOf(1L, 2L), groups[0].items.map { it.id })
    }

    @Test
    fun groupsByMonth() {
        val groups = groupMediaByDate(items, MediaGrouping.Month, utc)
        assertEquals(LocalDate.of(2024, 3, 1), groups[0].start)
        assertEquals(3, groups[0].items.size)
        assertEquals(LocalDate.of(2023, 12, 1), groups[1].start)
    }

    @Test
    fun groupsByYear() {
        val groups = groupMediaByDate(items, MediaGrouping.Year, utc)
        assertEquals(listOf(LocalDate.of(2024, 1, 1), LocalDate.of(2023, 1, 1), null), groups.map { it.start })
    }

    @Test
    fun undatedItemsFormOneTrailingGroupWithoutADate() {
        val last = groupMediaByDate(items, MediaGrouping.Day, utc).last()
        assertNull(last.start)
        assertEquals(listOf(5L), last.items.map { it.id })
    }

    @Test
    fun timeZoneDecidesTheDay() {
        val item = listOf(testMedia(1, taken = at("2024-01-01T00:30:00Z")))
        val inUtc = groupMediaByDate(item, MediaGrouping.Day, utc).single().start
        val inNewYork = groupMediaByDate(item, MediaGrouping.Day, ZoneId.of("America/New_York")).single().start
        assertEquals(LocalDate.of(2024, 1, 1), inUtc)
        assertEquals(LocalDate.of(2023, 12, 31), inNewYork)
    }
}
