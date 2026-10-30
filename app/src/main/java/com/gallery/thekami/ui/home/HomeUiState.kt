package com.gallery.thekami.ui.home

import com.gallery.thekami.core.domain.media.MediaAccess
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.TimelineEntry

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object PermissionRequired : HomeUiState
    data class Failed(val message: String?) : HomeUiState

    /**
     * [sequence] is the items Home shows, in grid order; the viewer navigates exactly this list.
     * [livePairs] maps a still image key to its paired Live Photo video.
     */
    data class Ready(
        val timeline: List<TimelineEntry>,
        val sequence: List<MediaItem>,
        val livePairs: Map<String, MediaItem>,
        val shownCount: Int,
        val libraryCount: Int,
        val access: MediaAccess,
    ) : HomeUiState
}
