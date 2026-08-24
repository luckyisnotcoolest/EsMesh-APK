package com.example.data.protocol

import com.example.data.model.EsMeshMessage
import com.example.data.model.MessageDeliveryStatus
import org.json.JSONException
import org.json.JSONObject

sealed class ProtocolValidationResult {
    data class Success(val message: EsMeshMessage) : ProtocolValidationResult()
    data class Failure(val reason: String, val rawJson: String) : ProtocolValidationResult()
}

object EsMeshProtocolValidator {
    const val CURRENT_PROTOCOL = "EsMesh/1"
    const val MAX_PAYLOAD_BYTES = 4096
    const val MAX_TTL = 15

    private val supportedTypes = setOf(
        "message",
        "status",
        "ping",
        "pong",
        "node_join",
        "node_leave",
        "route_update",
        "ack",
        "config_req",
        "config_ack"
    )

    fun validateAndParse(rawJson: String): ProtocolValidationResult {
        if (rawJson.isBlank()) {
            return ProtocolValidationResult.Failure("Empty JSON message received", rawJson)
        }

        try {
            val json = JSONObject(rawJson)

            // Validate protocol header
            if (!json.has("protocol")) {
                return ProtocolValidationResult.Failure("Missing 'protocol' field in payload", rawJson)
            }
            val protocol = json.optString("protocol")
            if (protocol != CURRENT_PROTOCOL) {
                return ProtocolValidationResult.Failure(
                    "Unsupported protocol version '$protocol'. Expected '$CURRENT_PROTOCOL'",
                    rawJson
                )
            }

            // Validate message type
            if (!json.has("type")) {
                return ProtocolValidationResult.Failure("Missing 'type' field in payload", rawJson)
            }
            val type = json.optString("type")
            if (!supportedTypes.contains(type.lowercase())) {
                return ProtocolValidationResult.Failure("Unknown or unsupported message type '$type'", rawJson)
            }

            // Validate message id
            if (!json.has("id") || json.optString("id").isBlank()) {
                return ProtocolValidationResult.Failure("Missing or empty 'id' field", rawJson)
            }
            val id = json.optString("id")

            // Validate source & destination
            if (!json.has("source") || json.optString("source").isBlank()) {
                return ProtocolValidationResult.Failure("Missing 'source' address in message", rawJson)
            }
            val source = json.optString("source")

            if (!json.has("destination") || json.optString("destination").isBlank()) {
                return ProtocolValidationResult.Failure("Missing 'destination' address in message", rawJson)
            }
            val destination = json.optString("destination")

            // Validate timestamp
            val timestamp = json.optLong("timestamp", System.currentTimeMillis())

            // Validate TTL
            val ttl = json.optInt("ttl", 5)
            if (ttl <= 0) {
                return ProtocolValidationResult.Failure("Expired TTL (ttl=$ttl)", rawJson)
            }
            if (ttl > MAX_TTL) {
                return ProtocolValidationResult.Failure("Excessive TTL (ttl=$ttl > $MAX_TTL)", rawJson)
            }

            // Validate payload
            val payload = json.optString("payload", "")
            if (payload.toByteArray(Charsets.UTF_8).size > MAX_PAYLOAD_BYTES) {
                return ProtocolValidationResult.Failure("Payload size exceeds maximum allowed $MAX_PAYLOAD_BYTES bytes", rawJson)
            }

            val message = EsMeshMessage(
                id = id,
                protocol = protocol,
                type = type,
                source = source,
                destination = destination,
                timestamp = timestamp,
                ttl = ttl,
                payload = payload,
                deliveryStatus = MessageDeliveryStatus.DELIVERED,
                isOutgoing = false
            )

            return ProtocolValidationResult.Success(message)

        } catch (e: JSONException) {
            return ProtocolValidationResult.Failure("Malformed JSON format: ${e.localizedMessage}", rawJson)
        } catch (e: Exception) {
            return ProtocolValidationResult.Failure("Protocol parse error: ${e.localizedMessage}", rawJson)
        }
    }

    fun serializeMessage(message: EsMeshMessage): String {
        val json = JSONObject()
        json.put("protocol", message.protocol)
        json.put("type", message.type)
        json.put("id", message.id)
        json.put("source", message.source)
        json.put("destination", message.destination)
        json.put("timestamp", message.timestamp)
        json.put("ttl", message.ttl)
        json.put("payload", message.payload)
        return json.toString()
    }
}
