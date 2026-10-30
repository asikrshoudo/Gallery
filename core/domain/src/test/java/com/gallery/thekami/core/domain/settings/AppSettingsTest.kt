package com.gallery.thekami.core.domain.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSettingsTest {
    @Test
    fun defaultsMatchProductDecisions() {
        val settings = AppSettings()
        assertEquals(ThemeMode.Dark, settings.themeMode)
        assertEquals(FontChoice.UbuntuMono, settings.fontChoice)
        assertEquals(NavBarStyle.Standard, settings.navBarStyle)
    }
}
