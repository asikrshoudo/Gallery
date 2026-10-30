package com.gallery.thekami.ui

import androidx.compose.runtime.saveable.listSaver
import com.gallery.thekami.core.domain.navigation.NavigationState
import com.gallery.thekami.core.domain.navigation.TopLevelDestination

val NavigationStateSaver = listSaver<NavigationState, Any>(
    save = { listOf(it.selected.name, it.searchOpen) },
    restore = { saved ->
        NavigationState(
            selected = TopLevelDestination.entries.firstOrNull { it.name == saved[0] }
                ?: TopLevelDestination.Home,
            searchOpen = saved[1] as Boolean,
        )
    },
)
