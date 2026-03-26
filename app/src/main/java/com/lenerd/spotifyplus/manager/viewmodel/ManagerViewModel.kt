package com.lenerd.spotifyplus.manager.viewmodel

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lenerd.spotifyplus.manager.model.AppTheme
import com.lenerd.spotifyplus.manager.model.HookItem
import com.lenerd.spotifyplus.manager.model.HookStatus
import com.lenerd.spotifyplus.manager.model.ManagerUiState
import com.lenerd.spotifyplus.manager.model.UpdateInfo
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

class ManagerViewModel : ViewModel() {
    private var xposedService: XposedService? = null
    private var remotePrefs: SharedPreferences? = null

    init {
        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                xposedService = service
                remotePrefs = service.getRemotePreferences("SpotifyPlus")

                loadSettingsFromRemotePrefs()
            }

            override fun onServiceDied(service: XposedService) {
                xposedService = null
                remotePrefs = null
            }
        })
    }

    private fun loadSettingsFromRemotePrefs() {
        val prefs = remotePrefs ?: return

        uiState = uiState.copy(
            autoCheckUpdates = prefs.getBoolean("auto_check_updates", true),
            debugLogging = prefs.getBoolean("debug_logging", false),
            startupCheck = prefs.getBoolean("startup_check", true)
        )
    }

    var uiState by mutableStateOf(
        ManagerUiState(
            generalSettings = listOf(
                HookItem(
                    id = "lastfmUsername",
                    name = "Beautiful Lyrics",
                    description = "Enhanced synced lyrics overlay and animations",
                    enabled = true
                ),
                HookItem(
                    id = "theme",
                    name = "Theme Hook",
                    description = "Overrides Spotify colors and AMOLED styling",
                    enabled = true
                ),
                HookItem(
                    id = "translation",
                    name = "Lyrics Translation",
                    description = "Translates current lyrics through external logic",
                    enabled = false
                ),
                HookItem(
                    id = "background",
                    name = "Animated Background",
                    description = "Dynamic blurred/art-driven visual background",
                    enabled = true
                ),
                HookItem(
                    id = "script_adblock",
                    name = "Request Blocking Script",
                    description = "Blocks selected network endpoints",
                    enabled = false,
                    isScript = true
                ),
                HookItem(
                    id = "script_dev",
                    name = "Debug Script",
                    description = "Extra logging and experimental diagnostics",
                    enabled = false,
                    isScript = true
                )
            )
        )
    )
        private set

    fun refreshStatus() {
        val enabledHooks = uiState.generalSettings.count { it.enabled }

        val status = when {
            !uiState.moduleEnabled -> HookStatus.NOT_HOOKED
            enabledHooks == 0 -> HookStatus.NOT_HOOKED
            enabledHooks < uiState.generalSettings.size -> HookStatus.PARTIAL
            else -> HookStatus.HOOKED
        }

        uiState = uiState.copy(
            hookStatus = status,
            lastCheckedStatus = "Just now",
            statusMessage = when (status) {
                HookStatus.HOOKED -> "SpotifyPlus is active and all hooks are enabled."
                HookStatus.PARTIAL -> "SpotifyPlus is partially active. Some hooks/scripts are disabled."
                HookStatus.NOT_HOOKED -> "SpotifyPlus is not fully active or no hooks are enabled."
            }
        )
    }

    fun checkForUpdates() {
        uiState = uiState.copy(
            updateInfo = UpdateInfo(
                currentVersion = "1.0.0",
                latestVersion = "1.1.0",
                updateAvailable = true,
                changelog = listOf(
                    "Added manager dashboard",
                    "Improved hook status detection",
                    "Added script toggles",
                    "Improved AMOLED theme support"
                )
            ),
            statusMessage = "Checked for updates."
        )
    }

    fun installUpdate() {
        uiState = uiState.copy(
            updateInfo = uiState.updateInfo.copy(
                currentVersion = uiState.updateInfo.latestVersion,
                updateAvailable = false
            ),
            statusMessage = "Update installed successfully."
        )
    }

    fun toggleHook(id: String) {
        uiState = uiState.copy(
            generalSettings = uiState.generalSettings.map {
                if (!it.isScript && it.id == id) it.copy(enabled = !it.enabled) else it
            }
        )
        refreshStatus()
    }

    fun toggleScript(id: String) {
        uiState = uiState.copy(
            generalSettings = uiState.generalSettings.map {
                if (it.isScript && it.id == id) it.copy(enabled = !it.enabled) else it
            }
        )
        refreshStatus()
    }

    fun enableAllHooks() {
        uiState = uiState.copy(
            generalSettings = uiState.generalSettings.map { it.copy(enabled = true) }
        )
        refreshStatus()
    }

    fun disableAllHooks() {
        uiState = uiState.copy(
            generalSettings = uiState.generalSettings.map { it.copy(enabled = false) }
        )
        refreshStatus()
    }

    fun setAutoCheckUpdates(enabled: Boolean) {
        uiState = uiState.copy(autoCheckUpdates = enabled)
    }

    fun setDebugLogging(enabled: Boolean) {
        uiState = uiState.copy(debugLogging = enabled)
    }

    fun setStartupCheck(enabled: Boolean) {
        uiState = uiState.copy(startupCheck = enabled)
    }

    fun setTheme(theme: AppTheme) {
        uiState = uiState.copy(theme = theme)
    }
}