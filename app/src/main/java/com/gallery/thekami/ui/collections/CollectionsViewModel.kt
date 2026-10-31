package com.gallery.thekami.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gallery.thekami.GalleryApplication
import com.gallery.thekami.core.data.album.AlbumsRepository
import com.gallery.thekami.core.data.people.PeopleRepository
import com.gallery.thekami.core.domain.album.Album
import com.gallery.thekami.core.domain.album.resolveAlbum
import com.gallery.thekami.core.domain.media.MediaLibraryState
import com.gallery.thekami.core.domain.media.MediaMarks
import com.gallery.thekami.core.domain.media.buildLivePhotoPairs
import com.gallery.thekami.core.domain.media.groupFolders
import com.gallery.thekami.core.domain.media.selectMarked
import com.gallery.thekami.core.domain.media.splitFolders
import com.gallery.thekami.core.domain.people.Person
import com.gallery.thekami.core.domain.people.Relationship
import com.gallery.thekami.core.domain.people.resolvePerson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Resolves albums, people, folders, favourites and pins against the shared library. */
class CollectionsViewModel(
    library: Flow<MediaLibraryState>,
    private val albumsRepository: AlbumsRepository,
    private val peopleRepository: PeopleRepository,
    marks: Flow<MediaMarks>,
    faceCounts: StateFlow<Map<String, Int>>,
) : ViewModel() {
    val albums: StateFlow<List<Album>> =
        albumsRepository.albums.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val people: StateFlow<List<Person>> =
        peopleRepository.people.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Photos with faces found by the optional detector. Empty unless detection is on. */
    val faceCounts: StateFlow<Map<String, Int>> = faceCounts

    val state: StateFlow<CollectionsState> =
        combine(library, albumsRepository.albums, peopleRepository.people, marks, ::toState)
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionsState.Loading)

    /** Optionally puts [firstMediaKey] into the new album (used by "Add to album" in the viewer). */
    fun createAlbum(name: String, firstMediaKey: String? = null) {
        viewModelScope.launch {
            val id = albumsRepository.createAlbum(name)
            if (id != null && firstMediaKey != null) albumsRepository.addMedia(id, listOf(firstMediaKey))
        }
    }

    fun renameAlbum(id: String, name: String) {
        viewModelScope.launch { albumsRepository.renameAlbum(id, name) }
    }

    fun deleteAlbum(id: String) {
        viewModelScope.launch { albumsRepository.deleteAlbum(id) }
    }

    fun addMedia(id: String, keys: Collection<String>) {
        viewModelScope.launch { albumsRepository.addMedia(id, keys) }
    }

    fun removeMedia(id: String, keys: Collection<String>) {
        viewModelScope.launch { albumsRepository.removeMedia(id, keys) }
    }

    fun setCover(id: String, key: String) {
        viewModelScope.launch { albumsRepository.setCover(id, key) }
    }

    fun toggleMedia(albumId: String, key: String, add: Boolean) {
        if (add) addMedia(albumId, listOf(key)) else removeMedia(albumId, listOf(key))
    }

    fun createPerson(name: String, relationship: Relationship?, firstMediaKey: String? = null) {
        viewModelScope.launch { peopleRepository.createPerson(name, relationship, firstMediaKey) }
    }

    fun editPerson(id: String, name: String, relationship: Relationship?) {
        viewModelScope.launch { peopleRepository.editPerson(id, name, relationship) }
    }

    fun deletePerson(id: String) {
        viewModelScope.launch { peopleRepository.deletePerson(id) }
    }

    fun mergePerson(targetId: String, sourceId: String) {
        viewModelScope.launch { peopleRepository.merge(targetId, sourceId) }
    }

    fun addPersonMedia(id: String, keys: Collection<String>) {
        viewModelScope.launch { peopleRepository.addMedia(id, keys) }
    }

    fun removePersonMedia(id: String, keys: Collection<String>) {
        viewModelScope.launch { peopleRepository.removeMedia(id, keys) }
    }

    fun setPersonCover(id: String, key: String) {
        viewModelScope.launch { peopleRepository.setCover(id, key) }
    }

    fun togglePerson(personId: String, key: String, add: Boolean) {
        if (add) addPersonMedia(personId, listOf(key)) else removePersonMedia(personId, listOf(key))
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GalleryApplication
                CollectionsViewModel(
                    library = app.mediaLibrary.state,
                    albumsRepository = app.albumsRepository,
                    peopleRepository = app.peopleRepository,
                    marks = app.marksRepository.marks,
                    faceCounts = app.faceScan.counts,
                )
            }
        }
    }
}

private fun toState(
    library: MediaLibraryState,
    albums: List<Album>,
    people: List<Person>,
    marks: MediaMarks,
): CollectionsState = when (library) {
    MediaLibraryState.Loading -> CollectionsState.Loading
    is MediaLibraryState.PermissionRequired -> CollectionsState.PermissionRequired
    is MediaLibraryState.Failed -> CollectionsState.Failed(library.message)
    is MediaLibraryState.Ready -> {
        val byKey = library.items.associateBy { it.key }
        val favourites = selectMarked(library.items, marks.favourites)
        val pinned = selectMarked(library.items, marks.pinned)
        CollectionsState.Ready(
            items = library.items,
            albums = albums.map { resolveAlbum(it, byKey) },
            people = people.map { resolvePerson(it, byKey) },
            folders = splitFolders(groupFolders(library.items)),
            livePairs = buildLivePhotoPairs(library.items),
            access = library.access,
            favourites = favourites,
            favouriteMissing = marks.favourites.size - favourites.size,
            pinned = pinned,
            pinnedMissing = marks.pinned.size - pinned.size,
        )
    }
}
