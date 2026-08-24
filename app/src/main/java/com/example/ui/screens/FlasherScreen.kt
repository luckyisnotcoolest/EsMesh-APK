package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.flasher.FlashStatus
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
import com.example.ui.viewmodel.FlasherViewModel

@Composable
fun FlasherScreen(
    viewModel: FlasherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onFileSelected(context, it) }
    }

    LaunchedEffect(Unit) {
        viewModel.detectUsbDevices(context)
    }

    val isFlashing = uiState.flashStatus is FlashStatus.Flashing || uiState.flashStatus is FlashStatus.Connecting

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EsMeshBlack)
            .testTag("flasher_screen")
    ) {
        EsMeshTopAppBar(
            title = "EsMesh",
            subtitle = "FLASHER",
            connectionStatus = DeviceConnectionStatus.DISCONNECTED,
            actions = {
                IconButton(onClick = { viewModel.detectUsbDevices(context) }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh USB", tint = EsMeshTextPrimary)
                }
            }
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

            // Step 1: Select Firmware Binary
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EsMeshCharcoalCard)
                        .border(1.dp, EsMeshBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "STEP 1: SELECT ESP32 FIRMWARE (.BIN)",
                            style = MaterialTheme.typography.labelSmall,
                            color = EsMeshYellow,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (uiState.fileName != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Selected File: ${uiState.fileName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EsMeshTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Size: ${uiState.fileSizeFormatted} • Target: ${uiState.chipTarget}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EsMeshGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            Text(
                                text = "Choose a compiled EsMesh firmware image (.bin) from device storage.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EsMeshTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        EsMeshCyberButton(
                            text = if (uiState.fileName != null) "CHANGE BINARY" else "SELECT BINARY FILE",
                            icon = Icons.Default.FileOpen,
                            onClick = { filePickerLauncher.launch("*/*") },
                            isPrimary = uiState.fileName == null,
                            enabled = !isFlashing,
                            modifier = Modifier.fillMaxWidth().testTag("select_bin_btn")
                        )
                    }
                }
            }

            // Step 2: USB OTG Connection
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EsMeshCharcoalCard)
                        .border(1.dp, EsMeshBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "STEP 2: USB OTG SERIAL CONNECTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = EsMeshYellow,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (uiState.detectedUsbDevices.isEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Usb, contentDescription = null, tint = EsMeshTextMuted, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "No USB OTG Device Detected",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = EsMeshTextMuted
                                    )
                                    Text(
                                        text = "Connect ESP32 via USB OTG adapter and tap Refresh.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EsMeshTextSecondary
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                uiState.detectedUsbDevices.forEach { dev ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF181824))
                                            .border(1.dp, if (dev == uiState.selectedUsbDevice) EsMeshRed else EsMeshBorder, RoundedCornerShape(6.dp))
                                            .clickable { viewModel.selectUsbDevice(dev) }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = dev.productName ?: "ESP32 USB Serial Node",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = EsMeshTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "VID: 0x${Integer.toHexString(dev.vendorId).uppercase()} PID: 0x${Integer.toHexString(dev.productId).uppercase()}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EsMeshYellow,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        if (dev == uiState.selectedUsbDevice) {
                                            Text(text = "SELECTED", color = EsMeshRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Baud Rate Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Flashing Baud Rate:", color = EsMeshTextSecondary, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${uiState.baudRate} bps",
                                color = EsMeshYellow,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Step 3: Flash Progress & Trigger
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EsMeshCharcoalCard)
                        .border(1.dp, if (isFlashing) EsMeshRed else EsMeshBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FLASHING STATUS",
                                style = MaterialTheme.typography.labelSmall,
                                color = EsMeshYellow,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.progressPercent}%",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isFlashing) EsMeshRed else EsMeshTextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { uiState.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = EsMeshRed,
                            trackColor = Color(0xFF20202C)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = uiState.currentStage,
                            style = MaterialTheme.typography.bodySmall,
                            color = EsMeshTextPrimary,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isFlashing) {
                            EsMeshCyberButton(
                                text = "CANCEL FLASHING",
                                icon = Icons.Default.Cancel,
                                onClick = { viewModel.cancelFlashing() },
                                isPrimary = false,
                                isDestructive = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            val canFlash = uiState.selectedFileUri != null && uiState.selectedUsbDevice != null
                            EsMeshCyberButton(
                                text = "START FIRMWARE FLASH",
                                icon = Icons.Default.Memory,
                                onClick = { viewModel.flashFirmware(context) },
                                isPrimary = true,
                                enabled = canFlash,
                                modifier = Modifier.fillMaxWidth().testTag("start_flash_btn")
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
