package com.gallery.thekami.core.domain.faces

import com.gallery.thekami.core.domain.media.MediaType
import com.gallery.thekami.core.domain.media.testMedia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceScanTest {
    private val a = testMedia(1, taken = 5)
    private val b = testMedia(2, taken = 4)
    private val c = testMedia(3, taken = 3)
    private val clip = testMedia(4, type = MediaType.Video, taken = 2)
    private val gif = testMedia(5, taken = 1).copy(mimeType = "image/gif")
    private val library = listOf(a, b, c, clip, gif)

    @Test
    fun onlyReadableStillImagesAreScannable() {
        assertTrue(isScannable(a))
        assertFalse(isScannable(clip))
        assertFalse(isScannable(gif))
    }

    @Test
    fun pendingSkipsScannedAndKeepsNewestFirstWithinLimit() {
        assertEquals(listOf(2L, 3L), pendingForScan(library, setOf(a.key), 10).map { it.id })
        assertEquals(listOf(1L), pendingForScan(library, emptySet(), 1).map { it.id })
    }

    @Test
    fun nothingPendingWhenEverythingIsScanned() {
        assertTrue(pendingForScan(library, setOf(a.key, b.key, c.key), 10).isEmpty())
    }

    @Test
    fun progressCountsOnlyScannableImages() {
        val progress = scanProgress(library, mapOf(a.key to 2, b.key to 0, clip.key to 1))
        assertEquals(ScanProgress(scanned = 2, total = 3), progress)
    }

    @Test
    fun facesFilterIgnoresZeroAndFailedScans() {
        val counts = mapOf(a.key to 2, b.key to 0, c.key to FACE_SCAN_FAILED)
        assertEquals(listOf(1L), itemsWithFaces(library, counts).map { it.id })
    }
}
