package com.gallery.thekami.core.data.album

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gallery.thekami.core.domain.album.Album
import com.gallery.thekami.core.domain.album.normalizeAlbumName
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.albumsDataStore: DataStore<Preferences> by preferencesDataStore(name = "gallery_albums")

/**
 * Persistent logical albums. Every change is one atomic DataStore edit. Albums only store
 * media keys: deleting an album, or removing items from it, never touches the media files.
 */
class AlbumsRepository(context: Context) {
    private val store = context.applicationContext.albumsDataStore

    val albums: Flow<List<Album>> = store.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toAlbums() }
        .distinctUntilChanged()

    /** Returns the new album id, or null when [rawName] is blank. */
    suspend fun createAlbum(rawName: String): String? {
        val name = normalizeAlbumName(rawName) ?: return null
        val id = UUID.randomUUID().toString()
        store.edit { prefs ->
            prefs[Keys.IDS] = (prefs[Keys.IDS] ?: emptySet()) + id
            prefs[Keys.name(id)] = name
            prefs[Keys.created(id)] = System.currentTimeMillis()
        }
        return id
    }

    suspend fun renameAlbum(id: String, rawName: String) {
        val name = normalizeAlbumName(rawName) ?: return
        store.edit { prefs -> if (prefs.has(id)) prefs[Keys.name(id)] = name }
    }

    suspend fun deleteAlbum(id: String) {
        store.edit { prefs ->
            prefs[Keys.IDS] = (prefs[Keys.IDS] ?: emptySet()) - id
            prefs.remove(Keys.name(id))
            prefs.remove(Keys.created(id))
            prefs.remove(Keys.cover(id))
            prefs.remove(Keys.media(id))
        }
    }

    suspend fun addMedia(id: String, keys: Collection<String>) {
        if (keys.isEmpty()) return
        store.edit { prefs ->
            if (prefs.has(id)) prefs[Keys.media(id)] = (prefs[Keys.media(id)] ?: emptySet()) + keys
        }
    }

    suspend fun removeMedia(id: String, keys: Collection<String>) {
        if (keys.isEmpty()) return
        store.edit { prefs ->
            if (!prefs.has(id)) return@edit
            prefs[Keys.media(id)] = (prefs[Keys.media(id)] ?: emptySet()) - keys.toSet()
            if (prefs[Keys.cover(id)] in keys) prefs.remove(Keys.cover(id))
        }
    }

    /** The cover must already be in the album, otherwise the request is ignored. */
    suspend fun setCover(id: String, key: String) {
        store.edit { prefs ->
            if (prefs.has(id) && key in (prefs[Keys.media(id)] ?: emptySet())) prefs[Keys.cover(id)] = key
        }
    }

    private fun Preferences.has(id: String): Boolean = id in (this[Keys.IDS] ?: emptySet())
}

private object Keys {
    val IDS = stringSetPreferencesKey("album_ids")
    fun name(id: String) = stringPreferencesKey("album_name_$id")
    fun created(id: String) = longPreferencesKey("album_created_$id")
    fun cover(id: String) = stringPreferencesKey("album_cover_$id")
    fun media(id: String) = stringSetPreferencesKey("album_media_$id")
}

private fun Preferences.toAlbums(): List<Album> =
    (this[Keys.IDS] ?: emptySet())
        .mapNotNull { id ->
            val name = this[Keys.name(id)] ?: return@mapNotNull null
            Album(
                id = id,
                name = name,
                createdAtMillis = this[Keys.created(id)] ?: 0L,
                coverKey = this[Keys.cover(id)],
                mediaKeys = this[Keys.media(id)] ?: emptySet(),
            )
        }
        .sortedWith(compareBy<Album>({ it.createdAtMillis }, { it.id }))
