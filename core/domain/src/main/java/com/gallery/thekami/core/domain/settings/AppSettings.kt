package com.gallery.thekami.core.domain.settings

enum class ThemeMode { Dark, Light }

enum class FontChoice { UbuntuMono, SystemDefault }

enum class NavBarStyle { Standard, Floating }

/** App-wide preferences. Defaults: dark theme, Ubuntu Mono, standard bottom navigation. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.Dark,
    val fontChoice: FontChoice = FontChoice.UbuntuMono,
    val navBarStyle: NavBarStyle = NavBarStyle.Standard,
)
