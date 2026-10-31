package com.gallery.thekami.core.domain.search

import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.distinctMedia
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * A source of search matches. Today there is only [MetadataSearchProvider]. A future People or
 * content provider can be added to [SearchEngine] without changing callers; until such a
 * provider really exists, nothing in the app claims to search people or picture content.
 */
interface MediaSearchProvider {
    val id: String

    /** Keys of the media that match [query]. */
    fun search(query: SearchQuery): Set<String>
}

/** File name, folder and date, computed from MediaStore metadata only. Fully offline. */
class MetadataSearchProvider(
    items: Collection<MediaItem>,
    zone: ZoneId = ZoneId.systemDefault(),
) : MediaSearchProvider {
    override val id: String = "metadata"

    private class Entry(val key: String, val haystack: String, val date: LocalDate?)

    private val entries: List<Entry> = items.map { item ->
        val folder = listOfNotNull(item.bucketName, item.relativePath).joinToString(" ")
        Entry(
            key = item.key,
            haystack = "${item.displayName} $folder".lowercase(),
            date = item.effectiveDateMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() },
        )
    }

    override fun search(query: SearchQuery): Set<String> {
        if (query.isBlank) return emptySet()
        val matches = HashSet<String>()
        for (entry in entries) {
            if (query.terms.all { termMatches(it, entry) }) matches += entry.key
        }
        return matches
    }

    private fun termMatches(term: SearchTerm, entry: Entry): Boolean {
        val date = entry.date
        val dateHit = term.date != null && date != null && term.date.matches(date)
        if (term.dateOnly) return dateHit
        return dateHit || entry.haystack.contains(term.text)
    }
}

/** Combines providers. Results keep the order of [items], so pass the library newest first. */
class SearchEngine(items: List<MediaItem>, private val providers: List<MediaSearchProvider>) {
    private val ordered = distinctMedia(items)

    fun search(query: SearchQuery): List<MediaItem> {
        if (query.isBlank) return emptyList()
        val keys = HashSet<String>()
        providers.forEach { keys += it.search(query) }
        if (keys.isEmpty()) return emptyList()
        return ordered.filter { it.key in keys }
    }
}
