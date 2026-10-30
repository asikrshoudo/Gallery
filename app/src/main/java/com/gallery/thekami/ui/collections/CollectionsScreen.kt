package com.gallery.thekami.ui.collections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gallery.thekami.R
import com.gallery.thekami.core.ui.states.EmptyState
import com.gallery.thekami.ui.ScreenTitle

@Composable
fun CollectionsScreen(contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(contentPadding)) {
        ScreenTitle(stringResource(R.string.collections_title))
        EmptyState(
            title = stringResource(R.string.collections_empty_title),
            message = stringResource(R.string.collections_empty_message),
            modifier = Modifier.weight(1f),
        )
    }
}
