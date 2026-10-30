package com.gallery.thekami.core.domain.navigation

/** Immutable shell navigation state. Bottom Search visibility is derived, never stored. */
data class NavigationState(
    val selected: TopLevelDestination = TopLevelDestination.Home,
    val searchOpen: Boolean = false,
) {
    val showsBottomSearch: Boolean
        get() = !searchOpen && selected.hasBottomSearch

    fun select(destination: TopLevelDestination): NavigationState =
        copy(selected = destination, searchOpen = false)

    fun openSearch(): NavigationState =
        if (selected.hasBottomSearch) copy(searchOpen = true) else this

    fun closeSearch(): NavigationState = copy(searchOpen = false)

    /** State after a system Back press, or null when Back should leave the app. */
    fun back(): NavigationState? = when {
        searchOpen -> closeSearch()
        selected != TopLevelDestination.Home -> select(TopLevelDestination.Home)
        else -> null
    }
}
