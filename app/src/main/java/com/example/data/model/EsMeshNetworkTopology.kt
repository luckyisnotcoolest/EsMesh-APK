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
    val gatewayNodeId: String = "",
    val routerSsid: String = "",
    val gatewayIp: String = "",
    val totalNodes: Int = 0,
    val activeRoutes: Int = 0,
    val nodes: List<MeshNodeItem> = emptyList(),
    val routes: List<NetworkRouteItem> = emptyList()
)

