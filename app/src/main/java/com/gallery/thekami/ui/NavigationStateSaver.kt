package com.gallery.thekami.ui

import androidx.compose.runtime.saveable.listSaver
import com.gallery.thekami.core.domain.navigation.CollectionsRoute
import com.gallery.thekami.core.domain.navigation.NavigationState
import com.gallery.thekami.core.domain.navigation.TopLevelDestination

val NavigationStateSaver = listSaver<NavigationState, Any>(
    save = { state ->
        listOf(
            state.selected.name,
            state.searchOpen,
            ArrayList(state.collectionsStack.map { it.encode() }),
        )
    },
    restore = { saved ->
        val stack = (saved.getOrNull(2) as? List<*>).orEmpty()
            .mapNotNull { (it as? String)?.let(CollectionsRoute::decode) }
        NavigationState(
            selected = TopLevelDestination.entries.firstOrNull { it.name == saved[0] }
                ?: TopLevelDestination.Home,
            searchOpen = saved[1] as Boolean,
            collectionsStack = stack,
        )
    },
)
