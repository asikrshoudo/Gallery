package com.gallery.thekami.core.data.privatemedia

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.MediaType
import com.gallery.thekami.core.domain.media.restoreRelativePath
import com.gallery.thekami.core.domain.privatemedia.PrivateEntry
import com.gallery.thekami.core.domain.privatemedia.toMediaItem
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class PrivateError { CouldNotRead, NotEnoughSpace, VerificationFailed, Unsupported, Failed }

sealed interface PrivateResult {
    data class Success(val entry: PrivateEntry) : PrivateResult
    data class Failure(val error: PrivateError) : PrivateResult
}

private const val BUFFER_SIZE = 64 * 1024
private const val MIN_FREE_BYTES = 32L * 1024 * 1024

/**
 * Private media lives inside this app's own storage (filesDir). Other apps cannot read it, it is
 * not scanned by MediaStore, and with allowBackup=false it is not backed up. It is NOT encrypted
 * by Gallery (Android's own file-based encryption protects it while the device is locked), and
 * clearing the app's data or uninstalling the app permanently deletes it.
 *
 * A file is copied, then checked (size and SHA-256 against the source) before it is reported as
 * stored. Failed or cancelled copies remove their partial file. Nothing here ever touches the
 * original in MediaStore, and nothing is deleted except by an explicit call.
 */
class PrivateStore(context: Context) {
    private val appContext = context.applicationContext
    private val dir = File(appContext.filesDir, "private_media")
    private val indexFile = File(dir, "index.json")
    private val indexBackup = File(dir, "index.json.bak")
    private val mutex = Mutex()
    private val _entries = MutableStateFlow<List<PrivateEntry>>(emptyList())

    val entries: StateFlow<List<PrivateEntry>> = _entries.asStateFlow()

    suspend fun load() {
        withContext(Dispatchers.IO) { mutex.withLock { _entries.value = readIndex() } }
    }

    fun uriFor(entry: PrivateEntry): String = Uri.fromFile(File(dir, entry.fileName)).toString()

    fun toMediaItem(entry: PrivateEntry): MediaItem = entry.toMediaItem(uriFor(entry))

    fun entryForKey(key: String): PrivateEntry? = _entries.value.firstOrNull { uriFor(it) == key }

    suspend fun importMedia(item: MediaItem): PrivateResult = withContext(Dispatchers.IO) {
        dir.mkdirs()
        if (dir.usableSpace < (item.sizeBytes ?: 0L) + MIN_FREE_BYTES) {
            return@withContext PrivateResult.Failure(PrivateError.NotEnoughSpace)
        }
        val id = UUID.randomUUID().toString()
        val target = File(dir, "$id.${extensionOf(item)}")
        try {
            val sourceDigest = MessageDigest.getInstance("SHA-256")
            var copied = 0L
            val input = appContext.contentResolver.openInputStream(Uri.parse(item.uri))
                ?: return@withContext PrivateResult.Failure(PrivateError.CouldNotRead)
            input.use { source ->
                FileOutputStream(target).use { out ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        ensureActive()
                        val read = source.read(buffer)
                        if (read < 0) break
                        out.write(buffer, 0, read)
                        sourceDigest.update(buffer, 0, read)
                        copied += read
                    }
                    out.fd.sync()
                }
            }
            val expected = item.sizeBytes
            val sizeMismatch = expected != null && expected > 0 && expected != copied
            if (copied == 0L || target.length() != copied || sizeMismatch ||
                !MessageDigest.isEqual(sourceDigest.digest(), digestOf(target))
            ) {
                target.delete()
                return@withContext PrivateResult.Failure(PrivateError.VerificationFailed)
            }
            val entry = PrivateEntry(
                id = id,
                fileName = target.name,
                displayName = item.displayName,
                mimeType = item.mimeType,
                type = item.type,
                width = item.width,
                height = item.height,
                sizeBytes = copied,
                dateTakenMillis = item.dateTakenMillis,
                dateModifiedMillis = item.dateModifiedMillis,
                durationMillis = item.durationMillis,
                orientationDegrees = item.orientationDegrees,
                originalRelativePath = item.relativePath,
                importedAtMillis = System.currentTimeMillis(),
                trashedAtMillis = null,
            )
            mutex.withLock {
                val updated = _entries.value + entry
                writeIndex(updated)
                _entries.value = updated
            }
            PrivateResult.Success(entry)
        } catch (e: CancellationException) {
            target.delete()
            throw e
        } catch (e: IOException) {
            target.delete()
            PrivateResult.Failure(if (isNoSpace(e)) PrivateError.NotEnoughSpace else PrivateError.CouldNotRead)
        } catch (e: SecurityException) {
            target.delete()
            PrivateResult.Failure(PrivateError.CouldNotRead)
        }
    }

    suspend fun moveToHiddenTrash(id: String, nowMillis: Long) = update(id) { it.copy(trashedAtMillis = nowMillis) }

    suspend fun restoreFromHiddenTrash(ids: Collection<String>) {
        ids.forEach { id -> update(id) { it.copy(trashedAtMillis = null) } }
    }

    /** Permanently removes these private files. Call only after the user confirmed. */
    suspend fun deleteForever(ids: Collection<String>) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val doomed = _entries.value.filter { it.id in ids }
                val removed = doomed.filter { File(dir, it.fileName).let { f -> !f.exists() || f.delete() } }.map { it.id }.toSet()
                if (removed.isNotEmpty()) {
                    val updated = _entries.value.filter { it.id !in removed }
                    writeIndex(updated)
                    _entries.value = updated
                }
            }
        }
    }

    /**
     * Copies the item back into the gallery (MediaStore), checks the copy, and only then removes
     * the private copy. Needs Android 10+ (writing to shared storage without a storage
     * permission). Returns null on success.
     */
    suspend fun exportToGallery(id: String): PrivateError? = withContext(Dispatchers.IO) {
        val entry = _entries.value.firstOrNull { it.id == id } ?: return@withContext PrivateError.Failed
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@withContext PrivateError.Unsupported
        val file = File(dir, entry.fileName)
        if (!file.exists()) return@withContext PrivateError.Failed
        val resolver = appContext.contentResolver
        val collection = if (entry.type == MediaType.Image) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, entry.displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, entry.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, restoreRelativePath(entry.type, entry.originalRelativePath))
            put(MediaStore.MediaColumns.IS_PENDING, 1)
            entry.dateTakenMillis?.let { put(MediaStore.MediaColumns.DATE_TAKEN, it) }
        }
        val created = try { resolver.insert(collection, values) } catch (e: RuntimeException) { null }
            ?: return@withContext PrivateError.Failed
        try {
            val sourceDigest = MessageDigest.getInstance("SHA-256")
            val output = resolver.openOutputStream(created, "w") ?: throw IOException("No output stream")
            output.use { out ->
                FileInputStream(file).use { input ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        out.write(buffer, 0, read)
                        sourceDigest.update(buffer, 0, read)
                    }
                }
            }
            val copiedDigest = resolver.openInputStream(created)?.use { digestOf(it) }
            if (copiedDigest == null || !MessageDigest.isEqual(sourceDigest.digest(), copiedDigest)) {
                resolver.delete(created, null, null)
                return@withContext PrivateError.VerificationFailed
            }
            resolver.update(created, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        } catch (e: CancellationException) {
            resolver.delete(created, null, null)
            throw e
        } catch (e: Exception) {
            resolver.delete(created, null, null)
            return@withContext PrivateError.Failed
        }
        deleteForever(listOf(id))
        null
    }

    private suspend fun update(id: String, change: (PrivateEntry) -> PrivateEntry) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val updated = _entries.value.map { if (it.id == id) change(it) else it }
                writeIndex(updated)
                _entries.value = updated
            }
        }
    }

    private fun readIndex(): List<PrivateEntry> {
        for (file in listOf(indexFile, indexBackup)) {
            if (!file.exists()) continue
            try {
                val array = JSONArray(file.readText())
                return (0 until array.length())
                    .mapNotNull { runCatching { array.getJSONObject(it).toEntry() }.getOrNull() }
                    .filter { File(dir, it.fileName).exists() }
            } catch (e: Exception) {
                // Try the backup next; files are never deleted because an index could not be read.
            }
        }
        return emptyList()
    }

    private fun writeIndex(entries: List<PrivateEntry>) {
        dir.mkdirs()
        val temp = File(dir, "index.json.tmp")
        temp.writeText(JSONArray().apply { entries.forEach { put(it.toJson()) } }.toString())
        if (indexFile.exists()) indexFile.copyTo(indexBackup, overwrite = true)
        if (!temp.renameTo(indexFile)) throw IOException("Could not save the private index")
    }

    private fun extensionOf(item: MediaItem): String {
        val fromName = item.displayName.substringAfterLast('.', "").lowercase()
        if (fromName.isNotEmpty() && fromName.length <= 5 && fromName.all { it.isLetterOrDigit() }) return fromName
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(item.mimeType) ?: "bin"
    }

    private fun isNoSpace(e: IOException): Boolean =
        e.message.orEmpty().let { it.contains("ENOSPC") || it.contains("No space", ignoreCase = true) }

    private fun digestOf(file: File): ByteArray = FileInputStream(file).use { digestOf(it) }

    private fun digestOf(input: java.io.InputStream): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
        }
        return digest.digest()
    }
}

private fun PrivateEntry.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("file", fileName)
    put("name", displayName)
    put("mime", mimeType)
    put("type", type.name)
    width?.let { put("w", it) }
    height?.let { put("h", it) }
    put("size", sizeBytes)
    dateTakenMillis?.let { put("taken", it) }
    dateModifiedMillis?.let { put("modified", it) }
    durationMillis?.let { put("duration", it) }
    put("rotation", orientationDegrees)
    originalRelativePath?.let { put("path", it) }
    put("imported", importedAtMillis)
    trashedAtMillis?.let { put("trashed", it) }
}

private fun JSONObject.longOrNull(key: String): Long? = if (has(key) && !isNull(key)) getLong(key) else null
private fun JSONObject.intOrNull(key: String): Int? = if (has(key) && !isNull(key)) getInt(key) else null

private fun JSONObject.toEntry(): PrivateEntry = PrivateEntry(
    id = getString("id"),
    fileName = getString("file"),
    displayName = getString("name"),
    mimeType = getString("mime"),
    type = MediaType.valueOf(getString("type")),
    width = intOrNull("w"),
    height = intOrNull("h"),
    sizeBytes = getLong("size"),
    dateTakenMillis = longOrNull("taken"),
    dateModifiedMillis = longOrNull("modified"),
    durationMillis = longOrNull("duration"),
    orientationDegrees = optInt("rotation", 0),
    originalRelativePath = if (has("path") && !isNull("path")) getString("path") else null,
    importedAtMillis = getLong("imported"),
    trashedAtMillis = longOrNull("trashed"),
)
