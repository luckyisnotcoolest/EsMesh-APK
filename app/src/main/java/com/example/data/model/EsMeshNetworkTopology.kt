package com.example.data.model

data class MeshNodeItem(
    val nodeId: String,
    val name: String,
    val role: String, // "Gateway", "Mesh Node", "Leaf Node", "Repeater"
    val ipAddress: String,
    val parentNodeId: String?,
    val rssi: Int,
    val hops: Int,
    val lastSeenSecAgo: Int,
    val isReachable: Boolean = true
)

data class NetworkRouteItem(
    val destinationNode: String,
    val nextHopNode: String,
    val metricHops: Int,
    val signalStrength: Int,
    val state: String = "Active"
)

data class EsMeshNetworkTopology(
    val gatewayNodeId: String = "ESM-A1B2",
    val routerSsid: String = "MyRouter",
    val gatewayIp: String = "192.168.1.120",
    val totalNodes: Int = 4,
    val activeRoutes: Int = 6,
    val nodes: List<MeshNodeItem> = listOf(
        MeshNodeItem("ESM-A1B2", "ESP32 Gateway", "Gateway", "192.168.1.120", null, -48, 0, 1),
        MeshNodeItem("ESM-83C4", "Node Alpha (Living Room)", "Mesh Node", "10.0.0.2", "ESM-A1B2", -58, 1, 2),
        MeshNodeItem("ESM-94D5", "Node Beta (Workshop)", "Mesh Node", "10.0.0.3", "ESM-A1B2", -65, 1, 3),
        MeshNodeItem("ESM-11E6", "Node Gamma (Garden)", "Leaf Node", "10.0.0.4", "ESM-83C4", -78, 2, 5)
    ),
    val routes: List<NetworkRouteItem> = listOf(
        NetworkRouteItem("ESM-83C4", "Direct (ESM-A1B2)", 1, -58),
        NetworkRouteItem("ESM-94D5", "Direct (ESM-A1B2)", 1, -65),
        NetworkRouteItem("ESM-11E6", "Via ESM-83C4", 2, -78)
    )
)
