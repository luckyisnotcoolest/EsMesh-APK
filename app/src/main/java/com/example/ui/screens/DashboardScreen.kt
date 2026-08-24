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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.EsMeshDevice
import com.example.ui.components.AddDeviceManuallyDialog
import com.example.ui.components.EsMeshCyberButton
import com.example.ui.components.EsMeshErrorBanner
import com.example.ui.components.EsMeshTopAppBar
import com.example.ui.components.ProtocolBadge
import com.example.ui.components.RssiMeter
import com.example.ui.components.StatusLedIndicator
import com.example.ui.components.TelemetryMetricCard
import com.example.ui.theme.EsMeshBlack
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshBorderBright
import com.example.ui.theme.EsMeshCharcoalCard
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshRedContainer
import com.example.ui.theme.EsMeshTextMuted
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshTextSecondary
import com.example.ui.theme.EsMeshYellow
import com.example.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val connectedDevice by viewModel.connectedDevice.collectAsStateWithLifecycle()
    val isConnecting by viewModel.isConnecting.collectAsStateWithLifecycle()
    val lastError by viewModel.lastError.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    val status = when {
        isConnecting -> DeviceConnectionStatus.CONNECTING
        connectedDevice != null -> DeviceConnectionStatus.CONNECTED
        else -> DeviceConnectionStatus.DISCONNECTED
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EsMeshBlack)
            .testTag("dashboard_screen")
    ) {
        EsMeshTopAppBar(
            title = "EsMesh",
            subtitle = "DASHBOARD",
            connectionStatus = status
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Connection Error Banner if any
            if (lastError != null) {
                item {
                    EsMeshErrorBanner(
                        title = "Network Communication Notice",
                        description = lastError ?: "Unable to establish stable mesh socket",
                        actionLabel = "DISCOVER DEVICES",
                        onActionClick = {
                            viewModel.startDiscovery()
                            onNavigateToDevices()
                        }
                    )
                }
            }

            // Primary Device Card or Empty State
            item {
                if (connectedDevice != null) {
                    ConnectedGatewayCard(
                        device = connectedDevice!!,
                        onDisconnect = { viewModel.disconnect() }
                    )
                } else {
                    DisconnectedGatewayCard(
                        onDiscoverClick = {
                            viewModel.startDiscovery()
                            onNavigateToDevices()
                        },
                        onAddManualClick = { showAddDialog = true }
                    )
                }
            }

            // Telemetry Metric Grid
            item {
                Text(
                    text = "RADIO TELEMETRY & NETWORK METRICS",
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshTextMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                val dev = connectedDevice
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TelemetryMetricCard(
                            label = "Messages",
                            value = "${dev?.totalMessages ?: 0}",
                            icon = Icons.Default.MarkChatRead,
                            accentColor = EsMeshYellow,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryMetricCard(
                            label = "Mesh Nodes",
                            value = "${dev?.meshNodesCount ?: 0}",
                            icon = Icons.Default.DeviceHub,
                            accentColor = EsMeshYellow,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryMetricCard(
                            label = "Packets",
                            value = "${dev?.totalPackets ?: 0}",
                            icon = Icons.Default.Speed,
                            accentColor = EsMeshRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TelemetryMetricCard(
                            label = "Connection",
                            value = if (connectedDevice != null) "WebSocket" else "Offline",
                            icon = Icons.Default.Sensors,
                            accentColor = if (connectedDevice != null) EsMeshYellow else EsMeshTextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryMetricCard(
                            label = "RSSI",
                            value = if (connectedDevice != null) "${dev?.rssi ?: -48} dBm" else "-- dBm",
                            icon = Icons.Default.Radar,
                            accentColor = if (connectedDevice != null) EsMeshYellow else EsMeshTextMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Access Nav Buttons Grid
            item {
                Text(
                    text = "CONTROLLER ACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshTextMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        EsMeshCyberButton(
                            text = "CHAT",
                            icon = Icons.Default.Chat,
                            onClick = onNavigateToChat,
                            isPrimary = true,
                            modifier = Modifier.weight(1f).testTag("dashboard_chat_btn")
                        )
                        EsMeshCyberButton(
                            text = "DEVICES",
                            icon = Icons.Default.Devices,
                            onClick = onNavigateToDevices,
                            isPrimary = false,
                            modifier = Modifier.weight(1f).testTag("dashboard_devices_btn")
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        EsMeshCyberButton(
                            text = "NETWORK",
                            icon = Icons.Default.AltRoute,
                            onClick = onNavigateToNetwork,
                            isPrimary = false,
                            modifier = Modifier.weight(1f).testTag("dashboard_network_btn")
                        )
                        EsMeshCyberButton(
                            text = "CONFIGURATION",
                            icon = Icons.Default.Settings,
                            onClick = onNavigateToConfig,
                            isPrimary = false,
                            modifier = Modifier.weight(1f).testTag("dashboard_config_btn")
                        )
                    }
                    EsMeshCyberButton(
                        text = "DIAGNOSTICS",
                        icon = Icons.Default.Speed,
                        onClick = onNavigateToDiagnostics,
                        isPrimary = false,
                        modifier = Modifier.fillMaxWidth().testTag("dashboard_diag_btn")
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAddDialog) {
        AddDeviceManuallyDialog(
            onDismiss = { showAddDialog = false },
            onTestAndAdd = { ip, httpP, wsP ->
                showAddDialog = false
                val newDev = EsMeshDevice(
                    id = "ESM-${ip.takeLast(4)}",
                    name = "ESP32 Manual Node",
                    ipAddress = ip,
                    httpPort = httpP,
                    wsPort = wsP,
                    connectionStatus = DeviceConnectionStatus.CONNECTED
                )
                viewModel.connectDevice(newDev)
            }
        )
    }
}

@Composable
private fun ConnectedGatewayCard(
    device: EsMeshDevice,
    onDisconnect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EsMeshCharcoalCard)
            .border(1.5.dp, EsMeshRed, RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CONNECTED GATEWAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = EsMeshYellow,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = EsMeshTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusLedIndicator(status = DeviceConnectionStatus.CONNECTED)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tech stats rows
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TechInfoRow(label = "Device:", value = device.model)
                TechInfoRow(label = "Node ID:", value = device.nodeId, isAccent = true)
                TechInfoRow(label = "Wi-Fi:", value = device.ssid)
                TechInfoRow(label = "IP Address:", value = "${device.ipAddress}:${device.wsPort}")
                TechInfoRow(label = "RSSI:", value = "${device.rssi} dBm")
                TechInfoRow(label = "Protocol:", value = device.protocolVersion)
            }

            Spacer(modifier = Modifier.height(16.dp))

            EsMeshCyberButton(
                text = "DISCONNECT GATEWAY",
                icon = Icons.Default.PowerSettingsNew,
                onClick = onDisconnect,
                isPrimary = false,
                isDestructive = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DisconnectedGatewayCard(
    onDiscoverClick: () -> Unit,
    onAddManualClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EsMeshCharcoalCard)
            .border(1.dp, EsMeshBorder, RoundedCornerShape(10.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1E1E28), RoundedCornerShape(24.dp))
                    .border(1.dp, EsMeshBorder, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Router,
                    contentDescription = "Router",
                    tint = EsMeshTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "No ESP32 connected",
                style = MaterialTheme.typography.titleMedium,
                color = EsMeshTextPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Scan your local network for active _espmesh._tcp nodes or specify a static IP address manually.",
                style = MaterialTheme.typography.bodyMedium,
                color = EsMeshTextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EsMeshCyberButton(
                    text = "DISCOVER DEVICES",
                    icon = Icons.Default.Search,
                    onClick = onDiscoverClick,
                    isPrimary = true,
                    modifier = Modifier.weight(1f).testTag("discover_devices_btn")
                )
                EsMeshCyberButton(
                    text = "ADD DEVICE",
                    icon = Icons.Default.Add,
                    onClick = onAddManualClick,
                    isPrimary = false,
                    modifier = Modifier.weight(1f).testTag("add_device_btn")
                )
            }
        }
    }
}

@Composable
private fun TechInfoRow(
    label: String,
    value: String,
    isAccent: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = EsMeshTextMuted,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = if (isAccent) EsMeshYellow else EsMeshTextPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
