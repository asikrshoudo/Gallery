package com.gallery.thekami.core.domain.search

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** A date a search word can stand for. */
sealed interface DateTerm {
    fun matches(date: LocalDate): Boolean

    data class Year(val year: Int) : DateTerm {
        override fun matches(date: LocalDate) = date.year == year
    }

    data class Month(val month: Int) : DateTerm {
        override fun matches(date: LocalDate) = date.monthValue == month
    }

    data class YearMonth(val year: Int, val month: Int) : DateTerm {
        override fun matches(date: LocalDate) = date.year == year && date.monthValue == month
    }

    data class Day(val day: LocalDate) : DateTerm {
        override fun matches(date: LocalDate) = date == day
    }

    data class DayOfMonth(val day: Int) : DateTerm {
        override fun matches(date: LocalDate) = date.dayOfMonth == day
    }
}

/**
 * One search word. It matches an item when the word appears in the file name or folder, or
 * when [date] fits the item's date. [dateOnly] words (a bare day number next to a month name)
 * match by date only, so "15 march" does not match every file with "15" in its name.
 */
data class SearchTerm(val text: String, val date: DateTerm? = null, val dateOnly: Boolean = false)

/** Every term must match (AND). A blank query has no terms and matches nothing. */
data class SearchQuery(val raw: String, val terms: List<SearchTerm>) {
    val isBlank: Boolean get() = terms.isEmpty()
}

private val IsoDay = Regex("""(\d{4})-(\d{1,2})-(\d{1,2})""")
private val IsoMonth = Regex("""(\d{4})-(\d{1,2})""")

private fun monthNames(locale: Locale): Map<String, Int> {
    val names = HashMap<String, Int>()
    val styles = listOf(TextStyle.FULL, TextStyle.SHORT, TextStyle.FULL_STANDALONE, TextStyle.SHORT_STANDALONE)
    for (language in listOf(Locale.ENGLISH, locale)) {
        for (month in java.time.Month.values()) {
            for (style in styles) {
                val name = month.getDisplayName(style, language).lowercase().trimEnd('.')
                if (name.length >= 3) names.putIfAbsent(name, month.value)
            }
        }
    }
    return names
}

/** Understands English month names plus the names of [locale]. [today] resolves "today"/"yesterday". */
fun parseSearchQuery(raw: String, today: LocalDate, locale: Locale): SearchQuery {
    val tokens = raw.lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }.distinct()
    if (tokens.isEmpty()) return SearchQuery(raw, emptyList())
    val months = monthNames(locale)
    val hasMonth = tokens.any { it in months }
    return SearchQuery(raw, tokens.map { toTerm(it, today, months, hasMonth) })
}

private fun toTerm(token: String, today: LocalDate, months: Map<String, Int>, hasMonth: Boolean): SearchTerm {
    IsoDay.matchEntire(token)?.let { match ->
        val (y, m, d) = match.destructured
        val date = runCatching { LocalDate.of(y.toInt(), m.toInt(), d.toInt()) }.getOrNull()
        if (date != null) return SearchTerm(token, DateTerm.Day(date))
    }
    IsoMonth.matchEntire(token)?.let { match ->
        val (y, m) = match.destructured
        val ok = runCatching { YearMonth.of(y.toInt(), m.toInt()) }.isSuccess
        if (ok) return SearchTerm(token, DateTerm.YearMonth(y.toInt(), m.toInt()))
    }
    if (token.length == 4 && token.all { it.isDigit() }) {
        val year = token.toInt()
        if (year in 1970..2100) return SearchTerm(token, DateTerm.Year(year))
    }
    months[token]?.let { return SearchTerm(token, DateTerm.Month(it)) }
    if (token == "today") return SearchTerm(token, DateTerm.Day(today))
    if (token == "yesterday") return SearchTerm(token, DateTerm.Day(today.minusDays(1)))
    if (hasMonth && token.length <= 2 && token.all { it.isDigit() }) {
        val day = token.toInt()
        if (day in 1..31) return SearchTerm(token, DateTerm.DayOfMonth(day), dateOnly = true)
    }
    return SearchTerm(token)
}
