@file:OptIn(UnstableApi::class)

package com.gallery.thekami.ui.viewer

import android.app.Activity
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.gallery.thekami.GalleryApplication
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.album.Album
import com.gallery.thekami.core.domain.media.MediaItem
import kotlin.math.abs
import kotlinx.coroutines.launch

private enum class ViewerDialog { Info, AddToAlbum, ConfirmDelete, DeleteUnsupported, DeleteFailed }

/**
 * Full-screen viewer over [session].items (the sequence it was opened from). The pager, the
 * filmstrip and the playback target are all derived from the pager's current page, so they
 * cannot disagree. Controls hide on tap, and stay visible when TalkBack-style touch
 * exploration is on.
 */
@Composable
fun ViewerScreen(
    session: ViewerSession,
    viewModel: ViewerViewModel,
    albums: List<Album>,
    onToggleAlbum: (albumId: String, key: String, add: Boolean) -> Unit,
    onCreateAlbum: (name: String, key: String) -> Unit,
    onMediaChanged: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = session.items
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val playback = viewModel.playback
    val playerState by playback.state.collectAsStateWithLifecycle()
    val imageLoader = remember { (context.applicationContext as GalleryApplication).viewerImageLoader }

    val pagerState = rememberPagerState(
        initialPage = viewModel.currentIndex.coerceIn(0, maxOf(items.lastIndex, 0)),
        pageCount = { items.size },
    )
    val currentIndex = pagerState.currentPage
    val current = items.getOrNull(currentIndex)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { viewModel.onPageChanged(it) }
    }

    var controlsVisible by rememberSaveable { mutableStateOf(true) }
    val touchExploration = rememberTouchExplorationEnabled()
    val showControls = controlsVisible || touchExploration
    val toggleControls = { controlsVisible = !controlsVisible }
    SystemBarsEffect(hidden = !showControls)

    // Live Photo: only when a real paired video exists for the current still.
    val pair = current?.takeIf { !it.isVideo }?.let { session.livePairs[it.key] }
    var livePlaying by remember(current?.key) { mutableStateOf(false) }
    val activeVideo: MediaItem? = when {
        current?.isVideo == true -> current
        livePlaying && pair != null -> pair
        else -> null
    }
    LaunchedEffect(activeVideo?.key) {
        if (activeVideo == null) playback.clear() else playback.load(activeVideo, autoplay = activeVideo !== current)
    }
    LaunchedEffect(playerState.isEnded, livePlaying) {
        if (livePlaying && playerState.isEnded) livePlaying = false
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { playback.pause() }
    DisposableEffect(Unit) { onDispose { playback.clear() } }

    // Delete: our confirmation first, then Android's own system confirmation.
    var dialog by remember { mutableStateOf<ViewerDialog?>(null) }
    var pendingDelete by remember { mutableStateOf<MediaItem?>(null) }
    val deleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        val target = pendingDelete
        pendingDelete = null
        if (result.resultCode == Activity.RESULT_OK && target != null) {
            viewModel.removeItem(target.key)
            onMediaChanged()
        }
    }

    Box(modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 8.dp,
            beyondViewportPageCount = 1,
            key = { items[it].key },
        ) { page ->
            val item = items[page]
            val isCurrent = page == pagerState.currentPage
            if (item.isVideo) {
                VideoPage(item, isCurrent, playerState, playback.player, toggleControls)
            } else {
                PhotoPage(
                    item = item,
                    isCurrent = isCurrent,
                    showLive = isCurrent && livePlaying && pair != null,
                    player = playback.player,
                    imageLoader = imageLoader,
                    onTap = toggleControls,
                )
            }
        }

        AnimatedVisibility(
            visible = showControls,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
        ) {
            ViewerTopBar(
                onClose = viewModel::close,
                onShare = { current?.let { shareMedia(context, it, context.getString(R.string.viewer_share_title)) } },
                onAddToAlbum = { dialog = ViewerDialog.AddToAlbum },
                onInfo = { dialog = ViewerDialog.Info },
                onDelete = { dialog = if (canTrashFromViewer) ViewerDialog.ConfirmDelete else ViewerDialog.DeleteUnsupported },
            )
        }

        val videoReady = current?.isVideo == true && playerState.loadedKey == current.key && playerState.error == null
        AnimatedVisibility(
            visible = showControls && videoReady,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
        ) {
            CenterPlayButton(playerState, onClick = playback::togglePlay)
        }

        AnimatedVisibility(
            visible = showControls,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    .padding(top = 8.dp),
            ) {
                if (videoReady) VideoSeekBar(playerState, onSeek = playback::seekTo)
                if (pair != null) LiveToggle(active = livePlaying, onClick = { livePlaying = !livePlaying })
                Filmstrip(
                    items = items,
                    currentIndex = currentIndex,
                    onSelect = { index ->
                        scope.launch {
                            if (abs(index - pagerState.currentPage) <= 3) pagerState.animateScrollToPage(index)
                            else pagerState.scrollToPage(index)
                        }
                    },
                )
            }
        }
    }

    when (dialog) {
        ViewerDialog.Info -> current?.let { MediaInfoDialog(it) { dialog = null } }
        ViewerDialog.AddToAlbum -> current?.let {
            AddToAlbumDialog(
                item = it,
                albums = albums,
                onToggle = onToggleAlbum,
                onCreate = onCreateAlbum,
                onDismiss = { dialog = null },
            )
        }
        ViewerDialog.ConfirmDelete -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(stringResource(R.string.viewer_delete_title)) },
            text = { Text(stringResource(R.string.viewer_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    dialog = null
                    val target = current
                    val sender = target?.let { createTrashRequest(context, it) }
                    if (target == null || sender == null) {
                        dialog = ViewerDialog.DeleteFailed
                    } else {
                        pendingDelete = target
                        deleteLauncher.launch(IntentSenderRequest.Builder(sender).build())
                    }
                }) { Text(stringResource(R.string.viewer_delete_confirm)) }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.viewer_cancel)) } },
        )
        ViewerDialog.DeleteUnsupported -> MessageDialog(
            R.string.viewer_delete_unsupported_title, R.string.viewer_delete_unsupported_message,
        ) { dialog = null }
        ViewerDialog.DeleteFailed -> MessageDialog(
            R.string.viewer_delete_failed_title, R.string.viewer_delete_failed_message,
        ) { dialog = null }
        null -> Unit
    }
}

@Composable
private fun MessageDialog(title: Int, message: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title)) },
        text = { Text(stringResource(message)) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.viewer_ok)) } },
    )
}

@Composable
private fun PhotoPage(
    item: MediaItem,
    isCurrent: Boolean,
    showLive: Boolean,
    player: Player,
    imageLoader: ImageLoader,
    onTap: () -> Unit,
) {
    val context = LocalContext.current
    var failed by remember(item.key) { mutableStateOf(false) }
    // Decoded to at most 2560px on the long side: sharp when zoomed, never full resolution.
    val request = remember(item.key) { ImageRequest.Builder(context).data(item.uri).size(2560, 2560).build() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ZoomableBox(isCurrent = isCurrent, onTap = onTap) {
            if (failed) {
                ErrorBlock(stringResource(R.string.viewer_photo_error), stringResource(R.string.viewer_photo_error_detail))
            } else {
                AsyncImage(
                    model = request,
                    imageLoader = imageLoader,
                    contentDescription = item.displayName,
                    contentScale = ContentScale.Fit,
                    onError = { failed = true },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        if (showLive) VideoSurface(player)
    }
}

@Composable
private fun VideoPage(
    item: MediaItem,
    isCurrent: Boolean,
    state: PlayerUiState,
    player: Player,
    onTap: () -> Unit,
) {
    val context = LocalContext.current
    val poster = remember(item.key) { ImageRequest.Builder(context).data(item.uri).build() }
    val showSurface = isCurrent && state.loadedKey == item.key && state.error == null
    Box(
        Modifier.fillMaxSize().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onTap,
        ),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = poster,
            contentDescription = item.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        if (showSurface) VideoSurface(player)
        val error = state.error
        if (isCurrent && state.loadedKey == item.key && error != null) {
            ErrorBlock(
                title = stringResource(
                    when (error) {
                        PlaybackError.Unsupported -> R.string.viewer_video_unsupported
                        PlaybackError.Missing -> R.string.viewer_video_missing
                        PlaybackError.Generic -> R.string.viewer_video_error
                    },
                ),
                detail = null,
            )
        }
    }
}

@Composable
private fun VideoSurface(player: Player) {
    AndroidView(
        factory = { ctx -> PlayerView(ctx).apply { useController = false; this.player = player } },
        modifier = Modifier.fillMaxSize(),
        update = { it.player = player },
        onRelease = { it.player = null },
    )
}

@Composable
private fun ErrorBlock(title: String, detail: String?) {
    Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
        if (detail != null) {
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(top = 8.dp))
        }
    }
}

/** Hides the system bars while controls are hidden and always restores them on exit. */
@Composable
private fun SystemBarsEffect(hidden: Boolean) {
    val activity = LocalActivity.current
    DisposableEffect(hidden, activity) {
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (hidden) controller.hide(WindowInsetsCompat.Type.systemBars()) else controller.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose { }
    }
    DisposableEffect(activity) {
        onDispose {
            activity?.window?.let { WindowCompat.getInsetsController(it, it.decorView).show(WindowInsetsCompat.Type.systemBars()) }
        }
    }
}

@Composable
private fun rememberTouchExplorationEnabled(): Boolean {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(AccessibilityManager::class.java) }
    var enabled by remember { mutableStateOf(manager?.isTouchExplorationEnabled == true) }
    DisposableEffect(manager) {
        val listener = AccessibilityManager.TouchExplorationStateChangeListener { enabled = it }
        manager?.addTouchExplorationStateChangeListener(listener)
        onDispose { manager?.removeTouchExplorationStateChangeListener(listener) }
    }
    return enabled
}
