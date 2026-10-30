package com.gallery.thekami.core.domain.media

import kotlinx.coroutines.flow.Flow

sealed interface MediaLibraryState {
    data object Loading : MediaLibraryState
    data class PermissionRequired(val access: MediaAccess) : MediaLibraryState
    /** [items] are de-duplicated and sorted newest first. [skippedCount] rows were unreadable. */
    data class Ready(
        val items: List<MediaItem>,
        val access: MediaAccess,
        val skippedCount: Int = 0,
    ) : MediaLibraryState
    data class Failed(val message: String?) : MediaLibraryState
}

/** Read-only view of the device media library. Never modifies, moves or deletes media. */
interface MediaRepository {
    /** Emits when the library state changes. Cancel the collector to stop all work. */
    fun libraryState(): Flow<MediaLibraryState>

    /** Explicit refresh: always re-reads the library. */
    fun refresh()

    /** Cheap check (permission or library changed?). Re-reads only if something changed. */
    fun checkForChanges()

    fun currentAccess(): MediaAccess

    fun permissionsToRequest(): List<String>
}
