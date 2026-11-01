package com.gallery.thekami.core.domain.media

import org.junit.Assert.assertEquals
import org.junit.Test

class RestorePathTest {
    @Test
    fun allowedOriginalFolderIsKept() {
        assertEquals("DCIM/Camera/", restoreRelativePath(MediaType.Image, "DCIM/Camera/"))
        assertEquals("Pictures/Trips/2024/", restoreRelativePath(MediaType.Image, "Pictures/Trips/2024"))
        assertEquals("Movies/Clips/", restoreRelativePath(MediaType.Video, "Movies/Clips/"))
        assertEquals("DCIM/Camera/", restoreRelativePath(MediaType.Video, "/DCIM/Camera/"))
    }

    @Test
    fun folderNotAllowedForTheTypeFallsBackToGalleryFolder() {
        assertEquals("Pictures/Gallery/", restoreRelativePath(MediaType.Image, "Download/"))
        assertEquals("Movies/Gallery/", restoreRelativePath(MediaType.Video, "Pictures/Trips/"))
        assertEquals("Pictures/Gallery/", restoreRelativePath(MediaType.Image, "Movies/"))
    }

    @Test
    fun missingOriginalFallsBack() {
        assertEquals("Pictures/Gallery/", restoreRelativePath(MediaType.Image, null))
        assertEquals("Movies/Gallery/", restoreRelativePath(MediaType.Video, ""))
    }

    @Test
    fun dotSegmentsAreDropped() {
        assertEquals("Pictures/Trips/", restoreRelativePath(MediaType.Image, "Pictures/../Trips/./"))
    }
}
