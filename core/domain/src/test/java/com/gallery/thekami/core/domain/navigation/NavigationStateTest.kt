package com.gallery.thekami.core.domain.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationStateTest {
    @Test
    fun startsOnHomeWithBottomSearch() {
        val state = NavigationState()
        assertEquals(TopLevelDestination.Home, state.selected)
        assertTrue(state.showsBottomSearch)
    }

    @Test
    fun collectionsHidesBottomSearch() {
        assertFalse(NavigationState().select(TopLevelDestination.Collections).showsBottomSearch)
    }

    @Test
    fun returningHomeShowsBottomSearchAgain() {
        val state = NavigationState().select(TopLevelDestination.Collections).select(TopLevelDestination.Home)
        assertTrue(state.showsBottomSearch)
    }

    @Test
    fun searchCannotOpenFromCollections() {
        val state = NavigationState().select(TopLevelDestination.Collections).openSearch()
        assertFalse(state.searchOpen)
    }

    @Test
    fun openSearchHidesBottomSearchAndBackClosesIt() {
        val open = NavigationState().openSearch()
        assertTrue(open.searchOpen)
        assertFalse(open.showsBottomSearch)
        assertEquals(NavigationState(), open.back())
    }

    @Test
    fun backFromCollectionsGoesHomeAndBackFromHomeLeaves() {
        val collections = NavigationState().select(TopLevelDestination.Collections)
        assertEquals(NavigationState(), collections.back())
        assertNull(NavigationState().back())
    }
}
