package com.gallery.thekami.ui.home

import com.gallery.thekami.core.domain.media.MediaGrouping
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineLabelsTest {
    private val today = LocalDate.of(2024, 6, 1)

    private fun label(grouping: MediaGrouping, start: LocalDate?) =
        timelineHeaderLabel(grouping, start, today, Locale.ENGLISH, "Today", "Yesterday", "No date")

    @Test
    fun todayAndYesterday() {
        assertEquals("Today", label(MediaGrouping.Day, today))
        assertEquals("Yesterday", label(MediaGrouping.Day, today.minusDays(1)))
    }

    @Test
    fun thisYearOmitsTheYear() {
        assertEquals("Mon, 4 Mar", label(MediaGrouping.Day, LocalDate.of(2024, 3, 4)))
    }

    @Test
    fun otherYearsShowTheYear() {
        assertEquals("4 Mar 2023", label(MediaGrouping.Day, LocalDate.of(2023, 3, 4)))
    }

    @Test
    fun monthYearAndNoDate() {
        assertEquals("March 2024", label(MediaGrouping.Month, LocalDate.of(2024, 3, 1)))
        assertEquals("2024", label(MediaGrouping.Year, LocalDate.of(2024, 1, 1)))
        assertEquals("No date", label(MediaGrouping.Day, null))
    }
}
