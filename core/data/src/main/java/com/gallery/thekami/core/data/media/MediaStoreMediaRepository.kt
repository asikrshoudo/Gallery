@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

package com.gallery.thekami.core.data.media

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.gallery.thekami.core.domain.media.MediaAccess
import com.gallery.thekami.core.domain.media.MediaLibraryState
import com.gallery.thekami.core.domain.media.MediaRepository
import com.gallery.thekami.core.domain.media.sortMedia
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge

private const val CHANGE_DEBOUNCE_MS = 750L

/**
 * MediaStore-backed library. Work happens on collector cancellation-aware IO. A new trigger
 * cancels an in-flight load. Observer events are debounced, and on Android 11+ the MediaStore
 * generation counter lets us skip reloads when nothing really changed.
 */
class MediaStoreMediaRepository(context: Context) : MediaRepository {
    private val appContext = context.applicationContext
    private val dataSource = MediaStoreDataSource(appContext)
    private val changes = MediaStoreChanges(appContext)
    private val accessChecker = MediaAccessChecker(appContext)

    /** true = forced reload, false = reload only if something changed. */
    private val triggers = MutableSharedFlow<Boolean>(extraBufferCapacity = 8)

    @Volatile private var lastGeneration: Long? = null
    @Volatile private var lastAccess: MediaAccess? = null
    @Volatile private var forceNext = false

    override fun libraryState(): Flow<MediaLibraryState> =
        merge(
            flowOf(false),
            changes.changes().debounce(CHANGE_DEBOUNCE_MS).map { false },
            triggers,
        )
            .mapLatest { force -> load(force) }
            .filterNotNull()

    override fun refresh() {
        forceNext = true
        triggers.tryEmit(true)
    }

    override fun checkForChanges() {
        triggers.tryEmit(false)
    }

    override fun currentAccess(): MediaAccess = accessChecker.currentAccess()

    override fun permissionsToRequest(): List<String> = accessChecker.permissionsToRequest()

    /** Returns null when nothing changed, so the previous state stays valid. */
    private suspend fun load(force: Boolean): MediaLibraryState? {
        val mustForce = force || forceNext
        val access = accessChecker.currentAccess()
        if (access == MediaAccess.Denied) {
            val unchanged = !mustForce && lastAccess == MediaAccess.Denied
            lastAccess = MediaAccess.Denied
            lastGeneration = null
            forceNext = false
            return if (unchanged) null else MediaLibraryState.PermissionRequired(access)
        }

        val generation = currentGeneration()
        val mustLoad = mustForce || generation == null ||
            generation != lastGeneration || access != lastAccess
        if (!mustLoad) return null

        return try {
            val result = dataSource.queryAll()
            lastGeneration = generation
            lastAccess = access
            forceNext = false
            MediaLibraryState.Ready(sortMedia(result.items), access, result.skipped)
        } catch (e: CancellationException) {
            throw e
        } catch (e: SecurityException) {
            lastGeneration = null
            lastAccess = null
            MediaLibraryState.PermissionRequired(MediaAccess.Denied)
        } catch (e: Exception) {
            lastGeneration = null
            MediaLibraryState.Failed(e.message)
        }
    }

    private fun currentGeneration(): Long? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                MediaStore.getGeneration(appContext, EXTERNAL_VOLUME)
            } catch (e: RuntimeException) {
                null
            }
        } else {
            null
        }
}
