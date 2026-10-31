package com.gallery.thekami.core.domain.media

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

/**
 * One library subscription shared by every screen, so Home and Collections never each run
 * their own MediaStore scan. Collection stops about 5 seconds after the last screen leaves.
 * It is a SharedFlow, so a refresh that finds identical data still emits.
 */
class SharedMediaLibrary(repository: MediaRepository, scope: CoroutineScope) {
    val state: SharedFlow<MediaLibraryState> =
        repository.libraryState().shareIn(scope, SharingStarted.WhileSubscribed(5_000), replay = 1)
}
