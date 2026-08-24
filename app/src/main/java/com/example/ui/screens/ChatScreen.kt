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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.EsMeshMessage
import com.example.data.model.MessageDeliveryStatus
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
import com.example.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EsMeshBlack)
            .testTag("chat_screen")
    ) {
        EsMeshTopAppBar(
            title = "EsMesh",
            subtitle = "CHAT",
            connectionStatus = if (uiState.isConnected) DeviceConnectionStatus.CONNECTED else DeviceConnectionStatus.DISCONNECTED,
            actions = {
                IconButton(onClick = { viewModel.clearConversation() }) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = "Clear History",
                        tint = EsMeshTextSecondary
                    )
                }
            }
        )

        // Search and Destination Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C0C10))
                .border(0.5.dp, EsMeshBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search messages...", color = EsMeshTextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = EsMeshTextMuted, modifier = Modifier.size(18.dp))
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EsMeshRed,
                    unfocusedBorderColor = EsMeshBorder,
                    focusedTextColor = EsMeshTextPrimary,
                    unfocusedTextColor = EsMeshTextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Destination badge selector
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1B1B26))
                    .border(0.8.dp, EsMeshBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "TO: ${uiState.destinationNode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = EsMeshYellow,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (uiState.messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "NO MESSAGES IN MESH",
                                style = MaterialTheme.typography.titleMedium,
                                color = EsMeshTextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (uiState.isConnected) "Type a message below to broadcast to all nodes." else "Connect to an ESP32 gateway to exchange mesh packets.",
                                style = MaterialTheme.typography.bodySmall,
                                color = EsMeshTextSecondary
                            )
                        }
                    }
                }
            }

            items(uiState.messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    message = msg,
                    onRetry = { viewModel.retryMessage(msg) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Message Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EsMeshCharcoalCard)
                .border(0.5.dp, EsMeshBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = { viewModel.onInputTextChanged(it) },
                placeholder = { Text("Enter mesh packet payload...", color = EsMeshTextMuted, fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EsMeshRed,
                    unfocusedBorderColor = EsMeshBorder,
                    focusedTextColor = EsMeshTextPrimary,
                    unfocusedTextColor = EsMeshTextPrimary
                ),
                maxLines = 3,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (uiState.inputText.isNotBlank() && !uiState.isSending) EsMeshRed else Color(0xFF22222E))
                    .border(1.dp, EsMeshBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isSending) {
                    CircularProgressIndicator(color = EsMeshYellow, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(
                        onClick = { viewModel.sendMessage() },
                        enabled = uiState.inputText.isNotBlank(),
                        modifier = Modifier.testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (uiState.inputText.isNotBlank()) Color.White else EsMeshTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: EsMeshMessage,
    onRetry: () -> Unit
) {
    val isOut = message.isOutgoing
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    val bubbleBg = if (isOut) Color(0xFF22080B) else Color(0xFF14141E)
    val bubbleBorder = if (isOut) EsMeshRed.copy(alpha = 0.5f) else EsMeshBorder

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOut) Alignment.End else Alignment.Start
    ) {
        // Node identifier / routing info header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${message.source} → ${message.destination}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isOut) EsMeshRed else EsMeshYellow,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Text(
                text = "• TTL ${message.ttl}",
                style = MaterialTheme.typography.labelSmall,
                color = EsMeshTextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Message body
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = if (isOut) 10.dp else 2.dp,
                        bottomEnd = if (isOut) 2.dp else 10.dp
                    )
                )
                .background(bubbleBg)
                .border(
                    width = 1.dp,
                    color = bubbleBorder,
                    shape = RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = if (isOut) 10.dp else 2.dp,
                        bottomEnd = if (isOut) 2.dp else 10.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.payload,
                style = MaterialTheme.typography.bodyMedium,
                color = EsMeshTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Status & Timestamp Footer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = EsMeshTextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            )

            if (isOut) {
                when (message.deliveryStatus) {
                    MessageDeliveryStatus.PENDING -> {
                        Text(text = "SENDING...", color = EsMeshYellow, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    MessageDeliveryStatus.SENT, MessageDeliveryStatus.DELIVERED -> {
                        Text(text = "✓ DELIVERED", color = EsMeshGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    MessageDeliveryStatus.FAILED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(text = "FAILED", color = EsMeshRed, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            IconButton(onClick = onRetry, modifier = Modifier.size(16.dp)) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry", tint = EsMeshRed)
                            }
                        }
                    }
                }
            }
        }
    }
}
