package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.components.EsMeshTopAppBar
import com.example.ui.components.ProtocolBadge
import com.example.ui.components.RenameDeviceDialog
import com.example.ui.components.RssiMeter
import com.example.ui.components.StatusLedIndicator
import com.example.ui.components.TechnicalDeviceCard
import com.example.ui.theme.EsMeshBlack
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshCharcoalCard
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshTextMuted
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshTextSecondary
import com.example.ui.theme.EsMeshYellow
import com.example.ui.viewmodel.DevicesViewModel

@Composable
fun DevicesScreen(
    viewModel: DevicesViewModel,
    onNavigateToDetails: (EsMeshDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var deviceToRename by remember { mutableStateOf<EsMeshDevice?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EsMeshBlack)
            .testTag("devices_screen")
    ) {
        EsMeshTopAppBar(
            title = "EsMesh",
            subtitle = "DEVICES",
            connectionStatus = if (uiState.connectedDeviceId != null) DeviceConnectionStatus.CONNECTED else DeviceConnectionStatus.DISCONNECTED
        )

        // Action Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0D12))
                .border(0.5.dp, EsMeshBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EsMeshCyberButton(
                text = if (uiState.isScanning) "SCANNING..." else "DISCOVER NODES",
                icon = if (uiState.isScanning) null else Icons.Default.Search,
                onClick = {
                    if (uiState.isScanning) viewModel.stopDiscovery() else viewModel.startDiscovery()
                },
                isPrimary = !uiState.isScanning,
                modifier = Modifier.weight(1f).testTag("scan_devices_btn")
            )

            EsMeshCyberButton(
                text = "MANUAL ADD",
                icon = Icons.Default.Add,
                onClick = { showAddDialog = true },
                isPrimary = false,
                modifier = Modifier.weight(1f).testTag("manual_add_btn")
            )
        }

        // Scan Status Banner
        if (uiState.isScanning || uiState.scanMessage.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141420))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.isScanning) {
                    CircularProgressIndicator(color = EsMeshYellow, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = uiState.scanMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshYellow,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Discovered Unsaved Devices Section
            if (uiState.discoveredDevices.isNotEmpty()) {
                item {
                    Text(
                        text = "DISCOVERED MESH NODES (${uiState.discoveredDevices.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = EsMeshYellow,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                items(uiState.discoveredDevices, key = { "disc_${it.id}" }) { device ->
                    DiscoveredDeviceItem(
                        device = device,
                        onConnect = { viewModel.connectDevice(device) },
                        onDetails = { onNavigateToDetails(device) }
                    )
                }
            }

            // Saved / Known Devices Section
            item {
                Text(
                    text = "SAVED GATEWAYS & NODES (${uiState.savedDevices.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshTextMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (uiState.savedDevices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EsMeshCharcoalCard)
                            .border(1.dp, EsMeshBorder, RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "NO SAVED NODES",
                                style = MaterialTheme.typography.titleMedium,
                                color = EsMeshTextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Discover Nodes' to auto-detect nearby ESP32 mesh nodes via mDNS or UDP.",
                                style = MaterialTheme.typography.bodySmall,
                                color = EsMeshTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(uiState.savedDevices, key = { it.id }) { device ->
                val isConnected = device.id == uiState.connectedDeviceId
                TechnicalDeviceCard(
                    device = device,
                    isConnected = isConnected,
                    onConnectClick = {
                        if (isConnected) viewModel.disconnect() else viewModel.connectDevice(device)
                    },
                    onDetailsClick = { onNavigateToDetails(device) }
                )
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
                viewModel.testManualConnection(ip, httpP, wsP) { result ->
                    result.onSuccess { dev ->
                        viewModel.connectDevice(dev)
                    }
                }
            }
        )
    }

    deviceToRename?.let { dev ->
        RenameDeviceDialog(
            currentName = dev.name,
            onDismiss = { deviceToRename = null },
            onRename = { newName ->
                viewModel.renameDevice(dev.id, newName)
                deviceToRename = null
            }
        )
    }
}

@Composable
fun DiscoveredDeviceItem(
    device: EsMeshDevice,
    onConnect: () -> Unit,
    onDetails: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF14141F))
            .border(1.dp, EsMeshYellow.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = EsMeshTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${device.ipAddress} • ${device.nodeId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshYellow,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                EsMeshCyberButton(
                    text = "CONNECT",
                    onClick = onConnect,
                    isPrimary = true,
                    modifier = Modifier.height(36.dp)
                )
            }
        }
    }
}
