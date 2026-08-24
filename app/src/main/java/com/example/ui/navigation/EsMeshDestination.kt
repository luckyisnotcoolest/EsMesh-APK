package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.ui.graphics.vector.ImageVector

sealed class EsMeshDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : EsMeshDestination("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Chat : EsMeshDestination("chat", "Chat", Icons.Default.Chat)
    object Devices : EsMeshDestination("devices", "Devices", Icons.Default.Devices)
    object Network : EsMeshDestination("network", "Network", Icons.Default.AltRoute)
    object Config : EsMeshDestination("config", "Config", Icons.Default.Settings)
    object Diagnostics : EsMeshDestination("diagnostics", "Diagnostics", Icons.Default.Speed)
    object Flasher : EsMeshDestination("flasher", "Flasher", Icons.Default.Memory)
    object Settings : EsMeshDestination("settings", "Settings", Icons.Default.Settings)
    object More : EsMeshDestination("more", "More", Icons.Default.MoreHoriz)
    object DeviceDetails : EsMeshDestination("device_details", "Details", Icons.Default.Devices)

    companion object {
        val bottomNavItems = listOf(
            Dashboard,
            Chat,
            Devices,
            Network,
            Config,
            More
        )
    }
}
