package com.gallery.thekami.ui.collections

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.navigation.CollectionsRoute
import com.gallery.thekami.core.ui.states.ErrorState
import com.gallery.thekami.core.ui.states.LoadingState
import com.gallery.thekami.ui.PermissionPrompt

/** Everything the Collections screens can ask the app to do. */
@Immutable
class CollectionsActions(
    val push: (CollectionsRoute) -> Unit,
    val pop: () -> Unit,
    val openSearch: () -> Unit,
    val openMedia: (sequence: List<MediaItem>, item: MediaItem) -> Unit,
    val requestAccess: () -> Unit,
    val openSettings: () -> Unit,
    val refresh: () -> Unit,
    val createAlbum: (name: String) -> Unit,
    val renameAlbum: (id: String, name: String) -> Unit,
    val deleteAlbum: (id: String) -> Unit,
    val addMedia: (id: String, keys: Collection<String>) -> Unit,
    val removeMedia: (id: String, keys: Collection<String>) -> Unit,
    val setCover: (id: String, key: String) -> Unit,
)

@Composable
fun CollectionsHost(
    route: CollectionsRoute?,
    state: CollectionsState,
    contentPadding: PaddingValues,
    actions: CollectionsActions,
    modifier: Modifier = Modifier,
) {
    val bottom = contentPadding.calculateBottomPadding()
    Box(modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding())) {
        when (state) {
            CollectionsState.Loading -> LoadingState(Modifier.padding(bottom = bottom))
            CollectionsState.PermissionRequired ->
                PermissionPrompt(actions.requestAccess, actions.openSettings, Modifier.padding(bottom = bottom))
            is CollectionsState.Failed -> ErrorState(
                title = stringResource(R.string.home_error_title),
                message = state.message ?: stringResource(R.string.home_error_message),
                onRetry = actions.refresh,
                modifier = Modifier.padding(bottom = bottom),
            )
            is CollectionsState.Ready -> Crossfade(
                targetState = route,
                animationSpec = tween(150),
                label = "collectionsRoute",
            ) { target -> RouteContent(target, state, bottom, actions) }
        }
    }
}

@Composable
private fun RouteContent(
    route: CollectionsRoute?,
    state: CollectionsState.Ready,
    bottom: Dp,
    actions: CollectionsActions,
) {
    when (route) {
        null -> CollectionsRootScreen(state, bottom, actions)
        CollectionsRoute.Albums -> AlbumsScreen(state, bottom, actions)
        is CollectionsRoute.AlbumDetail -> AlbumDetailScreen(state, route.albumId, bottom, actions)
        is CollectionsRoute.AlbumPicker -> AlbumPickerScreen(state, route.albumId, bottom, actions)
        CollectionsRoute.Folders -> FoldersScreen(state, bottom, actions)
        CollectionsRoute.OtherFolders -> OtherFoldersScreen(state, bottom, actions)
        is CollectionsRoute.FolderDetail -> FolderDetailScreen(state, route.folderId, bottom, actions)
    }
}
