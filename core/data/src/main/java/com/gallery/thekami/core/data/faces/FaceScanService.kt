package com.gallery.thekami.core.data.faces

import android.app.ActivityManager
import android.content.Context
import android.os.PowerManager
import com.gallery.thekami.core.domain.faces.FaceScanState
import com.gallery.thekami.core.domain.faces.pendingForScan
import com.gallery.thekami.core.domain.faces.scanProgress
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.MediaLibraryState
import com.gallery.thekami.core.domain.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield

private const val BATCH_SIZE = 16

/**
 * Optional, incremental face DETECTION. It only runs while the user has switched it on, skips
 * everything already checked (results are cached on disk), pauses in battery saver, and is
 * disabled on low-RAM devices. It does not recognise or group people.
 */
class FaceScanService(
    context: Context,
    private val library: Flow<MediaLibraryState>,
    private val settings: Flow<AppSettings>,
    private val scope: CoroutineScope,
) {
    private val appContext = context.applicationContext
    private val store = FaceIndexStore(appContext)
    private val scanner = FaceDetectionScanner(appContext)
    private val power = appContext.getSystemService(PowerManager::class.java)
    private val mutex = Mutex()

    @Volatile private var generation = 0

    val supported: Boolean = appContext.getSystemService(ActivityManager::class.java)?.isLowRamDevice != true

    private val _state = MutableStateFlow(FaceScanState(supported = supported))
    val state: StateFlow<FaceScanState> = _state.asStateFlow()

    private val _counts = MutableStateFlow<Map<String, Int>>(emptyMap())

    /** Media key -> number of faces found (0 = none, negative = could not be read). */
    val counts: StateFlow<Map<String, Int>> = _counts.asStateFlow()

    fun start() {
        scope.launch {
            _counts.value = store.load()
            settings.map { it.people.detectFaces }.distinctUntilChanged().collectLatest { enabled ->
                _state.update { it.copy(enabled = enabled, running = false, pausedForBattery = false) }
                if (enabled && supported) {
                    library.collectLatest { lib -> if (lib is MediaLibraryState.Ready) scan(lib.items) }
                }
            }
        }
    }

    /** Deletes every stored result. Turn detection off first so it does not start over. */
    suspend fun clear() {
        mutex.withLock {
            generation++
            store.clear()
            _counts.value = emptyMap()
            _state.update { it.copy(scanned = 0) }
        }
    }

    private suspend fun scan(items: List<MediaItem>) {
        publishProgress(items)
        while (true) {
            val pending = pendingForScan(items, _counts.value.keys, BATCH_SIZE)
            if (pending.isEmpty()) break
            while (power?.isPowerSaveMode == true) {
                _state.update { it.copy(running = false, pausedForBattery = true) }
                delay(30_000)
            }
            _state.update { it.copy(running = true, pausedForBattery = false) }
            val startGeneration = generation
            val batch = HashMap<String, Int>()
            for (item in pending) {
                batch[item.key] = scanner.countFaces(item)
                yield()
            }
            mutex.withLock {
                if (startGeneration == generation) {
                    store.append(batch)
                    _counts.update { it + batch }
                }
            }
            publishProgress(items)
            delay(50)
        }
        _state.update { it.copy(running = false) }
    }

    private fun publishProgress(items: List<MediaItem>) {
        val progress = scanProgress(items, _counts.value)
        _state.update { it.copy(scanned = progress.scanned, total = progress.total) }
    }
}
