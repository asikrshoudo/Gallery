package com.gallery.thekami.core.domain.media

import com.gallery.thekami.core.domain.media.MediaPermissionPolicy.READ_EXTERNAL_STORAGE
import com.gallery.thekami.core.domain.media.MediaPermissionPolicy.READ_MEDIA_IMAGES
import com.gallery.thekami.core.domain.media.MediaPermissionPolicy.READ_MEDIA_VIDEO
import com.gallery.thekami.core.domain.media.MediaPermissionPolicy.READ_MEDIA_VISUAL_USER_SELECTED
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaPermissionPolicyTest {
    @Test
    fun requestsMatchTheAndroidVersion() {
        assertEquals(listOf(READ_EXTERNAL_STORAGE), MediaPermissionPolicy.permissionsToRequest(32))
        assertEquals(listOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO), MediaPermissionPolicy.permissionsToRequest(33))
        assertEquals(
            listOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED),
            MediaPermissionPolicy.permissionsToRequest(34),
        )
    }

    @Test
    fun legacyStorageIsFullOrDenied() {
        assertEquals(MediaAccess.Full, MediaPermissionPolicy.resolveAccess(30, setOf(READ_EXTERNAL_STORAGE)))
        assertEquals(MediaAccess.Denied, MediaPermissionPolicy.resolveAccess(30, emptySet()))
    }

    @Test
    fun android13NeedsBothTypesForFullAccess() {
        assertEquals(MediaAccess.Full, MediaPermissionPolicy.resolveAccess(33, setOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)))
        assertEquals(MediaAccess.Partial, MediaPermissionPolicy.resolveAccess(33, setOf(READ_MEDIA_IMAGES)))
        assertEquals(MediaAccess.Denied, MediaPermissionPolicy.resolveAccess(33, emptySet()))
    }

    @Test
    fun android14UserSelectedIsPartial() {
        assertEquals(MediaAccess.Partial, MediaPermissionPolicy.resolveAccess(34, setOf(READ_MEDIA_VISUAL_USER_SELECTED)))
        assertEquals(MediaAccess.Denied, MediaPermissionPolicy.resolveAccess(34, emptySet()))
        assertEquals(
            MediaAccess.Full,
            MediaPermissionPolicy.resolveAccess(
                34,
                setOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED),
            ),
        )
    }

    @Test
    fun userSelectedAloneDoesNotCountBeforeAndroid14() {
        assertEquals(MediaAccess.Denied, MediaPermissionPolicy.resolveAccess(33, setOf(READ_MEDIA_VISUAL_USER_SELECTED)))
    }
}
