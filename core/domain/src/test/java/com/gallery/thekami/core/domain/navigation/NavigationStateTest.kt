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
    fun searchOpensFromCollectionsAndBackReturnsThere() {
        val collections = NavigationState().select(TopLevelDestination.Collections)
        val open = collections.openSearch()
        assertTrue(open.searchOpen)
        assertFalse(open.showsBottomSearch)
        assertEquals(collections, open.back())
    }

    @Test
    fun openSearchHidesBottomSearchAndBackClosesIt() {
        val open = NavigationState().openSearch()
        assertFalse(open.showsBottomSearch)
        assertEquals(NavigationState(), open.back())
    }

    @Test
    fun backFromCollectionsRootGoesHomeAndBackFromHomeLeaves() {
        val collections = NavigationState().select(TopLevelDestination.Collections)
        assertEquals(NavigationState(), collections.back())
        assertNull(NavigationState().back())
    }

    @Test
    fun pushingARouteSwitchesToCollectionsAndBackPopsIt() {
        val albums = NavigationState().push(CollectionsRoute.Albums)
        assertEquals(TopLevelDestination.Collections, albums.selected)
        assertEquals(CollectionsRoute.Albums, albums.collectionsRoute)
        val detail = albums.push(CollectionsRoute.AlbumDetail("a1"))
        assertEquals(CollectionsRoute.AlbumDetail("a1"), detail.collectionsRoute)
        assertEquals(albums, detail.back())
        assertEquals(TopLevelDestination.Collections, albums.back()?.selected)
        assertNull(albums.back()?.collectionsRoute)
    }

    @Test
    fun reselectingCollectionsReturnsToItsRoot() {
        val deep = NavigationState().push(CollectionsRoute.Folders).push(CollectionsRoute.OtherFolders)
        assertTrue(deep.select(TopLevelDestination.Collections).collectionsStack.isEmpty())
    }

    @Test
    fun leavingAndReturningKeepsTheCollectionsStack() {
        val state = NavigationState().push(CollectionsRoute.Albums).select(TopLevelDestination.Home)
        assertNull(state.collectionsRoute)
        assertEquals(CollectionsRoute.Albums, state.select(TopLevelDestination.Collections).collectionsRoute)
    }
}
