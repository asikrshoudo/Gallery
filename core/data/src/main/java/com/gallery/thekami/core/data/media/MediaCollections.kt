package com.gallery.thekami.core.data.media

import android.net.Uri
import android.provider.MediaStore
import com.gallery.thekami.core.domain.media.MediaType

/** "external" is the merged view of every storage volume (internal storage and SD cards). */
internal const val EXTERNAL_VOLUME = "external"

internal fun collectionUri(type: MediaType): Uri = when (type) {
    MediaType.Image -> MediaStore.Images.Media.getContentUri(EXTERNAL_VOLUME)
    MediaType.Video -> MediaStore.Video.Media.getContentUri(EXTERNAL_VOLUME)
}
