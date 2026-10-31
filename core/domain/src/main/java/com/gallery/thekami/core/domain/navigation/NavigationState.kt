package com.gallery.thekami.core.domain.navigation

/**
 * Immutable shell navigation state. Bottom Search visibility is derived, never stored.
 * [collectionsStack] holds screens opened inside Collections (empty = the Collections root).
 */
data class NavigationState(
    val selected: TopLevelDestination = TopLevelDestination.Home,
    val searchOpen: Boolean = false,
    val collectionsStack: List<CollectionsRoute> = emptyList(),
) {
    val showsBottomSearch: Boolean
        get() = !searchOpen && selected.hasBottomSearch

    /** The Collections screen to show, or null for the Collections root or other destinations. */
    val collectionsRoute: CollectionsRoute?
        get() = if (selected == TopLevelDestination.Collections) collectionsStack.lastOrNull() else null

    /** Selecting the already-selected Collections tab returns to its root. */
    fun select(destination: TopLevelDestination): NavigationState =
        if (destination == selected && destination == TopLevelDestination.Collections) {
            copy(searchOpen = false, collectionsStack = emptyList())
        } else {
            copy(selected = destination, searchOpen = false)
        }

    /** Search can be opened from anywhere: bottom action on Home, search entry in Collections. */
    fun openSearch(): NavigationState = copy(searchOpen = true)

    fun closeSearch(): NavigationState = copy(searchOpen = false)

    fun push(route: CollectionsRoute): NavigationState =
        copy(selected = TopLevelDestination.Collections, searchOpen = false, collectionsStack = collectionsStack + route)

    fun pop(): NavigationState = copy(collectionsStack = collectionsStack.dropLast(1))

    /** State after a system Back press, or null when Back should leave the app. */
    fun back(): NavigationState? = when {
        searchOpen -> closeSearch()
        selected == TopLevelDestination.Collections && collectionsStack.isNotEmpty() -> pop()
        selected != TopLevelDestination.Home -> select(TopLevelDestination.Home)
        else -> null
    }
}
