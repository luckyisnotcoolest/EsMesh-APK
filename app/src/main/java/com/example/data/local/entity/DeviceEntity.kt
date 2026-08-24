package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DeviceConnectionStatus
import com.example.data.model.EsMeshDevice

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val model: String,
    val chip: String,
    val nodeId: String,
    val ipAddress: String,
    val httpPort: Int,
    val wsPort: Int,
    val macAddress: String,
    val rssi: Int,
    val firmwareVersion: String,
    val protocolVersion: String,
    val capabilitiesCsv: String,
    val wifiMode: String,
    val wifiBand: String,
    val ssid: String,
    val uptimeSeconds: Long,
    val lastSeen: Long,
    val isFavorite: Boolean = false
) {
    fun toDomain(connectionStatus: DeviceConnectionStatus = DeviceConnectionStatus.DISCONNECTED): EsMeshDevice {
        val caps = if (capabilitiesCsv.isBlank()) emptyList() else capabilitiesCsv.split(",").map { it.trim() }
        return EsMeshDevice(
            id = id,
            name = name,
            model = model,
            chip = chip,
            nodeId = nodeId,
            ipAddress = ipAddress,
            httpPort = httpPort,
            wsPort = wsPort,
            macAddress = macAddress,
            rssi = rssi,
            firmwareVersion = firmwareVersion,
            protocolVersion = protocolVersion,
            capabilities = caps,
            wifiMode = wifiMode,
            wifiBand = wifiBand,
            ssid = ssid,
            uptimeSeconds = uptimeSeconds,
            lastSeen = lastSeen,
            connectionStatus = connectionStatus,
            isFavorite = isFavorite
        )
    }

    companion object {
        fun fromDomain(device: EsMeshDevice): DeviceEntity {
            return DeviceEntity(
                id = device.id,
                name = device.name,
                model = device.model,
                chip = device.chip,
                nodeId = device.nodeId,
                ipAddress = device.ipAddress,
                httpPort = device.httpPort,
                wsPort = device.wsPort,
                macAddress = device.macAddress,
                rssi = device.rssi,
                firmwareVersion = device.firmwareVersion,
                protocolVersion = device.protocolVersion,
                capabilitiesCsv = device.capabilities.joinToString(","),
                wifiMode = device.wifiMode,
                wifiBand = device.wifiBand,
                ssid = device.ssid,
                uptimeSeconds = device.uptimeSeconds,
                lastSeen = device.lastSeen,
                isFavorite = device.isFavorite
            )
        }
    }
}
