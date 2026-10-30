package com.gallery.thekami.core.domain.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaPathsTest {
    @Test
    fun primaryStorageFile() {
        assertEquals("DCIM/Camera/", MediaPaths.relativeDirFromDataPath("/storage/emulated/0/DCIM/Camera/IMG_1.jpg"))
    }

    @Test
    fun sdCardFile() {
        assertEquals("Movies/Trips/", MediaPaths.relativeDirFromDataPath("/storage/1A2B-3C4D/Movies/Trips/a.mp4"))
    }

    @Test
    fun fileAtVolumeRoot() {
        assertEquals("", MediaPaths.relativeDirFromDataPath("/storage/emulated/0/a.jpg"))
    }

    @Test
    fun unknownOrMissingPath() {
        assertNull(MediaPaths.relativeDirFromDataPath("/data/local/tmp/a.jpg"))
        assertNull(MediaPaths.relativeDirFromDataPath(null))
    }
}
