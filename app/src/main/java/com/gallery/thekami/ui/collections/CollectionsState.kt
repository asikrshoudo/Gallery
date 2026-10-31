package com.gallery.thekami.ui.collections

import com.gallery.thekami.core.domain.album.AlbumView
import com.gallery.thekami.core.domain.media.FolderOverview
import com.gallery.thekami.core.domain.media.MediaAccess
import com.gallery.thekami.core.domain.media.MediaItem

sealed interface CollectionsState {
    data object Loading : CollectionsState
    data object PermissionRequired : CollectionsState
    data class Failed(val message: String?) : CollectionsState

    /** [items] is the whole library, newest first. */
    data class Ready(
        val items: List<MediaItem>,
        val albums: List<AlbumView>,
        val folders: FolderOverview,
        val livePairs: Map<String, MediaItem>,
        val access: MediaAccess,
    ) : CollectionsState
}
