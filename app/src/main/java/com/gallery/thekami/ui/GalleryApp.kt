package com.gallery.thekami.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.gallery.thekami.ui.home.HomeUiState
import com.gallery.thekami.ui.home.HomeViewModel
import com.gallery.thekami.ui.search.SearchScreen
import com.gallery.thekami.ui.viewer.ViewerScreen
import com.gallery.thekami.ui.viewer.ViewerSession
import com.gallery.thekami.ui.viewer.ViewerViewModel
import kotlinx.coroutines.delay

@Composable
fun GalleryApp(navBarStyle: NavBarStyle) {
    val context = LocalContext.current
    val home: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val viewer: ViewerViewModel = viewModel()
    val uiState by home.uiState.collectAsStateWithLifecycle()
    val homeSettings by home.homeSettings.collectAsStateWithLifecycle()
    val isRefreshing by home.isRefreshing.collectAsStateWithLifecycle()
    val viewerSession by viewer.session.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { home.onPermissionResult() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { home.onResume() }

    // Keep the last session around while the viewer fades out.
    var lastViewerSession by remember { mutableStateOf<ViewerSession?>(null) }
    LaunchedEffect(viewerSession) {
        if (viewerSession != null) {
            lastViewerSession = viewerSession
        } else {
            delay(350)
            lastViewerSession = null
        }
    }

    var nav by rememberSaveable(stateSaver = NavigationStateSaver) { mutableStateOf(NavigationState()) }
    val backTarget = nav.back()
    BackHandler(enabled = backTarget != null) { backTarget?.let { nav = it } }
    // Registered last, so it wins: Back closes the viewer first.
    BackHandler(enabled = viewerSession != null) { viewer.close() }

    val overlayOpen = nav.searchOpen || viewerSession != null

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            // Hide the shell from accessibility services while an overlay covers it.
            modifier = if (overlayOpen) Modifier.clearAndSetSemantics {} else Modifier,
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
                        uiState = uiState,
                        settings = homeSettings,
                        isRefreshing = isRefreshing,
                        onRequestAccess = {
                            permissionLauncher.launch(home.permissionsToRequest().toTypedArray())
                        },
                        onOpenSettings = { openAppSettings(context) },
                        onRefresh = home::refresh,
                        onSettingsChange = home::setHomeSettings,
                        onOpenMedia = { item ->
                            (uiState as? HomeUiState.Ready)?.let { viewer.open(it.sequence, item.key, it.livePairs) }
                        },
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
        AnimatedVisibility(
            visible = viewerSession != null,
            enter = fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.96f),
            exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f),
        ) {
            (viewerSession ?: lastViewerSession)?.let {
                ViewerScreen(session = it, viewModel = viewer, onMediaChanged = home::onMediaChanged)
            }
        }
    }
}
