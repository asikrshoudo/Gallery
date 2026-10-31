package com.gallery.thekami.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.ui.states.EmptyState
import com.gallery.thekami.core.ui.states.ErrorState
import com.gallery.thekami.core.ui.states.LoadingState
import com.gallery.thekami.core.ui.theme.GalleryTheme
import com.gallery.thekami.ui.PermissionPrompt
import com.gallery.thekami.ui.collections.MediaGrid
import com.gallery.thekami.ui.quantityString

/** Local search by file name, folder and date. Opening a result passes the result list to the viewer. */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onClose: () -> Unit,
    onOpenMedia: (sequence: List<MediaItem>, item: MediaItem, livePairs: Map<String, MediaItem>) -> Unit,
    onRequestAccess: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = GalleryTheme.spacing
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var field by remember {
        val initial = viewModel.query.value
        mutableStateOf(TextFieldValue(initial, TextRange(initial.length)))
    }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.windowInsetsPadding(WindowInsets.systemBars).imePadding()) {
            Row(
                Modifier.fillMaxWidth().padding(spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
                OutlinedTextField(
                    value = field,
                    onValueChange = {
                        field = it
                        viewModel.setQuery(it.text)
                    },
                    modifier = Modifier.weight(1f).focusRequester(focusRequester),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    trailingIcon = {
                        if (field.text.isNotEmpty()) {
                            IconButton(onClick = {
                                field = TextFieldValue("")
                                viewModel.setQuery("")
                            }) {
                                Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.search_clear))
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                )
            }
            Box(Modifier.weight(1f)) {
                when (val s = state) {
                    SearchUiState.Loading -> LoadingState()
                    SearchUiState.PermissionRequired -> PermissionPrompt(onRequestAccess, onOpenSettings)
                    is SearchUiState.Failed -> ErrorState(
                        title = stringResource(R.string.home_error_title),
                        message = s.message ?: stringResource(R.string.home_error_message),
                        onRetry = onRefresh,
                    )
                    is SearchUiState.Idle -> EmptyState(
                        title = stringResource(R.string.search_idle_title),
                        message = stringResource(R.string.search_idle_message),
                    )
                    is SearchUiState.Results -> if (s.items.isEmpty()) {
                        EmptyState(
                            title = stringResource(R.string.search_no_results_title),
                            message = stringResource(R.string.search_no_results_message, s.query),
                        )
                    } else {
                        Column(Modifier.fillMaxSize()) {
                            Text(
                                text = quantityString(R.plurals.search_result_count, s.items.size),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs),
                            )
                            MediaGrid(
                                items = s.items,
                                selected = emptySet(),
                                onClick = { item ->
                                    focusManager.clearFocus()
                                    onOpenMedia(s.items, item, s.livePairs)
                                },
                                onLongClick = null,
                                contentPadding = PaddingValues(bottom = spacing.sm),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}
