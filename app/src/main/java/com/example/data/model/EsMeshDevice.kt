package com.example.data.model

enum class DeviceConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class EsMeshDevice(
    val id: String, // Unique identifier e.g. "ESM-A1B2" or IP
    val name: String = "ESP32 Gateway",
    val model: String = "ESP32-S3",
    val chip: String = "ESP32-S3-WROOM-1 (Revision v0.1)",
    val nodeId: String = "ESM-A1B2",
    val ipAddress: String = "192.168.1.120",
    val httpPort: Int = 80,
    val wsPort: Int = 80,
    val macAddress: String = "84:F7:03:A1:B2:C3",
    val rssi: Int = -48,
    val firmwareVersion: String = "v1.4.2-release",
    val protocolVersion: String = "EsMesh/1",
    val capabilities: List<String> = listOf(
        "wifi_sta",
        "wifi_ap",
        "wifi_2_4ghz",
        "websocket",
        "http",
        "mdns",
        "mesh",
        "repeater",
        "usb_update",
        "ota_update"
    ),
    val wifiMode: String = "STA+AP",
    val wifiBand: String = "2.4 GHz",
    val ssid: String = "MyRouter",
    val uptimeSeconds: Long = 86420L,
    val lastSeen: Long = System.currentTimeMillis(),
    val connectionStatus: DeviceConnectionStatus = DeviceConnectionStatus.DISCONNECTED,
    val totalMessages: Int = 12,
    val meshNodesCount: Int = 4,
    val totalPackets: Int = 345,
    val isFavorite: Boolean = false
) {
    fun hasCapability(cap: String): Boolean = capabilities.contains(cap.lowercase())

    val isOnline: Boolean
        get() = connectionStatus == DeviceConnectionStatus.CONNECTED

    val webInterfaceUrl: String
        get() = if (httpPort == 80) "http://$ipAddress/" else "http://$ipAddress:$httpPort/"

    val websocketUrl: String
        get() = if (wsPort == 80) "ws://$ipAddress/api/v1/ws" else "ws://$ipAddress:$wsPort/api/v1/ws"
}
