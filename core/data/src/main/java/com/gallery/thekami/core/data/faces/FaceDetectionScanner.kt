package com.gallery.thekami.core.data.faces

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.FaceDetector
import android.net.Uri
import com.gallery.thekami.core.domain.faces.FACE_SCAN_FAILED
import com.gallery.thekami.core.domain.media.MediaItem
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_SIDE = 640
private const val MAX_FACES = 8

/**
 * Counts faces with Android's built-in android.media.FaceDetector. It only finds FRONT-FACING,
 * upright faces and can miss or mis-detect; it does NOT identify or compare people. Images are
 * decoded at most ~640px on the long side, so memory use stays small.
 */
internal class FaceDetectionScanner(context: Context) {
    private val resolver = context.applicationContext.contentResolver

    /** Number of faces, or [FACE_SCAN_FAILED] when the image can't be read or decoded. */
    suspend fun countFaces(item: MediaItem): Int = withContext(Dispatchers.Default) {
        try {
            detect(item)
        } catch (e: CancellationException) {
            throw e
        } catch (e: OutOfMemoryError) {
            FACE_SCAN_FAILED
        } catch (e: Exception) {
            FACE_SCAN_FAILED
        }
    }

    private fun detect(item: MediaItem): Int {
        val uri = Uri.parse(item.uri)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return FACE_SCAN_FAILED
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return FACE_SCAN_FAILED

        var sample = 1
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / (sample * 2) >= MAX_SIDE) sample *= 2
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: return FACE_SCAN_FAILED

        val prepared = prepare(decoded, item.orientationDegrees)
        try {
            if (prepared.width < 32 || prepared.height < 32) return 0
            val detector = FaceDetector(prepared.width, prepared.height, MAX_FACES)
            val faces = arrayOfNulls<FaceDetector.Face>(MAX_FACES)
            val found = detector.findFaces(prepared, faces)
            return (0 until found).count { (faces[it]?.confidence() ?: 0f) >= FaceDetector.Face.CONFIDENCE_THRESHOLD }
        } finally {
            if (prepared !== decoded) decoded.recycle()
            prepared.recycle()
        }
    }

    /** FaceDetector needs an upright RGB_565 bitmap with an even width. */
    private fun prepare(source: Bitmap, rotationDegrees: Int): Bitmap {
        var bitmap = source
        if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            bitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }
        if (bitmap.width % 2 != 0) {
            val cropped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width - 1, bitmap.height)
            if (bitmap !== source) bitmap.recycle()
            bitmap = cropped
        }
        if (bitmap.config != Bitmap.Config.RGB_565) {
            val converted = bitmap.copy(Bitmap.Config.RGB_565, false)
            if (bitmap !== source) bitmap.recycle()
            bitmap = converted
        }
        return bitmap
    }
}
