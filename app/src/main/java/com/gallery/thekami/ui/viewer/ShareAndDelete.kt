package com.gallery.thekami.ui.viewer

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.gallery.thekami.core.domain.media.MediaItem

/** Shares the original file through the system chooser. Nothing is uploaded by the app. */
fun shareMedia(context: Context, item: MediaItem, chooserTitle: String) {
    val uri = Uri.parse(item.uri)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = item.mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri(item.displayName, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        // No app can handle the share; nothing to do.
    }
}

val canTrashFromViewer: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

/**
 * Android 11+: asks the system to move the item to Trash. Android shows its own confirmation
 * and nothing happens unless the user accepts. Returns null if the request can't be created.
 */
fun createTrashRequest(context: Context, item: MediaItem): IntentSender? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
    return try {
        MediaStore.createTrashRequest(context.contentResolver, listOf(Uri.parse(item.uri)), true).intentSender
    } catch (e: RuntimeException) {
        null
    }
}
