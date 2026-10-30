package com.gallery.thekami.ui.viewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gallery.thekami.core.domain.media.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** [items] is the exact sequence the viewer was opened from (a snapshot, not the whole library). */
data class ViewerSession(
    val items: List<MediaItem>,
    val livePairs: Map<String, MediaItem>,
)

/**
 * Holds the open viewer so rotation keeps the same item and playback position.
 * It is intentionally not restored after the process is killed: the viewer then starts closed.
 */
class ViewerViewModel(application: Application) : AndroidViewModel(application) {
    private val _session = MutableStateFlow<ViewerSession?>(null)
    val session: StateFlow<ViewerSession?> = _session.asStateFlow()

    var currentIndex: Int = 0
        private set

    val playback = PlayerController(application, viewModelScope)

    fun open(items: List<MediaItem>, startKey: String, livePairs: Map<String, MediaItem>) {
        val index = items.indexOfFirst { it.key == startKey }
        if (index < 0) return
        currentIndex = index
        _session.value = ViewerSession(items, livePairs)
    }

    fun close() {
        playback.clear()
        _session.value = null
    }

    fun onPageChanged(index: Int) {
        currentIndex = index
    }

    /** Called after Android confirmed the item was removed from the library. */
    fun removeItem(key: String) {
        val session = _session.value ?: return
        val items = session.items.filter { it.key != key }
        if (items.isEmpty()) {
            close()
        } else {
            currentIndex = currentIndex.coerceAtMost(items.lastIndex)
            _session.value = session.copy(items = items)
        }
    }

    override fun onCleared() {
        playback.release()
    }
}
