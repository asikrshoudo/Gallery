package com.gallery.thekami.core.domain.media

import com.gallery.thekami.core.domain.settings.HomeSettings
import com.gallery.thekami.core.domain.settings.HomeViewMode

/**
 * Picks what Home shows from the full library. Never mutates the input and keeps its order.
 * [excludedKeys] is the hook for hidden/private and Trash items once those exist.
 */
fun selectHomeMedia(
    items: List<MediaItem>,
    settings: HomeSettings,
    excludedKeys: Set<String> = emptySet(),
): List<MediaItem> = distinctMedia(items).filter { item ->
    item.key !in excludedKeys && typeAllowed(item, settings) && sourceAllowed(classifySource(item), settings)
}

private fun typeAllowed(item: MediaItem, settings: HomeSettings): Boolean = when (item.type) {
    MediaType.Image -> settings.includePhotos
    MediaType.Video -> settings.includeVideos
}

private fun sourceAllowed(source: MediaSource, settings: HomeSettings): Boolean = when (source) {
    MediaSource.Camera -> true
    MediaSource.Screenshots -> settings.viewMode == HomeViewMode.AllMedia && settings.includeScreenshots
    MediaSource.OtherApps -> settings.viewMode == HomeViewMode.AllMedia && settings.includeOtherApps
}
