package com.gallery.thekami.core.data.faces

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * One line per checked photo: "<media key><TAB><face count>". Only counts are stored, no face
 * data. A plain append-only file keeps large libraries cheap; unreadable lines are skipped.
 */
internal class FaceIndexStore(context: Context) {
    private val file = File(context.applicationContext.filesDir, "face_scan_index.tsv")

    suspend fun load(): Map<String, Int> = withContext(Dispatchers.IO) {
        val map = HashMap<String, Int>()
        if (file.exists()) {
            file.forEachLine { line ->
                val tab = line.lastIndexOf('\t')
                if (tab > 0) line.substring(tab + 1).toIntOrNull()?.let { map[line.substring(0, tab)] = it }
            }
        }
        map
    }

    suspend fun append(entries: Map<String, Int>) {
        if (entries.isEmpty()) return
        withContext(Dispatchers.IO) {
            file.appendText(entries.entries.joinToString(separator = "") { "${it.key}\t${it.value}\n" })
        }
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) { file.delete() }
    }
}
