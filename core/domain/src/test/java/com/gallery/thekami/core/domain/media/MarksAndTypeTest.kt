package com.gallery.thekami.core.domain.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarksAndTypeTest {
    private val a = testMedia(1, taken = 3)
    private val b = testMedia(2, taken = 2)
    private val c = testMedia(3, taken = 1)

    @Test
    fun selectMarkedKeepsLibraryOrderAndSkipsMissing() {
        val result = selectMarked(listOf(a, b, c), setOf(c.key, a.key, "content://gone/9"))
        assertEquals(listOf(1L, 3L), result.map { it.id })
    }

    @Test
    fun noKeysMeansNoItems() {
        assertTrue(selectMarked(listOf(a, b), emptySet()).isEmpty())
    }

    @Test
    fun marksReportMembership() {
        val marks = MediaMarks(favourites = setOf(a.key), pinned = setOf(b.key))
        assertTrue(marks.isFavourite(a.key))
        assertFalse(marks.isFavourite(b.key))
        assertTrue(marks.isPinned(b.key))
    }

    @Test
    fun describesTypesFromMime() {
        assertEquals("JPEG image", describeMediaType("image/jpeg"))
        assertEquals("HEIC image", describeMediaType("image/heic"))
        assertEquals("QuickTime video", describeMediaType("video/quicktime"))
        assertEquals("MP4 video", describeMediaType("video/mp4"))
        assertEquals("file", describeMediaType("application"))
    }
}
