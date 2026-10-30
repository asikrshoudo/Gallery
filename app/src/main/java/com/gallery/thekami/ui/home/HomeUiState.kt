package com.gallery.thekami.ui.home

import com.gallery.thekami.core.domain.media.MediaAccess
import com.gallery.thekami.core.domain.media.TimelineEntry

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object PermissionRequired : HomeUiState
    data class Failed(val message: String?) : HomeUiState

    /** [libraryCount] is everything MediaStore returned, [shownCount] what Home settings keep. */
    data class Ready(
        val timeline: List<TimelineEntry>,
        val shownCount: Int,
        val libraryCount: Int,
        val access: MediaAccess,
    ) : HomeUiState
}
