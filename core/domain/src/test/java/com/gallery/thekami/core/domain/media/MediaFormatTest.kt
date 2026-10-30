package com.gallery.thekami.core.domain.media

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaFormatTest {
    @Test
    fun formatsMinutesAndSeconds() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:07", formatDuration(7_900))
        assertEquals("12:34", formatDuration((12 * 60 + 34) * 1000L))
    }

    @Test
    fun formatsHours() {
        assertEquals("1:02:03", formatDuration((3600 + 2 * 60 + 3) * 1000L))
    }

    @Test
    fun negativeIsZero() {
        assertEquals("0:00", formatDuration(-5))
    }
}
