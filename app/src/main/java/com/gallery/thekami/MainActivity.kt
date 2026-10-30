package com.gallery.thekami

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gallery.thekami.core.data.settings.SettingsRepository
import com.gallery.thekami.core.domain.settings.AppSettings
import com.gallery.thekami.core.domain.settings.ThemeMode
import com.gallery.thekami.core.ui.theme.GalleryTheme
import com.gallery.thekami.ui.GalleryApp

class MainActivity : ComponentActivity() {
    private val settingsRepository by lazy { SettingsRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by settingsRepository.settings.collectAsState(initial = AppSettings())
            val dark = settings.themeMode == ThemeMode.Dark
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                )
                onDispose {}
            }
            GalleryTheme(themeMode = settings.themeMode, fontChoice = settings.fontChoice) {
                GalleryApp(navBarStyle = settings.navBarStyle)
            }
        }
    }
}
