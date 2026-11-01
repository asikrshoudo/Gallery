package com.gallery.thekami.core.domain.privatemedia

import com.gallery.thekami.core.domain.media.MediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivateMediaTest {
    private val day = 24L * 60 * 60 * 1000

    private fun entry(id: String, trashedAt: Long? = null) = PrivateEntry(
        id = id, fileName = "$id.jpg", displayName = "IMG_$id.jpg", mimeType = "image/jpeg",
        type = MediaType.Image, width = 10, height = 20, sizeBytes = 123, dateTakenMillis = 5,
        dateModifiedMillis = 6, durationMillis = null, orientationDegrees = 0,
        originalRelativePath = "DCIM/Camera/", importedAtMillis = 1, trashedAtMillis = trashedAt,
    )

    @Test
    fun activeAndHiddenTrashAreDisjoint() {
        val all = listOf(entry("a"), entry("b", trashedAt = 1), entry("c"))
        assertEquals(listOf("a", "c"), activePrivate(all).map { it.id })
        assertEquals(listOf("b"), hiddenTrashed(all).map { it.id })
    }

    @Test
    fun hiddenTrashExpiryIsTwoDaysAfterTrashing() {
        assertEquals(1_000L + 2 * day, entry("a", trashedAt = 1_000).hiddenTrashExpiresAt())
        assertEquals(null, entry("a").hiddenTrashExpiresAt())
    }

    @Test
    fun onlyTrashedAndPastRetentionAreExpired() {
        val now = 10 * day
        val fresh = entry("fresh", trashedAt = now - day)
        val old = entry("old", trashedAt = now - 3 * day)
        val active = entry("active")
        assertEquals(listOf("old"), expiredHidden(listOf(fresh, old, active), now).map { it.id })
    }

    @Test
    fun lateCheckNeverDeletesAnything() {
        // Expired entries are only reported. Nothing in this module removes them.
        val all = listOf(entry("old", trashedAt = 0))
        assertEquals(1, expiredHidden(all, 100 * day).size)
        assertEquals(1, all.size)
    }

    @Test
    fun entryBecomesAMediaItemKeyedByItsUri() {
        val item = entry("a").toMediaItem("file:///private/a.jpg")
        assertEquals("file:///private/a.jpg", item.uri)
        assertEquals("file:///private/a.jpg", item.key)
        assertEquals("IMG_a.jpg", item.displayName)
        assertEquals(10, item.width)
        assertTrue(item.id >= 0)
    }
}
