package com.gallery.thekami.core.domain.trash

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RetentionTest {
    private val day = 24L * 60 * 60 * 1000

    @Test
    fun hiddenTrashLastsTwoDays() {
        assertEquals(1_000L + 2 * day, hiddenTrashExpiresAt(1_000L))
    }

    @Test
    fun expiredOnlyAtOrAfterTheExpiryTime() {
        assertFalse(isExpired(1_000L, 999L))
        assertTrue(isExpired(1_000L, 1_000L))
        assertTrue(isExpired(1_000L, 5_000L))
    }

    @Test
    fun unknownExpiryIsNeverExpired() {
        assertFalse(isExpired(null, Long.MAX_VALUE))
        assertNull(remainingDays(null, 0))
    }

    @Test
    fun remainingDaysRoundsUpAndZeroMeansExpired() {
        val now = 10 * day
        assertEquals(0, remainingDays(now, now))
        assertEquals(0, remainingDays(now - 1, now))
        assertEquals(1, remainingDays(now + 1, now))
        assertEquals(1, remainingDays(now + day, now))
        assertEquals(2, remainingDays(now + day + 1, now))
        assertEquals(30, remainingDays(now + 30 * day, now))
    }
}
