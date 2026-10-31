package com.gallery.thekami.core.domain.search

import com.gallery.thekami.core.domain.media.testMedia
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTest {
    private val utc = ZoneId.of("UTC")
    private val today = LocalDate.of(2024, 6, 1)

    private fun at(iso: String) = Instant.parse(iso).toEpochMilli()
    private fun q(text: String, locale: Locale = Locale.ENGLISH) = parseSearchQuery(text, today, locale)

    private val scan = testMedia(3, name = "scan.jpg", taken = at("2024-03-20T10:00:00Z"), path = "Download/").copy(bucketName = "Download")
    private val trip = testMedia(1, name = "Beach_Trip.JPG", taken = at("2024-03-15T10:00:00Z"), path = "DCIM/Camera/").copy(bucketName = "Camera")
    private val shot = testMedia(2, name = "Screenshot_1.png", taken = at("2023-12-31T10:00:00Z"), path = "Pictures/Screenshots/").copy(bucketName = "Screenshots")
    private val undated = testMedia(4, name = "mystery.jpg", path = "Download/").copy(bucketName = "Download")
    private val library = listOf(scan, trip, shot, undated)
    private val engine = SearchEngine(library, listOf(MetadataSearchProvider(library, utc)))

    private fun ids(text: String) = engine.search(q(text)).map { it.id }

    @Test
    fun fileNameMatchIgnoresCase() {
        assertEquals(listOf(1L), ids("beach"))
        assertEquals(listOf(1L), ids("BEACH_trip"))
    }

    @Test
    fun folderNameAndPathMatch() {
        assertEquals(listOf(2L), ids("screenshots"))
        assertEquals(listOf(3L, 4L), ids("download"))
    }

    @Test
    fun yearMatchesItemsFromThatYear() {
        assertEquals(listOf(3L, 1L), ids("2024"))
    }

    @Test
    fun monthNameAndMonthWithYear() {
        assertEquals(listOf(3L, 1L), ids("march 2024"))
        assertEquals(listOf(3L, 1L), ids("mar"))
        assertEquals(listOf(2L), ids("december"))
    }

    @Test
    fun isoDayAndIsoMonth() {
        assertEquals(listOf(1L), ids("2024-03-15"))
        assertEquals(listOf(3L, 1L), ids("2024-03"))
    }

    @Test
    fun dayNumberNeedsAMonthAndOnlyMatchesByDate() {
        assertEquals(listOf(1L), ids("15 march"))
        assertNull(q("15").terms.single().date)
    }

    @Test
    fun everyTermMustMatch() {
        assertEquals(listOf(1L), ids("beach trip"))
        assertTrue(ids("beach scan").isEmpty())
    }

    @Test
    fun undatedItemsNeverMatchDateTerms() {
        assertTrue(4L !in ids("2024"))
        assertTrue(4L !in ids("march"))
    }

    @Test
    fun blankAndNoMatchGiveNothing() {
        assertTrue(ids("").isEmpty())
        assertTrue(ids("   ").isEmpty())
        assertTrue(ids("zzzz").isEmpty())
    }

    @Test
    fun todayAndYesterdayResolveAgainstToday() {
        assertEquals(DateTerm.Day(today), q("today").terms.single().date)
        assertEquals(DateTerm.Day(today.minusDays(1)), q("yesterday").terms.single().date)
    }

    @Test
    fun monthNamesOfTheDeviceLocaleAreUnderstood() {
        assertEquals(DateTerm.Month(3), q("mars", Locale.FRENCH).terms.single().date)
    }

    @Test
    fun resultsKeepLibraryOrderAcrossProviders() {
        val extra = object : MediaSearchProvider {
            override val id = "fake"
            override fun search(query: SearchQuery) = setOf(undated.key)
        }
        val both = SearchEngine(library, listOf(MetadataSearchProvider(library, utc), extra))
        assertEquals(listOf(3L, 1L, 4L), both.search(q("2024")).map { it.id })
    }
}
