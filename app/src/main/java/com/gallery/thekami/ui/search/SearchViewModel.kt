@file:OptIn(FlowPreview::class)

package com.gallery.thekami.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gallery.thekami.GalleryApplication
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.MediaLibraryState
import com.gallery.thekami.core.domain.media.buildLivePhotoPairs
import com.gallery.thekami.core.domain.search.MetadataSearchProvider
import com.gallery.thekami.core.domain.search.SearchEngine
import com.gallery.thekami.core.domain.search.parseSearchQuery
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val MAX_QUERY_LENGTH = 100

sealed interface SearchUiState {
    data object Loading : SearchUiState
    data object PermissionRequired : SearchUiState
    data class Failed(val message: String?) : SearchUiState
    data class Idle(val total: Int) : SearchUiState

    /** [items] is the exact sequence the viewer opens from a result. */
    data class Results(
        val query: String,
        val items: List<MediaItem>,
        val livePairs: Map<String, MediaItem>,
    ) : SearchUiState
}

private sealed interface IndexState {
    data object Loading : IndexState
    data object PermissionRequired : IndexState
    data class Failed(val message: String?) : IndexState
    data class Ready(val engine: SearchEngine, val total: Int, val livePairs: Map<String, MediaItem>) : IndexState
}

/**
 * Searches the shared library on a background thread, debounced while typing. The query lives
 * here, so it survives rotation, opening the viewer from a result, and closing and reopening
 * Search. It is not saved across process death.
 */
class SearchViewModel(library: Flow<MediaLibraryState>) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun setQuery(value: String) {
        _query.value = value.take(MAX_QUERY_LENGTH)
    }

    private val index: Flow<IndexState> = library.map(::toIndexState).flowOn(Dispatchers.Default)

    val state: StateFlow<SearchUiState> =
        combine(index, _query.debounce(120)) { idx, text -> toUiState(idx, text) }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState.Loading)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GalleryApplication
                SearchViewModel(app.mediaLibrary.state)
            }
        }
    }
}

private fun toIndexState(library: MediaLibraryState): IndexState = when (library) {
    MediaLibraryState.Loading -> IndexState.Loading
    is MediaLibraryState.PermissionRequired -> IndexState.PermissionRequired
    is MediaLibraryState.Failed -> IndexState.Failed(library.message)
    is MediaLibraryState.Ready -> IndexState.Ready(
        engine = SearchEngine(library.items, listOf(MetadataSearchProvider(library.items))),
        total = library.items.size,
        livePairs = buildLivePhotoPairs(library.items),
    )
}

private fun toUiState(index: IndexState, text: String): SearchUiState = when (index) {
    IndexState.Loading -> SearchUiState.Loading
    IndexState.PermissionRequired -> SearchUiState.PermissionRequired
    is IndexState.Failed -> SearchUiState.Failed(index.message)
    is IndexState.Ready -> {
        val query = parseSearchQuery(text, LocalDate.now(), Locale.getDefault())
        if (query.isBlank) {
            SearchUiState.Idle(index.total)
        } else {
            SearchUiState.Results(text.trim(), index.engine.search(query), index.livePairs)
        }
    }
}
