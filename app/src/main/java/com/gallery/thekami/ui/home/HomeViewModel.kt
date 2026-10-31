package com.gallery.thekami.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gallery.thekami.GalleryApplication
import com.gallery.thekami.core.data.settings.SettingsRepository
import com.gallery.thekami.core.domain.media.MediaAccess
import com.gallery.thekami.core.domain.media.MediaLibraryState
import com.gallery.thekami.core.domain.media.MediaRepository
import com.gallery.thekami.core.domain.media.buildLivePhotoPairs
import com.gallery.thekami.core.domain.media.buildTimeline
import com.gallery.thekami.core.domain.media.selectHomeMedia
import com.gallery.thekami.core.domain.settings.HomeSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Combines the media library with Home settings. Changing a Home setting re-derives the
 * timeline from the already loaded library on a background thread; it does not rescan.
 * Collection stops about 5 seconds after the UI leaves the foreground.
 */
class HomeViewModel(
    private val media: MediaRepository,
    private val libraryState: Flow<MediaLibraryState>,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val homeSettingsFlow = settingsRepository.settings.map { it.home }.distinctUntilChanged()

    val homeSettings: StateFlow<HomeSettings> =
        homeSettingsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeSettings())

    val uiState: StateFlow<HomeUiState> = combine(
        libraryState.onEach { _isRefreshing.value = false },
        homeSettingsFlow,
        ::toUiState,
    )
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    fun refresh() {
        _isRefreshing.value = true
        media.refresh()
    }

    fun onPermissionResult() = media.refresh()

    fun onMediaChanged() = media.refresh()

    fun onResume() {
        // With partial access the user's selection can change without a MediaStore event.
        if (media.currentAccess() == MediaAccess.Partial) media.refresh() else media.checkForChanges()
    }

    fun permissionsToRequest(): List<String> = media.permissionsToRequest()

    fun setHomeSettings(value: HomeSettings) {
        viewModelScope.launch { settingsRepository.setHomeSettings(value) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GalleryApplication
                HomeViewModel(app.mediaRepository, app.mediaLibrary.state, app.settingsRepository)
            }
        }
    }
}

private fun toUiState(library: MediaLibraryState, home: HomeSettings): HomeUiState = when (library) {
    MediaLibraryState.Loading -> HomeUiState.Loading
    is MediaLibraryState.PermissionRequired -> HomeUiState.PermissionRequired
    is MediaLibraryState.Failed -> HomeUiState.Failed(library.message)
    is MediaLibraryState.Ready -> {
        // Hidden/private and Trash keys will be passed as excludedKeys when those systems exist.
        val shown = selectHomeMedia(library.items, home)
        HomeUiState.Ready(
            timeline = buildTimeline(shown, home.grouping),
            sequence = shown,
            livePairs = buildLivePhotoPairs(library.items),
            shownCount = shown.size,
            libraryCount = library.items.size,
            access = library.access,
        )
    }
}
