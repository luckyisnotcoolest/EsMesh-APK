package com.example.data.model

data class EsMeshConfig(
    // GENERAL
    val deviceName: String = "ESP32 Gateway",
    val nodeId: String = "ESM-A1B2",
    val operatingMode: String = "Gateway", // "Gateway", "Node", "Repeater"

    // WI-FI
    val wifiSsid: String = "MyRouter",
    val wifiPassword: String = "",
    val isDhcp: Boolean = true,
    val staticIp: String = "192.168.1.120",
    val gateway: String = "192.168.1.1",
    val subnet: String = "255.255.255.0",
    val dns: String = "8.8.8.8",

    // AP
    val apEnabled: Boolean = true,
    val apSsid: String = "EsMesh-Gateway-A1B2",
    val apPassword: String = "esmesh1234",
    val apChannel: Int = 6,
    val maxClients: Int = 4,

    // NETWORK
    val httpPort: Int = 80,
    val wsPort: Int = 80,
    val mdnsName: String = "espmesh",
    val discoveryIntervalSec: Int = 10,
    val timeoutSec: Int = 5,
    val retryCount: Int = 3,

    // MESH
    val meshEnabled: Boolean = true,
    val maxHops: Int = 8,
    val ttl: Int = 5,
    val repeaterMode: Boolean = false,

    // SECURITY
    val apiAuthEnabled: Boolean = false,
    val authToken: String = "",
    val encryptionEnabled: Boolean = false,
    val encryptionKey: String = ""
)
