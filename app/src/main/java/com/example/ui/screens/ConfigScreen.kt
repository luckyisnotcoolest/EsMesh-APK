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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.components.EsMeshCyberButton
import com.example.ui.components.EsMeshErrorBanner
import com.example.ui.components.EsMeshTopAppBar
import com.example.ui.theme.EsMeshBlack
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshCharcoalCard
import com.example.ui.theme.EsMeshGreen
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshTextMuted
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshTextSecondary
import com.example.ui.theme.EsMeshYellow
import com.example.ui.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("GENERAL", "WI-FI", "AP", "NETWORK", "MESH", "SECURITY")

    var showRestartNotice by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EsMeshBlack)
            .testTag("config_screen")
    ) {
        EsMeshTopAppBar(
            title = "EsMesh",
            subtitle = "CONFIG",
            connectionStatus = if (uiState.isConnected) DeviceConnectionStatus.CONNECTED else DeviceConnectionStatus.DISCONNECTED,
            actions = {
                IconButton(onClick = { viewModel.restartDevice { showRestartNotice = true } }) {
                    Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = "Restart Node", tint = EsMeshRed)
                }
            }
        )

        // Configuration Tabs
        ScrollableTabRow(
            selectedTabIndex = uiState.activeTab,
            containerColor = Color(0xFF0D0D12),
            contentColor = EsMeshYellow,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.activeTab]),
                    color = EsMeshRed,
                    height = 2.5.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.activeTab == index,
                    onClick = { viewModel.setTab(index) },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.activeTab == index) EsMeshTextPrimary else EsMeshTextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.errorMessage != null) {
                item {
                    EsMeshErrorBanner(
                        title = "Configuration Warning",
                        description = uiState.errorMessage ?: ""
                    )
                }
            }

            if (uiState.saveSuccess) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F2615))
                            .border(1.dp, EsMeshGreen, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "✓ Configuration pushed to ESP32 node successfully!",
                            color = EsMeshGreen,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Tab contents
            item {
                when (uiState.activeTab) {
                    0 -> GeneralConfigSection(uiState.config) { update -> viewModel.updateConfig(update) }
                    1 -> WifiConfigSection(uiState.config) { update -> viewModel.updateConfig(update) }
                    2 -> ApConfigSection(uiState.config) { update -> viewModel.updateConfig(update) }
                    3 -> NetworkConfigSection(uiState.config) { update -> viewModel.updateConfig(update) }
                    4 -> MeshConfigSection(uiState.config) { update -> viewModel.updateConfig(update) }
                    5 -> SecurityConfigSection(uiState.config) { update -> viewModel.updateConfig(update) }
                }
            }

            // Push Config Button
            item {
                Spacer(modifier = Modifier.height(8.dp))

                EsMeshCyberButton(
                    text = if (uiState.isSaving) "SAVING TO NODE..." else "SAVE & PUSH CONFIG",
                    icon = if (uiState.isSaving) null else Icons.Default.Save,
                    onClick = { viewModel.saveConfig { } },
                    isPrimary = true,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth().testTag("save_config_btn")
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun GeneralConfigSection(
    config: com.example.data.model.EsMeshConfig,
    onUpdate: ((com.example.data.model.EsMeshConfig) -> com.example.data.model.EsMeshConfig) -> Unit
) {
    ConfigCard(title = "GENERAL NODE IDENTITY") {
        ConfigTextField(
            label = "Device Display Name",
            value = config.deviceName,
            onValueChange = { name -> onUpdate { it.copy(deviceName = name) } }
        )
        ConfigTextField(
            label = "Mesh Node ID",
            value = config.nodeId,
            onValueChange = { nid -> onUpdate { it.copy(nodeId = nid) } }
        )
        ConfigTextField(
            label = "Operating Mode",
            value = config.operatingMode,
            onValueChange = { mode -> onUpdate { it.copy(operatingMode = mode) } }
        )
    }
}

@Composable
private fun WifiConfigSection(
    config: com.example.data.model.EsMeshConfig,
    onUpdate: ((com.example.data.model.EsMeshConfig) -> com.example.data.model.EsMeshConfig) -> Unit
) {
    ConfigCard(title = "WI-FI STATION (STA) SETTINGS") {
        ConfigTextField(
            label = "SSID / Network Name",
            value = config.wifiSsid,
            onValueChange = { ssid -> onUpdate { it.copy(wifiSsid = ssid) } }
        )
        ConfigTextField(
            label = "WPA2/WPA3 Password",
            value = config.wifiPassword,
            isPassword = true,
            onValueChange = { pass -> onUpdate { it.copy(wifiPassword = pass) } }
        )
        ConfigSwitchRow(
            label = "Enable DHCP Auto IP",
            checked = config.isDhcp,
            onCheckedChange = { dhcp -> onUpdate { it.copy(isDhcp = dhcp) } }
        )
        if (!config.isDhcp) {
            ConfigTextField(
                label = "Static IP Address",
                value = config.staticIp,
                onValueChange = { ip -> onUpdate { it.copy(staticIp = ip) } }
            )
            ConfigTextField(
                label = "Gateway IP",
                value = config.gateway,
                onValueChange = { gw -> onUpdate { it.copy(gateway = gw) } }
            )
        }
    }
}

@Composable
private fun ApConfigSection(
    config: com.example.data.model.EsMeshConfig,
    onUpdate: ((com.example.data.model.EsMeshConfig) -> com.example.data.model.EsMeshConfig) -> Unit
) {
    ConfigCard(title = "ACCESS POINT (AP) SETTINGS") {
        ConfigSwitchRow(
            label = "Enable Onboard AP Hotspot",
            checked = config.apEnabled,
            onCheckedChange = { en -> onUpdate { it.copy(apEnabled = en) } }
        )
        if (config.apEnabled) {
            ConfigTextField(
                label = "AP SSID",
                value = config.apSsid,
                onValueChange = { ssid -> onUpdate { it.copy(apSsid = ssid) } }
            )
            ConfigTextField(
                label = "AP Password",
                value = config.apPassword,
                isPassword = true,
                onValueChange = { pass -> onUpdate { it.copy(apPassword = pass) } }
            )
            ConfigTextField(
                label = "Wi-Fi Channel (1-13)",
                value = config.apChannel.toString(),
                onValueChange = { ch -> onUpdate { it.copy(apChannel = ch.toIntOrNull() ?: 6) } }
            )
        }
    }
}

@Composable
private fun NetworkConfigSection(
    config: com.example.data.model.EsMeshConfig,
    onUpdate: ((com.example.data.model.EsMeshConfig) -> com.example.data.model.EsMeshConfig) -> Unit
) {
    ConfigCard(title = "NETWORK SERVICES & PORTS") {
        ConfigTextField(
            label = "HTTP Web Port",
            value = config.httpPort.toString(),
            onValueChange = { p -> onUpdate { it.copy(httpPort = p.toIntOrNull() ?: 80) } }
        )
        ConfigTextField(
            label = "WebSocket Port",
            value = config.wsPort.toString(),
            onValueChange = { p -> onUpdate { it.copy(wsPort = p.toIntOrNull() ?: 80) } }
        )
        ConfigTextField(
            label = "mDNS Hostname",
            value = config.mdnsName,
            onValueChange = { n -> onUpdate { it.copy(mdnsName = n) } }
        )
        ConfigTextField(
            label = "Discovery Broadcast Interval (sec)",
            value = config.discoveryIntervalSec.toString(),
            onValueChange = { s -> onUpdate { it.copy(discoveryIntervalSec = s.toIntOrNull() ?: 10) } }
        )
    }
}

@Composable
private fun MeshConfigSection(
    config: com.example.data.model.EsMeshConfig,
    onUpdate: ((com.example.data.model.EsMeshConfig) -> com.example.data.model.EsMeshConfig) -> Unit
) {
    ConfigCard(title = "MESH ROUTING & TOPOLOGY") {
        ConfigSwitchRow(
            label = "Enable RF Mesh Routing",
            checked = config.meshEnabled,
            onCheckedChange = { en -> onUpdate { it.copy(meshEnabled = en) } }
        )
        ConfigSwitchRow(
            label = "Repeater Mode",
            checked = config.repeaterMode,
            onCheckedChange = { rep -> onUpdate { it.copy(repeaterMode = rep) } }
        )
        ConfigTextField(
            label = "Max Mesh Hops",
            value = config.maxHops.toString(),
            onValueChange = { h -> onUpdate { it.copy(maxHops = h.toIntOrNull() ?: 8) } }
        )
        ConfigTextField(
            label = "Default Packet TTL",
            value = config.ttl.toString(),
            onValueChange = { t -> onUpdate { it.copy(ttl = t.toIntOrNull() ?: 5) } }
        )
    }
}

@Composable
private fun SecurityConfigSection(
    config: com.example.data.model.EsMeshConfig,
    onUpdate: ((com.example.data.model.EsMeshConfig) -> com.example.data.model.EsMeshConfig) -> Unit
) {
    ConfigCard(title = "SECURITY & ENCRYPTION") {
        ConfigSwitchRow(
            label = "Enable REST API Authentication",
            checked = config.apiAuthEnabled,
            onCheckedChange = { en -> onUpdate { it.copy(apiAuthEnabled = en) } }
        )
        if (config.apiAuthEnabled) {
            ConfigTextField(
                label = "Bearer Auth Token",
                value = config.authToken,
                isPassword = true,
                onValueChange = { token -> onUpdate { it.copy(authToken = token) } }
            )
        }
        ConfigSwitchRow(
            label = "Payload AES-128 Encryption",
            checked = config.encryptionEnabled,
            onCheckedChange = { en -> onUpdate { it.copy(encryptionEnabled = en) } }
        )
        if (config.encryptionEnabled) {
            ConfigTextField(
                label = "AES Pre-shared Key (16/32 bytes)",
                value = config.encryptionKey,
                isPassword = true,
                onValueChange = { key -> onUpdate { it.copy(encryptionKey = key) } }
            )
        }
    }
}

@Composable
private fun ConfigCard(title: String, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(EsMeshCharcoalCard)
            .border(1.dp, EsMeshBorder, RoundedCornerShape(8.dp))
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
private fun ConfigTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EsMeshRed,
            unfocusedBorderColor = EsMeshBorder,
            focusedLabelColor = EsMeshYellow,
            unfocusedLabelColor = EsMeshTextSecondary,
            focusedTextColor = EsMeshTextPrimary,
            unfocusedTextColor = EsMeshTextPrimary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ConfigSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = EsMeshTextPrimary, style = MaterialTheme.typography.bodyMedium)
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
