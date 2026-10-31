package com.gallery.thekami.ui

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/** Plural string with the count as its only argument, e.g. "1 item" / "3 items". */
@Composable
fun quantityString(@PluralsRes id: Int, count: Int): String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(id, count, configuration) { context.resources.getQuantityString(id, count, count) }
}
