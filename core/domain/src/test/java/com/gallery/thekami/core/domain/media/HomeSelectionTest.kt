package com.gallery.thekami.core.domain.media

import com.gallery.thekami.core.domain.settings.HomeSettings
import com.gallery.thekami.core.domain.settings.HomeViewMode
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeSelectionTest {
    private val cameraPhoto = testMedia(1, taken = 6, path = "DCIM/Camera/")
    private val cameraVideo = testMedia(2, type = MediaType.Video, taken = 5, path = "DCIM/Camera/")
    private val screenshot = testMedia(3, taken = 4, path = "Pictures/Screenshots/")
    private val chatPhoto = testMedia(4, taken = 3, path = "Pictures/WhatsApp/")
    private val chatVideo = testMedia(5, type = MediaType.Video, taken = 2, path = "Movies/")
    private val library = listOf(cameraPhoto, cameraVideo, screenshot, chatPhoto, chatVideo)

    private fun ids(settings: HomeSettings, excluded: Set<String> = emptySet()) =
        selectHomeMedia(library, settings, excluded).map { it.id }

    private val all = HomeSettings(viewMode = HomeViewMode.AllMedia)

    @Test
    fun cameraRollShowsOnlyCameraMedia() {
        assertEquals(listOf(1L, 2L), ids(HomeSettings()))
    }

    @Test
    fun cameraRollIgnoresScreenshotAndOtherAppToggles() {
        assertEquals(listOf(1L, 2L), ids(HomeSettings(includeScreenshots = true, includeOtherApps = true)))
    }

    @Test
    fun allMediaIncludesEverythingByDefaultInOriginalOrder() {
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), ids(all))
    }

    @Test
    fun allMediaCanExcludeScreenshots() {
        assertEquals(listOf(1L, 2L, 4L, 5L), ids(all.copy(includeScreenshots = false)))
    }

    @Test
    fun allMediaCanExcludeOtherApps() {
        assertEquals(listOf(1L, 2L, 3L), ids(all.copy(includeOtherApps = false)))
    }

    @Test
    fun photosAndVideosCanBeExcluded() {
        assertEquals(listOf(2L, 5L), ids(all.copy(includePhotos = false)))
        assertEquals(listOf(1L, 3L, 4L), ids(all.copy(includeVideos = false)))
        assertEquals(emptyList<Long>(), ids(all.copy(includePhotos = false, includeVideos = false)))
    }

    @Test
    fun excludedKeysAreRemoved() {
        assertEquals(listOf(2L, 3L, 4L, 5L), ids(all, setOf(cameraPhoto.key)))
    }

    @Test
    fun filteringHomeNeverChangesTheLibrary() {
        selectHomeMedia(library, HomeSettings(includePhotos = false))
        assertEquals(5, library.size)
    }

    @Test
    fun duplicatesAreCollapsed() {
        val result = selectHomeMedia(library + cameraPhoto.copy(), all)
        assertEquals(5, result.size)
    }
}
