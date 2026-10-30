package com.gallery.thekami.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.gallery.thekami.core.domain.navigation.NavigationState
import com.gallery.thekami.core.domain.navigation.TopLevelDestination
import com.gallery.thekami.core.domain.settings.NavBarStyle
import com.gallery.thekami.core.ui.navigation.GalleryBottomBar
import com.gallery.thekami.ui.collections.CollectionsScreen
import com.gallery.thekami.ui.home.HomeScreen
import com.gallery.thekami.ui.search.SearchScreen

@Composable
fun GalleryApp(navBarStyle: NavBarStyle) {
    var nav by rememberSaveable(stateSaver = NavigationStateSaver) { mutableStateOf(NavigationState()) }
    val backTarget = nav.back()
    BackHandler(enabled = backTarget != null) { backTarget?.let { nav = it } }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            // Hide the shell from accessibility services while the Search overlay covers it.
            modifier = if (nav.searchOpen) Modifier.clearAndSetSemantics {} else Modifier,
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                GalleryBottomBar(
                    selected = nav.selected,
                    showSearch = nav.showsBottomSearch,
                    style = navBarStyle,
                    onSelect = { nav = nav.select(it) },
                    onSearch = { nav = nav.openSearch() },
                )
            },
        ) { padding ->
            Crossfade(targetState = nav.selected, animationSpec = tween(150), label = "destination") { destination ->
                when (destination) {
                    TopLevelDestination.Home -> HomeScreen(padding)
                    TopLevelDestination.Collections -> CollectionsScreen(padding)
                }
            }
        }
        AnimatedVisibility(
            visible = nav.searchOpen,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
        ) {
            SearchScreen(onClose = { nav = nav.closeSearch() })
        }
    }
}
