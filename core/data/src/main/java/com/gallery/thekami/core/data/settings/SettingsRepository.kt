package com.gallery.thekami.core.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gallery.thekami.core.domain.settings.AppSettings
import com.gallery.thekami.core.domain.settings.FontChoice
import com.gallery.thekami.core.domain.settings.NavBarStyle
import com.gallery.thekami.core.domain.settings.ThemeMode
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.galleryDataStore: DataStore<Preferences> by preferencesDataStore(name = "gallery_settings")

/** Single source of truth for app-wide preferences. Unknown stored values fall back to defaults. */
class SettingsRepository(context: Context) {
    private val store = context.applicationContext.galleryDataStore

    val settings: Flow<AppSettings> = store.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toAppSettings() }
        .distinctUntilChanged()

    suspend fun setThemeMode(value: ThemeMode) {
        store.edit { it[Keys.THEME_MODE] = value.name }
    }

    suspend fun setFontChoice(value: FontChoice) {
        store.edit { it[Keys.FONT_CHOICE] = value.name }
    }

    suspend fun setNavBarStyle(value: NavBarStyle) {
        store.edit { it[Keys.NAV_BAR_STYLE] = value.name }
    }
}

private object Keys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val FONT_CHOICE = stringPreferencesKey("font_choice")
    val NAV_BAR_STYLE = stringPreferencesKey("nav_bar_style")
}

private fun Preferences.toAppSettings(): AppSettings {
    val defaults = AppSettings()
    return AppSettings(
        themeMode = enumOrDefault(this[Keys.THEME_MODE], defaults.themeMode),
        fontChoice = enumOrDefault(this[Keys.FONT_CHOICE], defaults.fontChoice),
        navBarStyle = enumOrDefault(this[Keys.NAV_BAR_STYLE], defaults.navBarStyle),
    )
}

private inline fun <reified E : Enum<E>> enumOrDefault(name: String?, default: E): E =
    enumValues<E>().firstOrNull { it.name == name } ?: default
