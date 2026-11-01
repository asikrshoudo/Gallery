package com.gallery.thekami.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gallery.thekami.GalleryApplication
import com.gallery.thekami.core.data.trash.MediaStoreTrashRepository
import com.gallery.thekami.core.domain.trash.TrashedMedia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TrashUiState {
    data object Loading : TrashUiState
    data object Unsupported : TrashUiState
    data class Failed(val message: String?) : TrashUiState
    data class Ready(val items: List<TrashedMedia>, val nowMillis: Long) : TrashUiState
}

/** Loads MediaStore's Trash on demand (when the Trash screen opens and after each action). */
class TrashViewModel(private val repository: MediaStoreTrashRepository) : ViewModel() {
    private val _state = MutableStateFlow<TrashUiState>(TrashUiState.Loading)
    val state: StateFlow<TrashUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            if (!repository.supported) {
                _state.value = TrashUiState.Unsupported
                return@launch
            }
            _state.value = try {
                TrashUiState.Ready(repository.load(), System.currentTimeMillis())
            } catch (e: Exception) {
                TrashUiState.Failed(e.message)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GalleryApplication
                TrashViewModel(app.trashRepository)
            }
        }
    }
}
