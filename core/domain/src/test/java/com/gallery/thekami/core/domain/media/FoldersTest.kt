package com.gallery.thekami.core.domain.media

import org.junit.Assert.assertEquals
import org.junit.Test

class FoldersTest {
    private val cam1 = testMedia(1, taken = 9, path = "DCIM/Camera/").copy(bucketName = "Camera")
    private val cam2 = testMedia(2, taken = 8, path = "DCIM/Camera/").copy(bucketName = "Camera")
    private val shot = testMedia(3, taken = 7, path = "Pictures/Screenshots/").copy(bucketName = "Screenshots")
    private val chat = testMedia(4, taken = 6, path = "Pictures/WhatsApp/").copy(bucketName = "WhatsApp")
    private val download = testMedia(5, taken = 5, path = "Download/").copy(bucketName = "Download")
    private val misc = testMedia(6, taken = 4, bucketId = 77).copy(bucketName = "Misc")
    private val library = listOf(cam1, cam2, shot, chat, download, misc)

    @Test
    fun groupsByFolderWithCountAndNewestCover() {
        val folders = groupFolders(library)
        assertEquals(5, folders.size)
        val camera = folders.first { it.name == "Camera" }
        assertEquals(2, camera.itemCount)
        assertEquals(cam1, camera.cover)
    }

    @Test
    fun folderKeyFallsBackToBucketThenUnknown() {
        assertEquals("dcim/camera/", folderKey(cam1))
        assertEquals("bucket:77", folderKey(misc))
        assertEquals("unknown", folderKey(testMedia(9)))
    }

    @Test
    fun nameFallsBackToLastPathSegment() {
        val item = testMedia(1, path = "Pictures/Trips/")
        assertEquals("Trips", groupFolders(listOf(item)).single().name)
    }

    @Test
    fun cameraAndScreenshotsAreFeaturedEverythingElseIsOther() {
        val overview = splitFolders(groupFolders(library))
        assertEquals(listOf("Camera", "Screenshots"), overview.featured.map { it.name })
        assertEquals(listOf("Download", "Misc", "WhatsApp"), overview.others.map { it.name })
        assertEquals(5, overview.totalCount)
    }

    @Test
    fun noFolderAppearsInBothLists() {
        val overview = splitFolders(groupFolders(library))
        val ids = (overview.featured + overview.others).map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun folderItemsReturnsOnlyThatFolderInOrder() {
        assertEquals(listOf(1L, 2L), folderItems(library, "dcim/camera/").map { it.id })
        assertEquals(emptyList<MediaItem>(), folderItems(library, "nope/"))
    }
}
