package com.example.data.repository

import com.example.data.local.dao.SettingDao
import com.example.data.local.entity.SettingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val settingDao: SettingDao) {

    fun getSettingFlow(key: String, defaultValue: String): Flow<String> {
        return settingDao.getSettingFlow(key).map { it?.value ?: defaultValue }
    }

    suspend fun getSetting(key: String, defaultValue: String): String {
        return settingDao.getSetting(key)?.value ?: defaultValue
    }

    suspend fun setSetting(key: String, value: String) {
        settingDao.setSetting(SettingEntity(key, value))
    }

    companion object {
        const val KEY_THEME = "app_theme" // "dark", "light", "system"
        const val KEY_AUTO_RECONNECT = "auto_reconnect"
        const val KEY_MDNS_ENABLED = "mdns_enabled"
        const val KEY_UDP_BROADCAST_PORT = "udp_port"
        const val KEY_NOTIFICATION_VIBRATE = "notif_vibrate"
    }
}
