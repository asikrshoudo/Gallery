package com.gallery.thekami.core.ui.navigation

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import com.gallery.thekami.core.ui.R
import com.gallery.thekami.core.ui.theme.GalleryColors
import com.gallery.thekami.core.ui.theme.GalleryDimens

/** Circular lime Search action. Lime with near-black icon in both themes. */
@Composable
fun SearchActionButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(GalleryDimens.SearchButtonSize)
            .semantics { role = Role.Button },
        shape = CircleShape,
        color = GalleryColors.Lime,
        contentColor = GalleryColors.OnLime,
        shadowElevation = 6.dp,
    ) {
        androidx.compose.foundation.layout.Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(R.string.action_search),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
