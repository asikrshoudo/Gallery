package com.gallery.thekami.ui

import android.app.Activity
import android.app.KeyguardManager
import android.content.IntentSender
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import coil3.SingletonImageLoader
import com.gallery.thekami.GalleryApplication
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.navigation.CollectionsRoute
import com.gallery.thekami.core.domain.navigation.NavigationState
import com.gallery.thekami.core.domain.navigation.TopLevelDestination
import com.gallery.thekami.core.domain.settings.NavBarStyle
import com.gallery.thekami.core.ui.media.LocalMediaMarks
import com.gallery.thekami.core.ui.navigation.GalleryBottomBar
import com.gallery.thekami.ui.collections.CollectionsActions
import com.gallery.thekami.ui.collections.CollectionsHost
import com.gallery.thekami.ui.collections.CollectionsState
import com.gallery.thekami.ui.collections.CollectionsViewModel
import com.gallery.thekami.ui.collections.PrivateAreaUi
import com.gallery.thekami.ui.collections.TrashUi
import com.gallery.thekami.ui.home.HomeScreen
import com.gallery.thekami.ui.home.HomeUiState
import com.gallery.thekami.ui.home.HomeViewModel
import com.gallery.thekami.ui.search.SearchScreen
import com.gallery.thekami.ui.search.SearchViewModel
import com.gallery.thekami.ui.privatemedia.PrivateOperationDialogs
import com.gallery.thekami.ui.privatemedia.PrivateMessage
import com.gallery.thekami.ui.privatemedia.PrivateViewModel
import com.gallery.thekami.ui.settings.SettingsScreen
import com.gallery.thekami.ui.settings.SettingsViewModel
import com.gallery.thekami.ui.trash.TrashViewModel
import com.gallery.thekami.ui.viewer.PrivateViewerActions
import com.gallery.thekami.ui.viewer.ViewerScreen
import com.gallery.thekami.ui.viewer.ViewerSession
import com.gallery.thekami.ui.viewer.ViewerViewModel
import com.gallery.thekami.ui.viewer.deleteRequest
import com.gallery.thekami.ui.viewer.trashRequest
import kotlinx.coroutines.delay

@Composable
fun GalleryApp(navBarStyle: NavBarStyle) {
    val context = LocalContext.current
    val home: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val collections: CollectionsViewModel = viewModel(factory = CollectionsViewModel.Factory)
    val viewer: ViewerViewModel = viewModel()
    val marksModel: MarksViewModel = viewModel(factory = MarksViewModel.Factory)
    val search: SearchViewModel = viewModel(factory = SearchViewModel.Factory)
    val settingsModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val privateVm: PrivateViewModel = viewModel(factory = PrivateViewModel.Factory)
    val trashVm: TrashViewModel = viewModel(factory = TrashViewModel.Factory)
    val uiState by home.uiState.collectAsStateWithLifecycle()
    val isRefreshing by home.isRefreshing.collectAsStateWithLifecycle()
    val collectionsState by collections.state.collectAsStateWithLifecycle()
    val albums by collections.albums.collectAsStateWithLifecycle()
    val people by collections.people.collectAsStateWithLifecycle()
    val faceCounts by collections.faceCounts.collectAsStateWithLifecycle()
    val marks by marksModel.marks.collectAsStateWithLifecycle()
    val viewerSession by viewer.session.collectAsStateWithLifecycle()
    val privateUnlocked by privateVm.unlocked.collectAsStateWithLifecycle()
    val privateItems by privateVm.active.collectAsStateWithLifecycle()
    val privateTrashed by privateVm.trashed.collectAsStateWithLifecycle()
    val moveState by privateVm.moveState.collectAsStateWithLifecycle()
    val privateMessage by privateVm.message.collectAsStateWithLifecycle()
    val trashState by trashVm.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
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

    // Private locks as soon as the app leaves the foreground (not on rotation) and image caches are
    // cleared so no private thumbnail stays in memory.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        if (activity?.isChangingConfigurations != true && privateVm.unlocked.value) {
            privateVm.lock()
            SingletonImageLoader.get(context).memoryCache?.clear()
            (context.applicationContext as GalleryApplication).viewerImageLoader.memoryCache?.clear()
        }
    }
    LaunchedEffect(privateUnlocked, viewerSession?.isPrivate) {
        if (!privateUnlocked && viewerSession?.isPrivate == true) viewer.close()
    }

    val keyguard = remember { context.getSystemService(KeyguardManager::class.java) }
    val deviceSecure = keyguard?.isDeviceSecure == true
    val credentialLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) privateVm.unlock()
    }
    val unlockTitle = stringResource(R.string.private_unlock_prompt_title)
    val unlockDescription = stringResource(R.string.private_unlock_prompt_desc)
    val unlock: () -> Unit = {
        keyguard?.createConfirmDeviceCredentialIntent(unlockTitle, unlockDescription)?.let { credentialLauncher.launch(it) }
    }

    // One launcher for every Android confirmation (restore, delete forever, delete original).
    var pendingSystem by remember { mutableStateOf<PendingSystemAction?>(null) }
    val systemLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        val action = pendingSystem
        pendingSystem = null
        if (result.resultCode == Activity.RESULT_OK) action?.onOk?.invoke() else action?.onCancel?.invoke()
    }
    val launchSystem: (IntentSender?, () -> Unit, () -> Unit) -> Unit = { sender, onOk, onCancel ->
        if (sender == null) {
            Toast.makeText(context, R.string.system_request_failed, Toast.LENGTH_SHORT).show()
        } else {
            pendingSystem = PendingSystemAction(onOk, onCancel)
            systemLauncher.launch(IntentSenderRequest.Builder(sender).build())
        }
    }

    var nav by rememberSaveable(stateSaver = NavigationStateSaver) { mutableStateOf(NavigationState()) }
    val backTarget = nav.back()
    BackHandler(enabled = backTarget != null) { backTarget?.let { nav = it } }
    // Registered last, so it wins: Back closes the viewer first.
    BackHandler(enabled = viewerSession != null) { viewer.close() }

    val route = nav.collectionsRoute
    val showingPrivate = privateUnlocked &&
        (route == CollectionsRoute.Private || route == CollectionsRoute.HiddenTrash || viewerSession?.isPrivate == true)
    DisposableEffect(showingPrivate, activity) {
        val window = activity?.window
        if (showingPrivate) window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
    LaunchedEffect(route) { if (route == CollectionsRoute.Trash) trashVm.refresh() }

    val overlayOpen = nav.searchOpen || nav.settingsOpen || viewerSession != null
    val requestAccess = { permissionLauncher.launch(home.permissionsToRequest().toTypedArray()) }

    val collectionsActions = CollectionsActions(
        push = { route -> nav = nav.push(route) },
        pop = { nav = nav.pop() },
        openSearch = { nav = nav.openSearch() },
        openGallerySettings = { nav = nav.openSettings() },
        openMedia = { sequence, item ->
            val pairs = (collectionsState as? CollectionsState.Ready)?.livePairs ?: emptyMap()
            viewer.open(sequence, item.key, pairs)
        },
        requestAccess = { requestAccess() },
        openSettings = { openAppSettings(context) },
        refresh = home::refresh,
        createAlbum = { name -> collections.createAlbum(name) },
        renameAlbum = { id, name -> collections.renameAlbum(id, name) },
        deleteAlbum = { id -> collections.deleteAlbum(id) },
        addMedia = { id, keys -> collections.addMedia(id, keys) },
        removeMedia = { id, keys -> collections.removeMedia(id, keys) },
        setCover = { id, key -> collections.setCover(id, key) },
        createPerson = { name, relationship -> collections.createPerson(name, relationship) },
        editPerson = { id, name, relationship -> collections.editPerson(id, name, relationship) },
        deletePerson = { id -> collections.deletePerson(id) },
        mergePerson = { targetId, sourceId -> collections.mergePerson(targetId, sourceId) },
        addPersonMedia = { id, keys -> collections.addPersonMedia(id, keys) },
        removePersonMedia = { id, keys -> collections.removePersonMedia(id, keys) },
        setPersonCover = { id, key -> collections.setPersonCover(id, key) },
    )

    val privateArea = PrivateAreaUi(
        unlocked = privateUnlocked,
        deviceSecure = deviceSecure,
        items = privateItems,
        trashed = privateTrashed,
        nowMillis = System.currentTimeMillis(),
        unlock = unlock,
        openMedia = { sequence, item -> viewer.open(sequence, item.key, emptyMap(), isPrivate = true) },
        restore = privateVm::restoreFromHiddenTrash,
        deleteForever = privateVm::deleteForever,
    )
    val trashUi = TrashUi(
        state = trashState,
        refresh = trashVm::refresh,
        restore = { items ->
            launchSystem(
                trashRequest(context, items.map { it.item }, trash = false),
                { trashVm.refresh(); home.onMediaChanged() },
                {},
            )
        },
        deleteForever = { items ->
            launchSystem(
                deleteRequest(context, items.map { it.item }),
                { trashVm.refresh(); home.onMediaChanged() },
                {},
            )
        },
    )
    val privateViewerActions = PrivateViewerActions(
        moveToPrivate = { item -> privateVm.moveToPrivate(item) },
        moveOut = { item -> privateVm.moveOut(item) { viewer.removeItem(item.key); home.onMediaChanged() } },
        deleteFromPrivate = { item ->
            privateVm.trashPrivate(item)
            viewer.removeItem(item.key)
        },
    )

    CompositionLocalProvider(LocalMediaMarks provides marks) {
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
                            isRefreshing = isRefreshing,
                            onRequestAccess = { requestAccess() },
                            onOpenSettings = { openAppSettings(context) },
                            onRefresh = home::refresh,
                            onOpenGallerySettings = { nav = nav.openSettings() },
                            onOpenMedia = { item ->
                                (uiState as? HomeUiState.Ready)?.let { viewer.open(it.sequence, item.key, it.livePairs) }
                            },
                        )
                        TopLevelDestination.Collections -> CollectionsHost(
                            route = nav.collectionsRoute,
                            depth = nav.collectionsStack.size,
                            state = collectionsState,
                            faceCounts = faceCounts,
                            privateArea = privateArea,
                            trash = trashUi,
                            contentPadding = padding,
                            actions = collectionsActions,
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = nav.searchOpen,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(150)),
            ) {
                SearchScreen(
                    viewModel = search,
                    onClose = { nav = nav.closeSearch() },
                    onOpenMedia = { sequence, item, pairs -> viewer.open(sequence, item.key, pairs) },
                    onRequestAccess = { requestAccess() },
                    onOpenSettings = { openAppSettings(context) },
                    onRefresh = home::refresh,
                    // The viewer opens on top of Search; hide Search from accessibility while it does.
                    modifier = if (viewerSession != null) Modifier.clearAndSetSemantics {} else Modifier,
                )
            }
            AnimatedVisibility(
                visible = nav.settingsOpen,
                enter = slideInHorizontally(tween(300)) { it } + fadeIn(tween(200)),
                exit = slideOutHorizontally(tween(260)) { it } + fadeOut(tween(200)),
            ) {
                SettingsScreen(viewModel = settingsModel, onClose = { nav = nav.closeSettings() })
            }
            PrivateOperationDialogs(
                moveState = moveState,
                message = privateMessage,
                onCancelCopy = privateVm::cancelMove,
                onDeleteOriginal = { original ->
                    privateVm.dismissMove()
                    launchSystem(
                        deleteRequest(context, listOf(original)),
                        { viewer.removeItem(original.key); home.onMediaChanged() },
                        { privateVm.showMessage(PrivateMessage.OriginalKept) },
                    )
                },
                onKeepOriginal = {
                    privateVm.dismissMove()
                    privateVm.showMessage(PrivateMessage.OriginalKept)
                },
                onDismissMessage = privateVm::dismissMessage,
            )
            AnimatedVisibility(
                visible = viewerSession != null,
                enter = fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.96f),
                exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f),
            ) {
                (viewerSession ?: lastViewerSession)?.let {
                    ViewerScreen(
                        session = it,
                        viewModel = viewer,
                        albums = albums,
                        people = people,
                        marks = marks,
                        onToggleFavourite = marksModel::toggleFavourite,
                        onTogglePinned = marksModel::togglePinned,
                        onToggleAlbum = { albumId, key, add -> collections.toggleMedia(albumId, key, add) },
                        onCreateAlbum = { name, key -> collections.createAlbum(name, key) },
                        onTogglePerson = { personId, key, add -> collections.togglePerson(personId, key, add) },
                        onCreatePerson = { name, relationship, key -> collections.createPerson(name, relationship, key) },
                        privateActions = privateViewerActions,
                        onMediaChanged = home::onMediaChanged,
                    )
                }
            }
        }
    }
}

private class PendingSystemAction(val onOk: () -> Unit, val onCancel: () -> Unit)
