package com.gallery.thekami.core.data.media

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import com.gallery.thekami.core.domain.media.MediaType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/** Emits Unit whenever MediaStore reports a change. The observer lives only while collected. */
internal class MediaStoreChanges(context: Context) {
    private val resolver = context.applicationContext.contentResolver

    fun changes(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        MediaType.entries.forEach { type ->
            resolver.registerContentObserver(collectionUri(type), true, observer)
        }
        awaitClose { resolver.unregisterContentObserver(observer) }
    }.conflate()
}
