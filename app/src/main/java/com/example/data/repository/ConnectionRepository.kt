package com.example.data.repository

import com.example.data.model.DeviceConnectionStatus
import com.example.data.model.EsMeshDevice
import com.example.data.model.EsMeshMessage
import com.example.data.model.MessageDeliveryStatus
import com.example.data.network.EsMeshHttpClient
import com.example.data.network.EsMeshWebSocketClient
import com.example.data.network.NetworkResponse
import com.example.data.network.WsConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectionRepository(
    val httpClient: EsMeshHttpClient,
    val webSocketClient: EsMeshWebSocketClient,
    private val deviceRepository: DeviceRepository,
    private val messageRepository: MessageRepository
) {
    private val _connectedDevice = MutableStateFlow<EsMeshDevice?>(null)
    val connectedDevice: StateFlow<EsMeshDevice?> = _connectedDevice.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    val wsState: StateFlow<WsConnectionState> = webSocketClient.connectionState
    val incomingMessages: SharedFlow<EsMeshMessage> = webSocketClient.incomingMessages
    val wsLogs: SharedFlow<String> = webSocketClient.rawLogs

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        scope.launch {
            incomingMessages.collect { msg ->
                messageRepository.insertMessage(msg)
                _connectedDevice.value?.let { dev ->
                    _connectedDevice.value = dev.copy(
                        totalMessages = dev.totalMessages + 1,
                        totalPackets = dev.totalPackets + 1,
                        lastSeen = System.currentTimeMillis()
                    )
                }
            }
        }

        scope.launch {
            wsState.collect { state ->
                when (state) {
                    is WsConnectionState.Connected -> {
                        _connectedDevice.value = _connectedDevice.value?.copy(
                            connectionStatus = DeviceConnectionStatus.CONNECTED
                        )
                    }
                    is WsConnectionState.Disconnected -> {
                        _connectedDevice.value = _connectedDevice.value?.copy(
                            connectionStatus = DeviceConnectionStatus.DISCONNECTED
                        )
                    }
                    is WsConnectionState.Failed -> {
                        _lastError.value = state.reason
                        _connectedDevice.value = _connectedDevice.value?.copy(
                            connectionStatus = DeviceConnectionStatus.ERROR
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    suspend fun testConnection(ip: String, httpPort: Int = 80, wsPort: Int = 80): Result<EsMeshDevice> {
        _lastError.value = null
        when (val res = httpClient.getDevice(ip, httpPort)) {
            is NetworkResponse.Success -> {
                val dev = res.data.copy(
                    wsPort = wsPort,
                    connectionStatus = DeviceConnectionStatus.DISCONNECTED
                )
                return Result.success(dev)
            }
            is NetworkResponse.Error -> {
                _lastError.value = "HTTP Error ${res.code}: ${res.message}"
                return Result.failure(Exception("HTTP Error ${res.code}: ${res.message}"))
            }
            is NetworkResponse.Failure -> {
                _lastError.value = res.exception.localizedMessage ?: "Connection timed out"
                return Result.failure(res.exception)
            }
        }
    }

    fun connectDevice(device: EsMeshDevice) {
        _isConnecting.value = true
        _lastError.value = null
        _connectedDevice.value = device.copy(connectionStatus = DeviceConnectionStatus.CONNECTING)

        scope.launch {
            // Save or update device in database
            deviceRepository.saveDevice(device)

            // Connect to WebSocket endpoint
            webSocketClient.connect(device.ipAddress, device.wsPort)
            _isConnecting.value = false
        }
    }

    fun disconnect() {
        webSocketClient.disconnect()
        _connectedDevice.value = null
        _isConnecting.value = false
    }

    suspend fun sendMessage(content: String, destinationNode: String = "BROADCAST"): EsMeshMessage {
        val currentDev = _connectedDevice.value
        val sourceNode = currentDev?.nodeId ?: "PHONE"
        val msgId = "MSG-" + (1000..9999).random().toString(16).uppercase()

        val msg = EsMeshMessage(
            id = msgId,
            protocol = "EsMesh/1",
            type = "message",
            source = sourceNode,
            destination = destinationNode,
            timestamp = System.currentTimeMillis(),
            ttl = 5,
            payload = content,
            deliveryStatus = MessageDeliveryStatus.PENDING,
            isOutgoing = true
        )

        messageRepository.insertMessage(msg)

        val sentViaWs = webSocketClient.sendMessage(msg)
        if (sentViaWs) {
            messageRepository.updateStatus(msg.id, MessageDeliveryStatus.DELIVERED)
            return msg.copy(deliveryStatus = MessageDeliveryStatus.DELIVERED)
        } else if (currentDev != null) {
            // Try HTTP fallback post
            when (httpClient.postMessage(currentDev.ipAddress, currentDev.httpPort, msg)) {
                is NetworkResponse.Success -> {
                    messageRepository.updateStatus(msg.id, MessageDeliveryStatus.DELIVERED)
                    return msg.copy(deliveryStatus = MessageDeliveryStatus.DELIVERED)
                }
                else -> {
                    messageRepository.updateStatus(msg.id, MessageDeliveryStatus.FAILED)
                    return msg.copy(deliveryStatus = MessageDeliveryStatus.FAILED, errorMessage = "Failed to transmit packet")
                }
            }
        } else {
            messageRepository.updateStatus(msg.id, MessageDeliveryStatus.FAILED)
            return msg.copy(deliveryStatus = MessageDeliveryStatus.FAILED, errorMessage = "No active gateway connection")
        }
    }
}
