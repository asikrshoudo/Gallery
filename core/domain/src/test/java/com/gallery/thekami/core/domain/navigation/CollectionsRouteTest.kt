package com.gallery.thekami.core.domain.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CollectionsRouteTest {
    @Test
    fun everyRouteSurvivesEncodeAndDecode() {
        val routes = listOf(
            CollectionsRoute.Albums,
            CollectionsRoute.Folders,
            CollectionsRoute.OtherFolders,
            CollectionsRoute.Favourites,
            CollectionsRoute.Pinned,
            CollectionsRoute.People,
            CollectionsRoute.AlbumDetail("3f2a-uuid"),
            CollectionsRoute.AlbumPicker("3f2a-uuid"),
            CollectionsRoute.PersonDetail("p-1"),
            CollectionsRoute.PersonPicker("p-1"),
            CollectionsRoute.FolderDetail("dcim/camera/"),
            CollectionsRoute.FolderDetail("bucket:42"),
            CollectionsRoute.FolderDetail(""),
        )
        routes.forEach { assertEquals(it, CollectionsRoute.decode(it.encode())) }
    }

    @Test
    fun unknownValuesDecodeToNull() {
        assertNull(CollectionsRoute.decode("nope"))
        assertNull(CollectionsRoute.decode(""))
    }
}
