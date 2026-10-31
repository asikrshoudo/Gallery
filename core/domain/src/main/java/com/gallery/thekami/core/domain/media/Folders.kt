package com.gallery.thekami.core.domain.media

/** [cover] is the first (newest) item of the folder, so pass a newest-first list. */
data class FolderInfo(
    val id: String,
    val name: String,
    val itemCount: Int,
    val cover: MediaItem,
    val source: MediaSource,
)

/** [featured] = camera and screenshot folders. [others] = every other folder, listed once. */
data class FolderOverview(
    val featured: List<FolderInfo>,
    val others: List<FolderInfo>,
) {
    val totalCount: Int get() = featured.size + others.size
}

/** Folder identity: the relative path MediaStore reports, else its bucket id. */
fun folderKey(item: MediaItem): String =
    item.relativePath?.lowercase() ?: item.bucketId?.let { "bucket:$it" } ?: "unknown"

private fun folderName(item: MediaItem): String {
    val bucket = item.bucketName
    if (!bucket.isNullOrBlank()) return bucket
    return item.relativePath.orEmpty().split('/').lastOrNull { it.isNotEmpty() }.orEmpty()
}

fun groupFolders(items: List<MediaItem>): List<FolderInfo> {
    val groups = LinkedHashMap<String, MutableList<MediaItem>>()
    for (item in distinctMedia(items)) groups.getOrPut(folderKey(item)) { mutableListOf() } += item
    return groups.map { (key, list) ->
        val cover = list.first()
        FolderInfo(key, folderName(cover), list.size, cover, classifySource(cover))
    }
}

fun splitFolders(folders: List<FolderInfo>): FolderOverview {
    val (featured, others) = folders.partition { it.source != MediaSource.OtherApps }
    return FolderOverview(
        featured = featured.sortedWith(compareBy<FolderInfo>({ it.source.ordinal }, { it.name.lowercase() })),
        others = others.sortedBy { it.name.lowercase() },
    )
}

fun folderItems(items: List<MediaItem>, folderId: String): List<MediaItem> =
    distinctMedia(items).filter { folderKey(it) == folderId }
