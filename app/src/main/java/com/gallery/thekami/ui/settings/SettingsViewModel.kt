package com.gallery.thekami.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gallery.thekami.GalleryApplication
import com.gallery.thekami.core.data.faces.FaceScanService
import com.gallery.thekami.core.data.settings.SettingsRepository
import com.gallery.thekami.core.domain.faces.FaceScanState
import com.gallery.thekami.core.domain.settings.AppSettings
import com.gallery.thekami.core.domain.settings.FontChoice
import com.gallery.thekami.core.domain.settings.HomeSettings
import com.gallery.thekami.core.domain.settings.NavBarStyle
import com.gallery.thekami.core.domain.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Every setting is written through SettingsRepository, the one source of truth the whole app reads. */
class SettingsViewModel(
    private val repository: SettingsRepository,
    private val faceScan: FaceScanService,
) : ViewModel() {
    val settings: StateFlow<AppSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val faceState: StateFlow<FaceScanState> = faceScan.state

    fun setTheme(value: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(value) }
    }

    fun setFont(value: FontChoice) {
        viewModelScope.launch { repository.setFontChoice(value) }
    }

    fun setNavBarStyle(value: NavBarStyle) {
        viewModelScope.launch { repository.setNavBarStyle(value) }
    }

    fun setHome(value: HomeSettings) {
        viewModelScope.launch { repository.setHomeSettings(value) }
    }

    fun setDetectFaces(value: Boolean) {
        viewModelScope.launch { repository.setDetectFaces(value) }
    }

    /** Turns detection off first (so it can't start over), then deletes the stored results. */
    fun turnOffAndClearFaceData() {
        viewModelScope.launch {
            repository.setDetectFaces(false)
            faceScan.clear()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GalleryApplication
                SettingsViewModel(app.settingsRepository, app.faceScan)
            }
        }
    }
}
