package com.gallery.thekami.core.domain.text

/** Trims, collapses whitespace and limits length. Returns null when nothing is left. */
fun normalizeLabel(raw: String, maxLength: Int): String? {
    val cleaned = raw.trim().replace(Regex("\\s+"), " ").take(maxLength).trim()
    return cleaned.ifEmpty { null }
}
