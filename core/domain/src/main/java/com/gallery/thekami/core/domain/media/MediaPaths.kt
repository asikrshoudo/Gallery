package com.gallery.thekami.core.domain.media

object MediaPaths {
    private val storageRoot =
        Regex("^/(?:storage/(?:emulated/\\d+|[^/]+)|sdcard|mnt/sdcard)/(.*)$")

    /**
     * Android 9 and below only expose absolute file paths. Converts one to the same
     * "relative directory" form newer Android reports, e.g. "DCIM/Camera/".
     */
    fun relativeDirFromDataPath(dataPath: String?): String? {
        val match = storageRoot.matchEntire(dataPath ?: return null) ?: return null
        val rest = match.groupValues[1]
        val slash = rest.lastIndexOf('/')
        return if (slash < 0) "" else rest.substring(0, slash + 1)
    }
}
