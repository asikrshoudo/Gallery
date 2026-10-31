package com.gallery.thekami.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gallery.thekami.GalleryApplication
import com.gallery.thekami.core.data.marks.MarksRepository
import com.gallery.thekami.core.domain.media.MediaMarks
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** App-level favourites and pins. Persisted in Gallery's own storage, never on the media file. */
class MarksViewModel(private val repository: MarksRepository) : ViewModel() {
    val marks: StateFlow<MediaMarks> =
        repository.marks.stateIn(viewModelScope, SharingStarted.Eagerly, MediaMarks())

    fun toggleFavourite(key: String) {
        viewModelScope.launch { repository.toggleFavourite(key) }
    }

    fun togglePinned(key: String) {
        viewModelScope.launch { repository.togglePinned(key) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GalleryApplication
                MarksViewModel(app.marksRepository)
            }
        }
    }
}
