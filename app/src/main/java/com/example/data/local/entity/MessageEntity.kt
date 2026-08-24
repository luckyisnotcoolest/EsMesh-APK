package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.EsMeshMessage
import com.example.data.model.MessageDeliveryStatus

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val messageId: String,
    val protocol: String,
    val type: String,
    val source: String,
    val destination: String,
    val timestamp: Long,
    val ttl: Int,
    val payload: String,
    val deliveryStatus: String, // "PENDING", "SENT", "DELIVERED", "FAILED"
    val isOutgoing: Boolean,
    val errorMessage: String? = null
) {
    fun toDomain(): EsMeshMessage {
        val status = try {
            MessageDeliveryStatus.valueOf(deliveryStatus)
        } catch (_: Exception) {
            MessageDeliveryStatus.DELIVERED
        }
        return EsMeshMessage(
            id = messageId,
            protocol = protocol,
            type = type,
            source = source,
            destination = destination,
            timestamp = timestamp,
            ttl = ttl,
            payload = payload,
            deliveryStatus = status,
            isOutgoing = isOutgoing,
            errorMessage = errorMessage
        )
    }

    companion object {
        fun fromDomain(msg: EsMeshMessage): MessageEntity {
            return MessageEntity(
                messageId = msg.id,
                protocol = msg.protocol,
                type = msg.type,
                source = msg.source,
                destination = msg.destination,
                timestamp = msg.timestamp,
                ttl = msg.ttl,
                payload = msg.payload,
                deliveryStatus = msg.deliveryStatus.name,
                isOutgoing = msg.isOutgoing,
                errorMessage = msg.errorMessage
            )
        }
    }
}
