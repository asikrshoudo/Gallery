package com.gallery.thekami

import android.app.Application
import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.video.VideoFrameDecoder
import com.gallery.thekami.core.data.album.AlbumsRepository
import com.gallery.thekami.core.data.media.MediaStoreMediaRepository
import com.gallery.thekami.core.data.settings.SettingsRepository
import com.gallery.thekami.core.domain.media.MediaRepository
import com.gallery.thekami.core.domain.media.SharedMediaLibrary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Holds the app-wide singletons so every screen shares one library and one settings store. */
class GalleryApplication : Application(), SingletonImageLoader.Factory {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val albumsRepository: AlbumsRepository by lazy { AlbumsRepository(this) }
    val mediaRepository: MediaRepository by lazy { MediaStoreMediaRepository(this) }

    /** One shared library subscription for Home and Collections. */
    val mediaLibrary: SharedMediaLibrary by lazy { SharedMediaLibrary(mediaRepository, appScope) }

    /**
     * Grid and filmstrip thumbnails. The video decoder is added explicitly. The GIF decoder is
     * not added here, but Coil may still register it by itself, so check on a device whether
     * GIF thumbnails in the grid stay still.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .memoryCache { MemoryCache.Builder().maxSizePercent(context, 0.2).build() }
            .build()

    /** Viewer only: explicitly adds animated GIF (and animated WebP on Android 9+) decoding. */
    val viewerImageLoader: ImageLoader by lazy {
        ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(AnimatedImageDecoder.Factory()) else add(GifDecoder.Factory())
                add(VideoFrameDecoder.Factory())
            }
            .memoryCache { MemoryCache.Builder().maxSizePercent(this@GalleryApplication, 0.15).build() }
            .build()
    }
}
