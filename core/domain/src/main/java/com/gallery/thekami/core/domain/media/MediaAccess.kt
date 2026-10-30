package com.gallery.thekami.core.domain.media

/** Full = all photos and videos; Partial = a user-selected subset or only one media type. */
enum class MediaAccess { Full, Partial, Denied }

/** Android-version-specific media permissions. Only what reading media needs is requested. */
object MediaPermissionPolicy {
    const val READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"
    const val READ_MEDIA_IMAGES = "android.permission.READ_MEDIA_IMAGES"
    const val READ_MEDIA_VIDEO = "android.permission.READ_MEDIA_VIDEO"
    const val READ_MEDIA_VISUAL_USER_SELECTED = "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"

    val allPermissions: List<String> = listOf(
        READ_EXTERNAL_STORAGE,
        READ_MEDIA_IMAGES,
        READ_MEDIA_VIDEO,
        READ_MEDIA_VISUAL_USER_SELECTED,
    )

    fun permissionsToRequest(sdk: Int): List<String> = when {
        sdk >= 34 -> listOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED)
        sdk >= 33 -> listOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)
        else -> listOf(READ_EXTERNAL_STORAGE)
    }

    fun resolveAccess(sdk: Int, granted: Set<String>): MediaAccess {
        if (sdk < 33) {
            return if (READ_EXTERNAL_STORAGE in granted) MediaAccess.Full else MediaAccess.Denied
        }
        val images = READ_MEDIA_IMAGES in granted
        val video = READ_MEDIA_VIDEO in granted
        return when {
            images && video -> MediaAccess.Full
            images || video -> MediaAccess.Partial
            sdk >= 34 && READ_MEDIA_VISUAL_USER_SELECTED in granted -> MediaAccess.Partial
            else -> MediaAccess.Denied
        }
    }
}
