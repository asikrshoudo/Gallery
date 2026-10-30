package com.gallery.thekami.core.domain.media

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaSourceTest {
    private fun source(path: String?) = classifySource(testMedia(1, path = path))

    @Test
    fun dcimIsCamera() {
        assertEquals(MediaSource.Camera, source("DCIM/Camera/"))
        assertEquals(MediaSource.Camera, source("dcim/OpenCamera/"))
    }

    @Test
    fun screenshotFoldersWinOverDcim() {
        assertEquals(MediaSource.Screenshots, source("Pictures/Screenshots/"))
        assertEquals(MediaSource.Screenshots, source("DCIM/Screenshots/"))
    }

    @Test
    fun everythingElseIsOtherApps() {
        assertEquals(MediaSource.OtherApps, source("Pictures/WhatsApp/"))
        assertEquals(MediaSource.OtherApps, source("Download/"))
        assertEquals(MediaSource.OtherApps, source("Movies/"))
        assertEquals(MediaSource.OtherApps, source(null))
    }

    @Test
    fun fileNameDoesNotMatter() {
        assertEquals(MediaSource.OtherApps, classifySource(testMedia(1, name = "Screenshot_1.png", path = "Pictures/Trips/")))
    }
}
