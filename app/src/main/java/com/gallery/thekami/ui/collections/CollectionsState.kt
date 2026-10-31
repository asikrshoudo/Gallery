package com.gallery.thekami.ui.collections

import com.gallery.thekami.core.domain.album.AlbumView
import com.gallery.thekami.core.domain.media.FolderOverview
import com.gallery.thekami.core.domain.media.MediaAccess
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.people.PersonView

sealed interface CollectionsState {
    data object Loading : CollectionsState
    data object PermissionRequired : CollectionsState
    data class Failed(val message: String?) : CollectionsState

    /**
     * [items] is the whole library, newest first. The "Missing" counts are marked or tagged
     * items that are not in the library right now (for example moved or deleted).
     */
    data class Ready(
        val items: List<MediaItem>,
        val albums: List<AlbumView>,
        val people: List<PersonView>,
        val folders: FolderOverview,
        val livePairs: Map<String, MediaItem>,
        val access: MediaAccess,
        val favourites: List<MediaItem>,
        val favouriteMissing: Int,
        val pinned: List<MediaItem>,
        val pinnedMissing: Int,
    ) : CollectionsState
}
