package com.gallery.thekami.core.domain.media

/**
 * Folder to put a restored item back into. MediaStore only accepts DCIM/Pictures for images
 * and DCIM/Movies for videos, so the original folder is reused only when it is allowed;
 * otherwise a "Gallery" folder inside Pictures or Movies is used. Always ends with "/".
 */
fun restoreRelativePath(type: MediaType, original: String?): String {
    val roots = when (type) {
        MediaType.Image -> listOf("DCIM", "Pictures")
        MediaType.Video -> listOf("DCIM", "Movies")
    }
    val parts = original.orEmpty().trim().split('/').filter { it.isNotEmpty() && it != "." && it != ".." }
    if (parts.isNotEmpty() && roots.any { it.equals(parts.first(), ignoreCase = true) }) {
        return parts.joinToString("/") + "/"
    }
    return (if (type == MediaType.Image) "Pictures/Gallery" else "Movies/Gallery") + "/"
}
