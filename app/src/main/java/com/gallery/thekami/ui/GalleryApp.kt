package com.gallery.thekami.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gallery.thekami.core.domain.navigation.NavigationState
import com.gallery.thekami.core.domain.navigation.TopLevelDestination
import com.gallery.thekami.core.domain.settings.NavBarStyle
import com.gallery.thekami.core.ui.navigation.GalleryBottomBar
import com.gallery.thekami.ui.collections.CollectionsScreen
import com.gallery.thekami.ui.home.HomeScreen
import com.gallery.thekami.ui.search.SearchScreen

@Composable
fun GalleryApp(navBarStyle: NavBarStyle) {
    val context = LocalContext.current
    val media: MediaViewModel = viewModel(factory = MediaViewModel.Factory)
    val mediaState by media.state.collectAsStateWithLifecycle()
    val isRefreshing by media.isRefreshing.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { media.onPermissionResult() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { media.onResume() }

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
                    TopLevelDestination.Home -> HomeScreen(
                        contentPadding = padding,
                        state = mediaState,
                        isRefreshing = isRefreshing,
                        onRequestAccess = {
                            permissionLauncher.launch(media.permissionsToRequest().toTypedArray())
                        },
                        onOpenSettings = { openAppSettings(context) },
                        onRefresh = media::refresh,
                    )
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
