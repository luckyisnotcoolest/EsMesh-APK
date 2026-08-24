package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshCharcoal
import com.example.ui.theme.EsMeshCharcoalCard
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshTextSecondary
import com.example.ui.theme.EsMeshYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceManuallyDialog(
    onDismiss: () -> Unit,
    onTestAndAdd: (ip: String, httpPort: Int, wsPort: Int) -> Unit
) {
    var ipAddress by remember { mutableStateOf("192.168.1.120") }
    var httpPort by remember { mutableStateOf("80") }
    var wsPort by remember { mutableStateOf("80") }
    var protocol by remember { mutableStateOf("EsMesh/1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ADD DEVICE MANUALLY",
                style = MaterialTheme.typography.titleLarge,
                color = EsMeshTextPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Configure direct network endpoints to communicate with an ESP32 EsMesh node.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EsMeshTextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("IP Address (e.g. 192.168.1.120)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EsMeshRed,
                        unfocusedBorderColor = EsMeshBorder,
                        focusedLabelColor = EsMeshYellow,
                        unfocusedLabelColor = EsMeshTextSecondary,
                        focusedTextColor = EsMeshTextPrimary,
                        unfocusedTextColor = EsMeshTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("manual_ip_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = httpPort,
                        onValueChange = { httpPort = it },
                        label = { Text("HTTP Port") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EsMeshRed,
                            unfocusedBorderColor = EsMeshBorder,
                            focusedLabelColor = EsMeshYellow,
                            focusedTextColor = EsMeshTextPrimary,
                            unfocusedTextColor = EsMeshTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = wsPort,
                        onValueChange = { wsPort = it },
                        label = { Text("WebSocket Port") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EsMeshRed,
                            unfocusedBorderColor = EsMeshBorder,
                            focusedLabelColor = EsMeshYellow,
                            focusedTextColor = EsMeshTextPrimary,
                            unfocusedTextColor = EsMeshTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = protocol,
                    onValueChange = { protocol = it },
                    label = { Text("Protocol Version") },
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EsMeshBorder,
                        unfocusedBorderColor = EsMeshBorder,
                        focusedLabelColor = EsMeshYellow,
                        focusedTextColor = EsMeshYellow,
                        unfocusedTextColor = EsMeshYellow
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            EsMeshCyberButton(
                text = "TEST CONNECTION",
                onClick = {
                    val httpP = httpPort.toIntOrNull() ?: 80
                    val wsP = wsPort.toIntOrNull() ?: 80
                    onTestAndAdd(ipAddress.trim(), httpP, wsP)
                },
                modifier = Modifier.testTag("test_and_add_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = EsMeshTextSecondary, fontFamily = FontFamily.Monospace)
            }
        },
        containerColor = EsMeshCharcoalCard,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun RenameDeviceDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "RENAME NODE",
                style = MaterialTheme.typography.titleLarge,
                color = EsMeshTextPrimary,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Node Display Name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EsMeshRed,
                    unfocusedBorderColor = EsMeshBorder,
                    focusedLabelColor = EsMeshYellow,
                    focusedTextColor = EsMeshTextPrimary,
                    unfocusedTextColor = EsMeshTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            EsMeshCyberButton(
                text = "SAVE",
                onClick = { onRename(name.trim()) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = EsMeshTextSecondary, fontFamily = FontFamily.Monospace)
            }
        },
        containerColor = EsMeshCharcoalCard,
        shape = RoundedCornerShape(12.dp)
    )
}
