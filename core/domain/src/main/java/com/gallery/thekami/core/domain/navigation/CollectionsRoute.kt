package com.gallery.thekami.core.domain.navigation

/** Screens inside the Collections destination. Encoded as strings so they survive rotation. */
sealed interface CollectionsRoute {
    fun encode(): String

    data object Albums : CollectionsRoute {
        override fun encode() = "albums"
    }

    data object Folders : CollectionsRoute {
        override fun encode() = "folders"
    }

    data object OtherFolders : CollectionsRoute {
        override fun encode() = "other-folders"
    }

    data object Favourites : CollectionsRoute {
        override fun encode() = "favourites"
    }

    data object Pinned : CollectionsRoute {
        override fun encode() = "pinned"
    }

    data object People : CollectionsRoute {
        override fun encode() = "people"
    }

    data object Trash : CollectionsRoute {
        override fun encode() = "trash"
    }

    data object Private : CollectionsRoute {
        override fun encode() = "private"
    }

    data object HiddenTrash : CollectionsRoute {
        override fun encode() = "hidden-trash"
    }

    data class AlbumDetail(val albumId: String) : CollectionsRoute {
        override fun encode() = "album:$albumId"
    }

    data class AlbumPicker(val albumId: String) : CollectionsRoute {
        override fun encode() = "picker:$albumId"
    }

    data class FolderDetail(val folderId: String) : CollectionsRoute {
        override fun encode() = "folder:$folderId"
    }

    data class PersonDetail(val personId: String) : CollectionsRoute {
        override fun encode() = "person:$personId"
    }

    data class PersonPicker(val personId: String) : CollectionsRoute {
        override fun encode() = "person-picker:$personId"
    }

    companion object {
        fun decode(value: String): CollectionsRoute? {
            val kind = value.substringBefore(':')
            val id = value.substringAfter(':', "")
            return when (kind) {
                "albums" -> Albums
                "folders" -> Folders
                "other-folders" -> OtherFolders
                "favourites" -> Favourites
                "pinned" -> Pinned
                "people" -> People
                "trash" -> Trash
                "private" -> Private
                "hidden-trash" -> HiddenTrash
                "album" -> AlbumDetail(id)
                "picker" -> AlbumPicker(id)
                "folder" -> FolderDetail(id)
                "person" -> PersonDetail(id)
                "person-picker" -> PersonPicker(id)
                else -> null
            }
        }
    }
}
