package com.gallery.thekami.core.data.trash

import android.content.Context
import android.os.Build
import com.gallery.thekami.core.data.media.MediaStoreDataSource
import com.gallery.thekami.core.domain.trash.TrashedMedia

/**
 * Reads Android's own MediaStore Trash (Android 11+). Restore and permanent delete are done by
 * the app through system requests, so Android asks the user to confirm each time.
 */
class MediaStoreTrashRepository(context: Context) {
    private val source = MediaStoreDataSource(context)

    val supported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    /** Soonest-to-expire first. Only what Android lets this app see is returned. */
    suspend fun load(): List<TrashedMedia> =
        source.queryTrashed().sortedWith(compareBy(nullsLast()) { it.expiresAtMillis })
}
