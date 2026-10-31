package com.gallery.thekami.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.TimelineEntry
import com.gallery.thekami.core.ui.media.MediaThumbnail
import com.gallery.thekami.core.ui.theme.GalleryTheme
import java.time.LocalDate

/** Dense edge-to-edge grid. Cells are lazy, keyed by media URI, and recycled while scrolling. */
@Composable
fun MediaTimelineGrid(
    entries: List<TimelineEntry>,
    contentPadding: PaddingValues,
    onOpenMedia: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(88.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(
            items = entries,
            key = { it.key },
            span = { entry ->
                if (entry is TimelineEntry.Header) GridItemSpan(maxLineSpan) else GridItemSpan(1)
            },
            contentType = { if (it is TimelineEntry.Header) 0 else 1 },
        ) { entry ->
            when (entry) {
                is TimelineEntry.Header -> DateHeader(entry)
                is TimelineEntry.Media -> MediaThumbnail(
                    item = entry.item,
                    modifier = Modifier.aspectRatio(1f).clickable { onOpenMedia(entry.item) },
                )
            }
        }
    }
}

@Composable
private fun DateHeader(header: TimelineEntry.Header) {
    val spacing = GalleryTheme.spacing
    val today = stringResource(R.string.timeline_today)
    val yesterday = stringResource(R.string.timeline_yesterday)
    val noDate = stringResource(R.string.timeline_no_date)
    val locale = LocalConfiguration.current.locales[0]
    val label = remember(header.grouping, header.start, locale, today) {
        timelineHeaderLabel(header.grouping, header.start, LocalDate.now(), locale, today, yesterday, noDate)
    }
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier
            .semantics { heading() }
            .padding(start = spacing.lg, end = spacing.lg, top = spacing.md, bottom = spacing.xs),
    )
}
