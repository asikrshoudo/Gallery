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

    data class AlbumDetail(val albumId: String) : CollectionsRoute {
        override fun encode() = "album:$albumId"
    }

    data class AlbumPicker(val albumId: String) : CollectionsRoute {
        override fun encode() = "picker:$albumId"
    }

    data class FolderDetail(val folderId: String) : CollectionsRoute {
        override fun encode() = "folder:$folderId"
    }

    companion object {
        fun decode(value: String): CollectionsRoute? {
            val kind = value.substringBefore(':')
            val id = value.substringAfter(':', "")
            return when (kind) {
                "albums" -> Albums
                "folders" -> Folders
                "other-folders" -> OtherFolders
                "album" -> AlbumDetail(id)
                "picker" -> AlbumPicker(id)
                "folder" -> FolderDetail(id)
                else -> null
            }
        }
    }
}
