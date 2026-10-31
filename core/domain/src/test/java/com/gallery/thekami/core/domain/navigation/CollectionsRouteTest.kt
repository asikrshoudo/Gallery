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
            CollectionsRoute.AlbumDetail("3f2a-uuid"),
            CollectionsRoute.AlbumPicker("3f2a-uuid"),
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
