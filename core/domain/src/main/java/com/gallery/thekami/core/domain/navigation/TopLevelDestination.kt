package com.gallery.thekami.core.domain.navigation

/**
 * Main bottom destinations. [hasBottomSearch] is the single source of truth for whether
 * the bottom Search action is shown; Collections carries Search at its top instead.
 */
enum class TopLevelDestination(val hasBottomSearch: Boolean) {
    Home(hasBottomSearch = true),
    Collections(hasBottomSearch = false),
}
