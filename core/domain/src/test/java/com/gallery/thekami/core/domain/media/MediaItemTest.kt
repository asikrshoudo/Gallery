package com.gallery.thekami.core.domain.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaItemTest {
    @Test
    fun effectiveDatePrefersCaptureDate() {
        assertEquals(100L, testMedia(1, taken = 100, modified = 200).effectiveDateMillis)
    }

    @Test
    fun effectiveDateFallsBackToModified() {
        assertEquals(200L, testMedia(1, modified = 200).effectiveDateMillis)
    }

    @Test
    fun effectiveDateIsNullWhenNoDateExists() {
        assertNull(testMedia(1).effectiveDateMillis)
    }
}
