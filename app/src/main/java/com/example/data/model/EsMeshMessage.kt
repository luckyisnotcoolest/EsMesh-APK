package com.example.data.model

enum class MessageDeliveryStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED
}

data class EsMeshMessage(
    val id: String, // e.g. "MSG-83A1"
    val protocol: String = "EsMesh/1",
    val type: String = "message", // message, status, ping, pong, node_join, node_leave, route_update, ack
    val source: String = "NODE-A",
    val destination: String = "PHONE",
    val timestamp: Long = System.currentTimeMillis(),
    val ttl: Int = 5,
    val payload: String = "",
    val deliveryStatus: MessageDeliveryStatus = MessageDeliveryStatus.DELIVERED,
    val isOutgoing: Boolean = false,
    val errorMessage: String? = null
)
