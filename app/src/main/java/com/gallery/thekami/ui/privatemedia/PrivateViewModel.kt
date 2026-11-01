package com.gallery.thekami.ui.privatemedia

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gallery.thekami.GalleryApplication
import com.gallery.thekami.core.data.privatemedia.PrivateError
import com.gallery.thekami.core.data.privatemedia.PrivateResult
import com.gallery.thekami.core.data.privatemedia.PrivateStore
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.sortMedia
import com.gallery.thekami.core.domain.privatemedia.activePrivate
import com.gallery.thekami.core.domain.privatemedia.expiredHidden
import com.gallery.thekami.core.domain.privatemedia.hiddenTrashed
import com.gallery.thekami.core.domain.privatemedia.hiddenTrashExpiresAt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PrivateTrashItem(val id: String, val item: MediaItem, val expiresAtMillis: Long)

sealed interface MoveState {
    data object Idle : MoveState
    data class Copying(val name: String) : MoveState
    /** The copy is stored and verified. The original has NOT been touched. */
    data class Copied(val original: MediaItem, val canDeleteOriginal: Boolean) : MoveState
}

enum class PrivateMessage {
    CopyReadFailed, CopyNoSpace, CopyVerifyFailed, ExportDone, ExportUnsupported, ExportFailed, OriginalKept,
}

/**
 * The private area. "Unlocked" lives only in memory: it resets whenever the app leaves the
 * foreground, and a new process always starts locked.
 */
class PrivateViewModel(private val store: PrivateStore) : ViewModel() {
    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    private val _moveState = MutableStateFlow<MoveState>(MoveState.Idle)
    val moveState: StateFlow<MoveState> = _moveState.asStateFlow()

    private val _message = MutableStateFlow<PrivateMessage?>(null)
    val message: StateFlow<PrivateMessage?> = _message.asStateFlow()

    val active: StateFlow<List<MediaItem>> = store.entries
        .map { entries -> sortMedia(activePrivate(entries).map(store::toMediaItem)) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val trashed: StateFlow<List<PrivateTrashItem>> = store.entries
        .map { entries ->
            hiddenTrashed(entries)
                .map { PrivateTrashItem(it.id, store.toMediaItem(it), it.hiddenTrashExpiresAt() ?: 0L) }
                .sortedBy { it.expiresAtMillis }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var moveJob: Job? = null

    init {
        viewModelScope.launch { store.load() }
    }

    fun unlock() {
        _unlocked.value = true
    }

    fun lock() {
        _unlocked.value = false
    }

    fun moveToPrivate(item: MediaItem) {
        if (moveJob?.isActive == true) return
        _moveState.value = MoveState.Copying(item.displayName)
        moveJob = viewModelScope.launch {
            when (val result = store.importMedia(item)) {
                is PrivateResult.Success ->
                    _moveState.value = MoveState.Copied(item, Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                is PrivateResult.Failure -> {
                    _moveState.value = MoveState.Idle
                    _message.value = when (result.error) {
                        PrivateError.NotEnoughSpace -> PrivateMessage.CopyNoSpace
                        PrivateError.VerificationFailed -> PrivateMessage.CopyVerifyFailed
                        else -> PrivateMessage.CopyReadFailed
                    }
                }
            }
        }
    }

    fun cancelMove() {
        moveJob?.cancel()
        _moveState.value = MoveState.Idle
    }

    fun dismissMove() {
        _moveState.value = MoveState.Idle
    }

    fun showMessage(message: PrivateMessage) {
        _message.value = message
    }

    fun dismissMessage() {
        _message.value = null
    }

    fun moveOut(item: MediaItem, onSuccess: () -> Unit) {
        val entry = store.entryForKey(item.key) ?: return
        viewModelScope.launch {
            val error = store.exportToGallery(entry.id)
            if (error == null) {
                _message.value = PrivateMessage.ExportDone
                onSuccess()
            } else {
                _message.value =
                    if (error == PrivateError.Unsupported) PrivateMessage.ExportUnsupported else PrivateMessage.ExportFailed
            }
        }
    }

    fun trashPrivate(item: MediaItem) {
        val entry = store.entryForKey(item.key) ?: return
        viewModelScope.launch { store.moveToHiddenTrash(entry.id, System.currentTimeMillis()) }
    }

    fun restoreFromHiddenTrash(ids: List<String>) {
        viewModelScope.launch { store.restoreFromHiddenTrash(ids) }
    }

    /** Only call after the user confirmed a permanent delete. */
    fun deleteForever(ids: List<String>) {
        viewModelScope.launch { store.deleteForever(ids) }
    }

    fun expiredIds(nowMillis: Long): List<String> = expiredHidden(store.entries.value, nowMillis).map { it.id }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GalleryApplication
                PrivateViewModel(app.privateStore)
            }
        }
    }
}
