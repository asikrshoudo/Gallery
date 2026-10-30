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
import com.gallery.thekami.core.data.media.MediaStoreMediaRepository
import com.gallery.thekami.core.data.settings.SettingsRepository
import com.gallery.thekami.core.domain.media.MediaRepository

/** Holds the app-wide singletons so every screen shares one library and one settings store. */
class GalleryApplication : Application(), SingletonImageLoader.Factory {
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val mediaRepository: MediaRepository by lazy { MediaStoreMediaRepository(this) }

    /**
     * Grid and filmstrip thumbnails: stills only (a GIF shows its first frame, so a screen full
     * of GIFs does not animate) plus video frames. Decoders are added explicitly, not via
     * service loading, so behaviour does not depend on auto-registration.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .serviceLoaderEnabled(false)
            .components { add(VideoFrameDecoder.Factory()) }
            .memoryCache { MemoryCache.Builder().maxSizePercent(context, 0.2).build() }
            .build()

    /** Viewer only: also decodes animated GIF (and animated WebP on Android 9+). */
    val viewerImageLoader: ImageLoader by lazy {
        ImageLoader.Builder(this)
            .serviceLoaderEnabled(false)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(AnimatedImageDecoder.Factory()) else add(GifDecoder.Factory())
                add(VideoFrameDecoder.Factory())
            }
            .memoryCache { MemoryCache.Builder().maxSizePercent(this@GalleryApplication, 0.15).build() }
            .build()
    }
}
