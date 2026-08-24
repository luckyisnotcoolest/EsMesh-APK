package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DeviceConnectionStatus
import com.example.ui.components.EsMeshTopAppBar
import com.example.ui.theme.EsMeshBlack
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshCharcoalCard
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshTextMuted
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshTextSecondary
import com.example.ui.theme.EsMeshYellow
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EsMeshBlack)
            .testTag("settings_screen")
    ) {
        EsMeshTopAppBar(
            title = "EsMesh",
            subtitle = "SETTINGS",
            connectionStatus = DeviceConnectionStatus.DISCONNECTED
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Mode Section
            item {
                SettingsSection(title = "VISUAL THEME") {
                    ThemeOptionRow(
                        title = "Dark Cyber Radio (Default)",
                        selected = uiState.themeMode == "dark",
                        onSelect = { viewModel.setThemeMode("dark") }
                    )
                    ThemeOptionRow(
                        title = "System Default",
                        selected = uiState.themeMode == "system",
                        onSelect = { viewModel.setThemeMode("system") }
                    )
                }
            }

            // Connectivity Preferences
            item {
                SettingsSection(title = "DISCOVERY & PROTOCOL PREFERENCES") {
                    SettingsSwitchRow(
                        title = "mDNS Auto-Discovery (_espmesh._tcp)",
                        description = "Automatically resolve nearby ESP32 gateway services.",
                        checked = uiState.mdnsEnabled,
                        onCheckedChange = { viewModel.setMdnsEnabled(it) }
                    )
                    SettingsSwitchRow(
                        title = "Auto-Reconnect WebSocket",
                        description = "Re-establish active mesh stream when connection drops.",
                        checked = uiState.autoReconnect,
                        onCheckedChange = { viewModel.setAutoReconnect(it) }
                    )
                    SettingsSwitchRow(
                        title = "Vibrate on Inbound Packets",
                        description = "Haptic feedback when messages or alerts arrive.",
                        checked = uiState.notificationVibrate,
                        onCheckedChange = { viewModel.setNotificationVibrate(it) }
                    )
                }
            }

            // UDP Port Config
            item {
                SettingsSection(title = "LOCAL UDP BROADCAST PORT") {
                    OutlinedTextField(
                        value = uiState.udpPort,
                        onValueChange = { viewModel.setUdpPort(it) },
                        label = { Text("UDP Port") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EsMeshRed,
                            unfocusedBorderColor = EsMeshBorder,
                            focusedTextColor = EsMeshTextPrimary,
                            unfocusedTextColor = EsMeshTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EsMeshCharcoalCard)
            .border(1.dp, EsMeshBorder, RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = EsMeshYellow,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun ThemeOptionRow(title: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = EsMeshTextPrimary, style = MaterialTheme.typography.bodyMedium)
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = EsMeshRed,
                unselectedColor = EsMeshBorder
            )
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, color = EsMeshTextPrimary, style = MaterialTheme.typography.bodyMedium)
            Text(text = description, color = EsMeshTextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = EsMeshYellow,
                checkedTrackColor = EsMeshRed,
                uncheckedThumbColor = EsMeshTextMuted,
                uncheckedTrackColor = Color(0xFF1E1E28)
            )
        )
    }
}
