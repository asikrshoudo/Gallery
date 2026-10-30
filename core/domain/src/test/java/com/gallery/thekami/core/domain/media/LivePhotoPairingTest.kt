package com.gallery.thekami.core.domain.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LivePhotoPairingTest {
    private val still = testMedia(1, name = "IMG_0001.HEIC", path = "DCIM/Camera/").copy(mimeType = "image/heic")
    private val motion = testMedia(2, name = "IMG_0001.MOV", type = MediaType.Video, path = "DCIM/Camera/")
        .copy(mimeType = "video/quicktime", durationMillis = 3_000)

    @Test
    fun pairsStillWithQuickTimeVideoOfSameNameAndFolder() {
        val pairs = buildLivePhotoPairs(listOf(still, motion))
        assertEquals(motion, pairs[still.key])
        assertEquals(1, pairs.size)
    }

    @Test
    fun nameMatchIsCaseInsensitive() {
        val upper = motion.copy(displayName = "img_0001.mov")
        assertEquals(upper, buildLivePhotoPairs(listOf(still, upper))[still.key])
    }

    @Test
    fun differentFolderDoesNotPair() {
        val elsewhere = motion.copy(relativePath = "Movies/")
        assertTrue(buildLivePhotoPairs(listOf(still, elsewhere)).isEmpty())
    }

    @Test
    fun longVideoDoesNotPair() {
        assertTrue(buildLivePhotoPairs(listOf(still, motion.copy(durationMillis = 60_000))).isEmpty())
    }

    @Test
    fun nonQuickTimeVideoDoesNotPair() {
        assertTrue(buildLivePhotoPairs(listOf(still, motion.copy(mimeType = "video/mp4"))).isEmpty())
    }

    @Test
    fun missingFolderDoesNotPair() {
        assertTrue(buildLivePhotoPairs(listOf(still.copy(relativePath = null), motion)).isEmpty())
    }

    @Test
    fun plainPhotoIsNeverALivePhoto() {
        val photo = testMedia(5, name = "IMG_0005.jpg", path = "DCIM/Camera/")
        assertTrue(buildLivePhotoPairs(listOf(photo, motion)).isEmpty())
    }
}
