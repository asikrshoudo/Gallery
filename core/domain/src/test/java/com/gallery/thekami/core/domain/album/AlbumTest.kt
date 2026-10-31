package com.gallery.thekami.core.domain.album

import com.gallery.thekami.core.domain.media.testMedia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlbumTest {
    @Test
    fun nameIsTrimmedAndWhitespaceCollapsed() {
        assertEquals("My trip", normalizeAlbumName("  My   trip "))
    }

    @Test
    fun blankNameIsRejected() {
        assertNull(normalizeAlbumName("   "))
        assertNull(normalizeAlbumName(""))
    }

    @Test
    fun longNameIsCut() {
        assertEquals(MAX_ALBUM_NAME_LENGTH, normalizeAlbumName("a".repeat(200))?.length)
    }

    private val old = testMedia(1, taken = 100)
    private val newer = testMedia(2, taken = 300)
    private val mid = testMedia(3, taken = 200)
    private val library = listOf(old, newer, mid).associateBy { it.key }

    private fun album(keys: Set<String>, cover: String? = null) =
        Album(id = "a", name = "Trip", createdAtMillis = 0, coverKey = cover, mediaKeys = keys)

    @Test
    fun itemsAreSortedNewestFirst() {
        val view = resolveAlbum(album(setOf(old.key, newer.key, mid.key)), library)
        assertEquals(listOf(2L, 3L, 1L), view.items.map { it.id })
        assertEquals(0, view.missingCount)
    }

    @Test
    fun missingKeysAreCountedNotShown() {
        val view = resolveAlbum(album(setOf(old.key, "content://gone/9")), library)
        assertEquals(listOf(1L), view.items.map { it.id })
        assertEquals(1, view.missingCount)
    }

    @Test
    fun chosenCoverIsUsedWhenItIsInTheAlbum() {
        val view = resolveAlbum(album(setOf(old.key, newer.key), cover = old.key), library)
        assertEquals(old, view.cover)
    }

    @Test
    fun coverFallsBackToNewestWhenNotSetOrNotInAlbum() {
        assertEquals(newer, resolveAlbum(album(setOf(old.key, newer.key)), library).cover)
        assertEquals(newer, resolveAlbum(album(setOf(old.key, newer.key), cover = mid.key), library).cover)
    }

    @Test
    fun emptyAlbumHasNoCover() {
        assertNull(resolveAlbum(album(emptySet()), library).cover)
    }
}
