package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: String = "dark", // "dark", "light", "system"
    val autoReconnect: Boolean = true,
    val mdnsEnabled: Boolean = true,
    val udpPort: String = "8266",
    val notificationVibrate: Boolean = true
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.getSettingFlow(SettingsRepository.KEY_THEME, "dark"),
        settingsRepository.getSettingFlow(SettingsRepository.KEY_AUTO_RECONNECT, "true"),
        settingsRepository.getSettingFlow(SettingsRepository.KEY_MDNS_ENABLED, "true"),
        settingsRepository.getSettingFlow(SettingsRepository.KEY_UDP_BROADCAST_PORT, "8266"),
        settingsRepository.getSettingFlow(SettingsRepository.KEY_NOTIFICATION_VIBRATE, "true")
    ) { theme, autoRec, mdns, port, vib ->
        SettingsUiState(
            themeMode = theme,
            autoReconnect = autoRec.toBoolean(),
            mdnsEnabled = mdns.toBoolean(),
            udpPort = port,
            notificationVibrate = vib.toBoolean()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setSetting(SettingsRepository.KEY_THEME, mode)
        }
    }

    fun setAutoReconnect(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSetting(SettingsRepository.KEY_AUTO_RECONNECT, enabled.toString())
        }
    }

    fun setMdnsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSetting(SettingsRepository.KEY_MDNS_ENABLED, enabled.toString())
        }
    }

    fun setUdpPort(port: String) {
        viewModelScope.launch {
            settingsRepository.setSetting(SettingsRepository.KEY_UDP_BROADCAST_PORT, port)
        }
    }

    fun setNotificationVibrate(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSetting(SettingsRepository.KEY_NOTIFICATION_VIBRATE, enabled.toString())
        }
    }

    class Factory(private val settingsRepo: SettingsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepo) as T
        }
    }
}
