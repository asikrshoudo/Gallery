package com.gallery.thekami.core.domain.settings

import com.gallery.thekami.core.domain.media.MediaGrouping

enum class ThemeMode { Dark, Light }

enum class FontChoice { UbuntuMono, SystemDefault }

enum class NavBarStyle { Standard, Floating }

/** CameraRoll = camera media only. AllMedia = camera media plus the sources enabled below. */
enum class HomeViewMode { CameraRoll, AllMedia }

/**
 * What Home shows. These only filter Home; excluded media stays in the library and in
 * Albums, Folders, Collections and search.
 */
data class HomeSettings(
    val viewMode: HomeViewMode = HomeViewMode.CameraRoll,
    val includePhotos: Boolean = true,
    val includeVideos: Boolean = true,
    val includeScreenshots: Boolean = true,
    val includeOtherApps: Boolean = true,
    val grouping: MediaGrouping = MediaGrouping.Day,
)

/** [detectFaces] is opt-in and off by default. */
data class PeopleSettings(val detectFaces: Boolean = false)

/** App-wide preferences. Defaults: dark theme, Ubuntu Mono, standard bottom navigation. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.Dark,
    val fontChoice: FontChoice = FontChoice.UbuntuMono,
    val navBarStyle: NavBarStyle = NavBarStyle.Standard,
    val home: HomeSettings = HomeSettings(),
    val people: PeopleSettings = PeopleSettings(),
)
