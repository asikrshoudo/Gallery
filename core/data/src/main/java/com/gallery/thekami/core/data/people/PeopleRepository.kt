package com.gallery.thekami.core.data.people

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gallery.thekami.core.domain.people.Person
import com.gallery.thekami.core.domain.people.Relationship
import com.gallery.thekami.core.domain.people.normalizePersonName
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.peopleDataStore: DataStore<Preferences> by preferencesDataStore(name = "gallery_people")

/**
 * People the USER created and the photos the user linked to them. Stored only in this app's
 * private storage as media keys. Gallery does not recognise faces to fill this in, and
 * nothing here touches the media files.
 */
class PeopleRepository(context: Context) {
    private val store = context.applicationContext.peopleDataStore

    val people: Flow<List<Person>> = store.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toPeople() }
        .distinctUntilChanged()

    /** Returns the new person's id, or null when [rawName] is blank. */
    suspend fun createPerson(rawName: String, relationship: Relationship?, firstMediaKey: String? = null): String? {
        val name = normalizePersonName(rawName) ?: return null
        val id = UUID.randomUUID().toString()
        store.edit { prefs ->
            prefs[Keys.IDS] = prefs[Keys.IDS].orEmpty() + id
            prefs[Keys.name(id)] = name
            prefs[Keys.created(id)] = System.currentTimeMillis()
            relationship?.let { prefs[Keys.relationship(id)] = it.encode() }
            if (firstMediaKey != null) prefs[Keys.media(id)] = setOf(firstMediaKey)
        }
        return id
    }

    suspend fun editPerson(id: String, rawName: String, relationship: Relationship?) {
        val name = normalizePersonName(rawName) ?: return
        store.edit { prefs ->
            if (!prefs.has(id)) return@edit
            prefs[Keys.name(id)] = name
            if (relationship == null) prefs.remove(Keys.relationship(id)) else prefs[Keys.relationship(id)] = relationship.encode()
        }
    }

    suspend fun deletePerson(id: String) {
        store.edit { prefs -> prefs.removePerson(id) }
    }

    suspend fun addMedia(id: String, keys: Collection<String>) {
        if (keys.isEmpty()) return
        store.edit { prefs ->
            if (prefs.has(id)) prefs[Keys.media(id)] = prefs[Keys.media(id)].orEmpty() + keys
        }
    }

    suspend fun removeMedia(id: String, keys: Collection<String>) {
        if (keys.isEmpty()) return
        store.edit { prefs ->
            if (!prefs.has(id)) return@edit
            prefs[Keys.media(id)] = prefs[Keys.media(id)].orEmpty() - keys.toSet()
            if (prefs[Keys.cover(id)] in keys) prefs.remove(Keys.cover(id))
        }
    }

    /** The cover must already be one of the person's photos, otherwise the request is ignored. */
    suspend fun setCover(id: String, key: String) {
        store.edit { prefs ->
            if (prefs.has(id) && key in prefs[Keys.media(id)].orEmpty()) prefs[Keys.cover(id)] = key
        }
    }

    /** Folds [sourceId] into [targetId] in one atomic edit, then removes the source person. */
    suspend fun merge(targetId: String, sourceId: String) {
        if (targetId == sourceId) return
        store.edit { prefs ->
            if (!prefs.has(targetId) || !prefs.has(sourceId)) return@edit
            prefs[Keys.media(targetId)] = prefs[Keys.media(targetId)].orEmpty() + prefs[Keys.media(sourceId)].orEmpty()
            if (prefs[Keys.relationship(targetId)] == null) {
                prefs[Keys.relationship(sourceId)]?.let { prefs[Keys.relationship(targetId)] = it }
            }
            if (prefs[Keys.cover(targetId)] == null) {
                prefs[Keys.cover(sourceId)]?.let { prefs[Keys.cover(targetId)] = it }
            }
            prefs.removePerson(sourceId)
        }
    }

    private fun Preferences.has(id: String): Boolean = id in this[Keys.IDS].orEmpty()
}

private object Keys {
    val IDS = stringSetPreferencesKey("person_ids")
    fun name(id: String) = stringPreferencesKey("person_name_$id")
    fun created(id: String) = longPreferencesKey("person_created_$id")
    fun cover(id: String) = stringPreferencesKey("person_cover_$id")
    fun media(id: String) = stringSetPreferencesKey("person_media_$id")
    fun relationship(id: String) = stringPreferencesKey("person_relationship_$id")
}

private fun MutablePreferences.removePerson(id: String) {
    this[Keys.IDS] = this[Keys.IDS].orEmpty() - id
    remove(Keys.name(id))
    remove(Keys.created(id))
    remove(Keys.cover(id))
    remove(Keys.media(id))
    remove(Keys.relationship(id))
}

private fun Preferences.toPeople(): List<Person> =
    this[Keys.IDS].orEmpty()
        .mapNotNull { id ->
            val name = this[Keys.name(id)] ?: return@mapNotNull null
            Person(
                id = id,
                name = name,
                relationship = Relationship.decode(this[Keys.relationship(id)]),
                createdAtMillis = this[Keys.created(id)] ?: 0L,
                coverKey = this[Keys.cover(id)],
                mediaKeys = this[Keys.media(id)].orEmpty(),
            )
        }
        .sortedWith(compareBy<Person>({ it.name.lowercase() }, { it.id }))
