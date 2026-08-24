package com.example.data.network

import android.util.Log
import com.example.data.model.EsMeshMessage
import com.example.data.protocol.EsMeshProtocolValidator
import com.example.data.protocol.ProtocolValidationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

sealed class WsConnectionState {
    object Disconnected : WsConnectionState()
    object Connecting : WsConnectionState()
    data class Connected(val url: String) : WsConnectionState()
    data class Reconnecting(val attempt: Int) : WsConnectionState()
    data class Failed(val reason: String) : WsConnectionState()
}

class EsMeshWebSocketClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
) {
    private val _connectionState = MutableStateFlow<WsConnectionState>(WsConnectionState.Disconnected)
    val connectionState: StateFlow<WsConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<EsMeshMessage>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<EsMeshMessage> = _incomingMessages.asSharedFlow()

    private val _rawLogs = MutableSharedFlow<String>(extraBufferCapacity = 128)
    val rawLogs: SharedFlow<String> = _rawLogs.asSharedFlow()

    private var webSocket: WebSocket? = null
    private var currentUrl: String? = null
    private var shouldKeepConnected = false
    private var reconnectAttempts = 0
    private var reconnectJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    fun connect(ip: String, port: Int = 80) {
        val url = if (port == 80) "ws://$ip/api/v1/ws" else "ws://$ip:$port/api/v1/ws"
        disconnect()
        shouldKeepConnected = true
        currentUrl = url
        startConnection(url)
    }

    private fun startConnection(url: String) {
        _connectionState.value = if (reconnectAttempts > 0) {
            WsConnectionState.Reconnecting(reconnectAttempts)
        } else {
            WsConnectionState.Connecting
        }

        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                reconnectAttempts = 0
                _connectionState.value = WsConnectionState.Connected(url)
                scope.launch {
                    _rawLogs.emit("[WS OPEN] Connected to $url")
                }
            }

            override fun onMessage(ws: WebSocket, text: String) {
                scope.launch {
                    _rawLogs.emit("[WS RX] $text")
                    when (val result = EsMeshProtocolValidator.validateAndParse(text)) {
                        is ProtocolValidationResult.Success -> {
                            _incomingMessages.emit(result.message)
                        }
                        is ProtocolValidationResult.Failure -> {
                            Log.w("EsMeshWS", "Invalid message rejected: ${result.reason}")
                            _rawLogs.emit("[WS ERROR] Rejected frame: ${result.reason}")
                        }
                    }
                }
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                ws.close(1000, null)
                _connectionState.value = WsConnectionState.Disconnected
                scope.launch {
                    _rawLogs.emit("[WS CLOSING] Code $code: $reason")
                }
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                _connectionState.value = WsConnectionState.Disconnected
                scope.launch {
                    _rawLogs.emit("[WS CLOSED] Code $code: $reason")
                }
                scheduleReconnect()
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                val errorMsg = t.localizedMessage ?: "WebSocket Connection Failed"
                _connectionState.value = WsConnectionState.Failed(errorMsg)
                scope.launch {
                    _rawLogs.emit("[WS FAILURE] $errorMsg")
                }
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (!shouldKeepConnected) return
        val url = currentUrl ?: return

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            reconnectAttempts++
            val backoffDelayMs = (reconnectAttempts * 2000L).coerceAtMost(10000L)
            _rawLogs.emit("[WS] Reconnecting in ${backoffDelayMs / 1000}s (Attempt $reconnectAttempts)...")
            delay(backoffDelayMs)
            if (isActive && shouldKeepConnected) {
                startConnection(url)
            }
        }
    }

    fun sendMessage(message: EsMeshMessage): Boolean {
        val ws = webSocket
        if (ws == null || _connectionState.value !is WsConnectionState.Connected) {
            return false
        }
        val serialized = EsMeshProtocolValidator.serializeMessage(message)
        val success = ws.send(serialized)
        scope.launch {
            _rawLogs.emit("[WS TX] $serialized (Success: $success)")
        }
        return success
    }

    fun disconnect() {
        shouldKeepConnected = false
        reconnectAttempts = 0
        reconnectJob?.cancel()
        reconnectJob = null
        try {
            webSocket?.close(1000, "User initiated disconnect")
        } catch (_: Exception) {}
        webSocket = null
        _connectionState.value = WsConnectionState.Disconnected
    }
}
