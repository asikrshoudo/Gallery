package com.gallery.thekami.core.domain.media

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaFilterTest {
    private val photo = testMedia(1, bucketId = 10, path = "DCIM/Camera/")
    private val video = testMedia(2, type = MediaType.Video, bucketId = 10, path = "Movies/")
    private val shot = testMedia(3, bucketId = 20, path = "Pictures/Screenshots/")
    private val noFolder = testMedia(4)
    private val all = listOf(photo, video, shot, noFolder)

    private fun ids(items: List<MediaItem>) = items.map { it.id }

    @Test
    fun noFiltersReturnsEverything() {
        assertEquals(listOf(1L, 2L, 3L, 4L), ids(filterMedia(all)))
    }

    @Test
    fun typeFilter() {
        val result = filterMedia(all, listOf(MediaFilter(types = setOf(MediaType.Video))))
        assertEquals(listOf(2L), ids(result))
    }

    @Test
    fun bucketFilterExcludesItemsWithoutBucket() {
        val result = filterMedia(all, listOf(MediaFilter(bucketIds = setOf(10L))))
        assertEquals(listOf(1L, 2L), ids(result))
    }

    @Test
    fun pathPrefixIsCaseInsensitive() {
        val result = filterMedia(all, listOf(MediaFilter(relativePathPrefixes = setOf("pictures/"))))
        assertEquals(listOf(3L), ids(result))
    }

    @Test
    fun fieldsInsideOneFilterAreCombinedWithAnd() {
        val filter = MediaFilter(types = setOf(MediaType.Image), bucketIds = setOf(10L))
        assertEquals(listOf(1L), ids(filterMedia(all, listOf(filter))))
    }

    @Test
    fun itemMatchingSeveralFiltersAppearsOnce() {
        val filters = listOf(
            MediaFilter(types = setOf(MediaType.Image)),
            MediaFilter(bucketIds = setOf(10L)),
            MediaFilter(relativePathPrefixes = setOf("DCIM/")),
        )
        val result = filterMedia(all, filters)
        assertEquals(listOf(1L, 2L, 3L, 4L), ids(result).sorted())
        assertEquals(1, result.count { it.id == 1L })
    }

    @Test
    fun repeatedItemsAreRemoved() {
        assertEquals(listOf(1L), ids(distinctMedia(listOf(photo, photo.copy()))))
    }

    @Test
    fun sameIdInImagesAndVideoTablesAreDifferentItems() {
        val image = testMedia(7)
        val clip = testMedia(7, type = MediaType.Video)
        assertEquals(2, distinctMedia(listOf(image, clip)).size)
    }
}
