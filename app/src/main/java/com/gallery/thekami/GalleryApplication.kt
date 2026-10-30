package com.gallery.thekami

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import com.gallery.thekami.core.data.media.MediaStoreMediaRepository
import com.gallery.thekami.core.data.settings.SettingsRepository
import com.gallery.thekami.core.domain.media.MediaRepository

/** Holds the app-wide singletons so every screen shares one library and one settings store. */
class GalleryApplication : Application(), SingletonImageLoader.Factory {
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val mediaRepository: MediaRepository by lazy { MediaStoreMediaRepository(this) }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .memoryCache { MemoryCache.Builder().maxSizePercent(context, 0.2).build() }
            .build()
}
