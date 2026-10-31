package com.gallery.thekami.core.data.marks

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gallery.thekami.core.domain.media.MediaMarks
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.marksDataStore: DataStore<Preferences> by preferencesDataStore(name = "gallery_marks")

/**
 * Favourites and pins live only in this app's own storage. Toggling is one atomic edit and
 * never touches the media file, its permissions or its location.
 */
class MarksRepository(context: Context) {
    private val store = context.applicationContext.marksDataStore

    val marks: Flow<MediaMarks> = store.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { MediaMarks(it[Keys.FAVOURITES].orEmpty(), it[Keys.PINNED].orEmpty()) }
        .distinctUntilChanged()

    suspend fun toggleFavourite(key: String) {
        store.edit { it[Keys.FAVOURITES] = it[Keys.FAVOURITES].orEmpty().toggled(key) }
    }

    suspend fun togglePinned(key: String) {
        store.edit { it[Keys.PINNED] = it[Keys.PINNED].orEmpty().toggled(key) }
    }
}

private object Keys {
    val FAVOURITES = stringSetPreferencesKey("favourites")
    val PINNED = stringSetPreferencesKey("pinned")
}

private fun Set<String>.toggled(key: String): Set<String> = if (key in this) this - key else this + key
