package com.gallery.thekami.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gallery.thekami.core.data.media.MediaStoreMediaRepository
import com.gallery.thekami.core.domain.media.MediaAccess
import com.gallery.thekami.core.domain.media.MediaLibraryState
import com.gallery.thekami.core.domain.media.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/**
 * Owns the library subscription. Collection stops about 5 seconds after the UI goes to the
 * background (observer unregistered, loads cancelled) and resumes without a rescan unless
 * MediaStore or the permission state actually changed.
 */
class MediaViewModel(private val repository: MediaRepository) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val state: StateFlow<MediaLibraryState> = repository.libraryState()
        .onEach { _isRefreshing.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MediaLibraryState.Loading)

    fun refresh() {
        _isRefreshing.value = true
        repository.refresh()
    }

    fun onPermissionResult() = repository.refresh()

    fun onResume() {
        // With partial access the user's selection can change without a MediaStore event.
        if (repository.currentAccess() == MediaAccess.Partial) repository.refresh() else repository.checkForChanges()
    }

    fun permissionsToRequest(): List<String> = repository.permissionsToRequest()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                MediaViewModel(MediaStoreMediaRepository(app))
            }
        }
    }
}
