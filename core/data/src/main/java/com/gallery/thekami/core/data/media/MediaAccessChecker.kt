package com.gallery.thekami.core.data.media

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.gallery.thekami.core.domain.media.MediaAccess
import com.gallery.thekami.core.domain.media.MediaPermissionPolicy

internal class MediaAccessChecker(private val context: Context) {
    fun currentAccess(): MediaAccess {
        val granted = MediaPermissionPolicy.allPermissions
            .filter { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
            .toSet()
        return MediaPermissionPolicy.resolveAccess(Build.VERSION.SDK_INT, granted)
    }

    fun permissionsToRequest(): List<String> =
        MediaPermissionPolicy.permissionsToRequest(Build.VERSION.SDK_INT)
}
