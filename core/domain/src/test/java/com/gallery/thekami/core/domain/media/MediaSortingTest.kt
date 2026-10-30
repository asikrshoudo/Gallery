package com.gallery.thekami.core.domain.media

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaSortingTest {
    private fun ids(items: List<MediaItem>) = items.map { it.id }

    @Test
    fun newestFirstPutsUndatedLast() {
        val items = listOf(testMedia(1), testMedia(2, taken = 100), testMedia(3, taken = 300))
        assertEquals(listOf(3L, 2L, 1L), ids(sortMedia(items)))
    }

    @Test
    fun oldestFirstStillPutsUndatedLast() {
        val items = listOf(testMedia(1), testMedia(2, taken = 100), testMedia(3, taken = 300))
        assertEquals(listOf(2L, 3L, 1L), ids(sortMedia(items, MediaSortOrder.OldestFirst)))
    }

    @Test
    fun equalDatesAreOrderedByIdDescending() {
        val items = listOf(testMedia(1, taken = 5), testMedia(2, taken = 5))
        assertEquals(listOf(2L, 1L), ids(sortMedia(items)))
    }

    @Test
    fun nameSortIgnoresCase() {
        val items = listOf(testMedia(1, name = "b.jpg"), testMedia(2, name = "A.jpg"))
        assertEquals(listOf(2L, 1L), ids(sortMedia(items, MediaSortOrder.NameAscending)))
        assertEquals(listOf(1L, 2L), ids(sortMedia(items, MediaSortOrder.NameDescending)))
    }

    @Test
    fun largestFirstPutsUnknownSizeLast() {
        val items = listOf(testMedia(1), testMedia(2, size = 10), testMedia(3, size = 99))
        assertEquals(listOf(3L, 2L, 1L), ids(sortMedia(items, MediaSortOrder.LargestFirst)))
    }

    @Test
    fun sortingDoesNotChangeTheInputList() {
        val items = listOf(testMedia(1, taken = 1), testMedia(2, taken = 2))
        sortMedia(items)
        assertEquals(listOf(1L, 2L), ids(items))
    }
}
