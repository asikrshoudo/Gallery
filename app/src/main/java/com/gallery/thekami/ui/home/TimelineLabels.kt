package com.gallery.thekami.ui.home

import com.gallery.thekami.core.domain.media.MediaGrouping
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun timelineHeaderLabel(
    grouping: MediaGrouping,
    start: LocalDate?,
    today: LocalDate,
    locale: Locale,
    todayLabel: String,
    yesterdayLabel: String,
    noDateLabel: String,
): String {
    if (start == null) return noDateLabel
    return when (grouping) {
        MediaGrouping.Day -> when {
            start == today -> todayLabel
            start == today.minusDays(1) -> yesterdayLabel
            start.year == today.year -> start.format(DateTimeFormatter.ofPattern("EEE, d MMM", locale))
            else -> start.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
        }
        MediaGrouping.Month -> start.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
        MediaGrouping.Year -> start.format(DateTimeFormatter.ofPattern("yyyy", locale))
    }
}
