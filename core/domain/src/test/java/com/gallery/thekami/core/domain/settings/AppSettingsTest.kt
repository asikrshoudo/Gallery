package com.gallery.thekami.core.domain.settings

import com.gallery.thekami.core.domain.media.MediaGrouping
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsTest {
    @Test
    fun defaultsMatchProductDecisions() {
        val settings = AppSettings()
        assertEquals(ThemeMode.Dark, settings.themeMode)
        assertEquals(FontChoice.UbuntuMono, settings.fontChoice)
        assertEquals(NavBarStyle.Standard, settings.navBarStyle)
    }

    @Test
    fun homeDefaultsToCameraRollWithEverythingIncluded() {
        val home = AppSettings().home
        assertEquals(HomeViewMode.CameraRoll, home.viewMode)
        assertEquals(MediaGrouping.Day, home.grouping)
        assertTrue(home.includePhotos && home.includeVideos && home.includeScreenshots && home.includeOtherApps)
    }
}
