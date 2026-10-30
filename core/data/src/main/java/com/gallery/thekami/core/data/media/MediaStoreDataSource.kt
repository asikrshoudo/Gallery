package com.gallery.thekami.core.data.media

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.MediaStore
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.MediaLocation
import com.gallery.thekami.core.domain.media.MediaPaths
import com.gallery.thekami.core.domain.media.MediaType
import com.gallery.thekami.core.domain.media.distinctMedia
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

internal data class QueryResult(val items: List<MediaItem>, val skipped: Int)

/**
 * Read-only MediaStore access: it only calls ContentResolver.query for metadata. It never
 * inserts, updates, deletes, moves or opens media files, and never decodes pixels.
 * Discovery covers every folder MediaStore knows about, not a hardcoded list.
 */
internal class MediaStoreDataSource(context: Context) {
    private val resolver: ContentResolver = context.applicationContext.contentResolver

    suspend fun queryAll(): QueryResult = withContext(Dispatchers.IO) {
        val items = ArrayList<MediaItem>()
        var skipped = 0
        var denied = 0
        for (type in MediaType.entries) {
            try {
                val result = query(type)
                items += result.items
                skipped += result.skipped
            } catch (e: SecurityException) {
                denied++
            }
        }
        if (denied == MediaType.entries.size) throw SecurityException("No media access")
        QueryResult(distinctMedia(items), skipped)
    }

    private suspend fun query(type: MediaType): QueryResult {
        val collection = collectionUri(type)
        return try {
            queryWith(collection, type, includeLocation = true)
        } catch (e: IllegalArgumentException) {
            // Some old devices reject the legacy location columns; retry without them.
            queryWith(collection, type, includeLocation = false)
        }
    }

    private suspend fun queryWith(collection: Uri, type: MediaType, includeLocation: Boolean): QueryResult {
        val cursor = resolver.query(collection, projection(type, includeLocation), null, null, null)
            ?: throw IllegalStateException("MediaStore returned no cursor")
        val items = ArrayList<MediaItem>()
        var skipped = 0
        cursor.use { c ->
            val ix = ColumnIndexes(c)
            var rows = 0
            while (c.moveToNext()) {
                if (++rows % 256 == 0) coroutineContext.ensureActive()
                val item = try {
                    read(c, ix, collection, type)
                } catch (e: RuntimeException) {
                    null
                }
                if (item == null) skipped++ else items += item
            }
        }
        return QueryResult(items, skipped)
    }

    @SuppressLint("InlinedApi")
    @Suppress("DEPRECATION")
    private fun projection(type: MediaType, includeLocation: Boolean): Array<String> {
        val columns = mutableListOf(
            BaseColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
        )
        columns += if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.MediaColumns.RELATIVE_PATH
        } else {
            MediaStore.MediaColumns.DATA
        }
        when (type) {
            MediaType.Image -> {
                columns += MediaStore.Images.ImageColumns.ORIENTATION
                // MediaStore hides GPS on Android 10+ (needs EXIF + ACCESS_MEDIA_LOCATION), so
                // the legacy columns are only read where they still work.
                if (includeLocation && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    columns += MediaStore.Images.ImageColumns.LATITUDE
                    columns += MediaStore.Images.ImageColumns.LONGITUDE
                }
            }
            MediaType.Video -> columns += MediaStore.Video.VideoColumns.DURATION
        }
        return columns.toTypedArray()
    }

    /** Column positions; -1 when a column was not requested. */
    @SuppressLint("InlinedApi")
    @Suppress("DEPRECATION")
    private class ColumnIndexes(c: Cursor) {
        val id = c.getColumnIndexOrThrow(BaseColumns._ID)
        val name = c.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
        val mime = c.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
        val width = c.getColumnIndex(MediaStore.MediaColumns.WIDTH)
        val height = c.getColumnIndex(MediaStore.MediaColumns.HEIGHT)
        val size = c.getColumnIndex(MediaStore.MediaColumns.SIZE)
        val taken = c.getColumnIndex(MediaStore.MediaColumns.DATE_TAKEN)
        val modified = c.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
        val added = c.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
        val bucketId = c.getColumnIndex(MediaStore.MediaColumns.BUCKET_ID)
        val bucketName = c.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
        val relativePath = c.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
        val data = c.getColumnIndex(MediaStore.MediaColumns.DATA)
        val orientation = c.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION)
        val duration = c.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)
        val latitude = c.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE)
        val longitude = c.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE)
    }

    /** Returns null for rows that cannot be used (for example no MIME type). */
    private fun read(c: Cursor, ix: ColumnIndexes, collection: Uri, type: MediaType): MediaItem? {
        val id = c.getLong(ix.id)
        val mime = c.stringOrNull(ix.mime)?.takeIf { it.isNotBlank() } ?: return null
        val relativePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            c.stringOrNull(ix.relativePath)
        } else {
            MediaPaths.relativeDirFromDataPath(c.stringOrNull(ix.data))
        }
        val lat = c.doubleOrNull(ix.latitude)
        val lon = c.doubleOrNull(ix.longitude)
        val location = if (lat != null && lon != null && !(lat == 0.0 && lon == 0.0)) {
            MediaLocation(lat, lon)
        } else {
            null
        }
        return MediaItem(
            id = id,
            uri = ContentUris.withAppendedId(collection, id).toString(),
            displayName = c.stringOrNull(ix.name)?.takeIf { it.isNotBlank() } ?: "media-$id",
            mimeType = mime,
            type = type,
            width = c.intOrNull(ix.width)?.takeIf { it > 0 },
            height = c.intOrNull(ix.height)?.takeIf { it > 0 },
            sizeBytes = c.longOrNull(ix.size)?.takeIf { it >= 0 },
            dateTakenMillis = c.longOrNull(ix.taken)?.takeIf { it > 0 },
            dateModifiedMillis = c.longOrNull(ix.modified)?.takeIf { it > 0 }?.let { it * 1000 },
            dateAddedMillis = c.longOrNull(ix.added)?.takeIf { it > 0 }?.let { it * 1000 },
            durationMillis = c.longOrNull(ix.duration)?.takeIf { it >= 0 },
            orientationDegrees = (((c.intOrNull(ix.orientation) ?: 0) % 360) + 360) % 360,
            location = location,
            bucketId = c.longOrNull(ix.bucketId),
            bucketName = c.stringOrNull(ix.bucketName),
            relativePath = relativePath,
        )
    }
}

private fun Cursor.stringOrNull(i: Int): String? = if (i < 0 || isNull(i)) null else getString(i)
private fun Cursor.longOrNull(i: Int): Long? = if (i < 0 || isNull(i)) null else getLong(i)
private fun Cursor.intOrNull(i: Int): Int? = if (i < 0 || isNull(i)) null else getInt(i)
private fun Cursor.doubleOrNull(i: Int): Double? = if (i < 0 || isNull(i)) null else getDouble(i)
