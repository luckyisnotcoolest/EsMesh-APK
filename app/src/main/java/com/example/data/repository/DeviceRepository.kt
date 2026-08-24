package com.example.data.repository

import com.example.data.local.dao.DeviceDao
import com.example.data.local.entity.DeviceEntity
import com.example.data.model.DeviceConnectionStatus
import com.example.data.model.EsMeshDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeviceRepository(private val deviceDao: DeviceDao) {

    fun getSavedDevicesFlow(connectedDeviceId: String?): Flow<List<EsMeshDevice>> {
        return deviceDao.getAllDevicesFlow().map { list ->
            list.map { entity ->
                val status = if (entity.id == connectedDeviceId) {
                    DeviceConnectionStatus.CONNECTED
                } else {
                    DeviceConnectionStatus.DISCONNECTED
                }
                entity.toDomain(status)
            }
        }
    }

    suspend fun getDeviceById(id: String): EsMeshDevice? {
        return deviceDao.getDeviceById(id)?.toDomain()
    }

    suspend fun saveDevice(device: EsMeshDevice) {
        deviceDao.insertOrUpdateDevice(DeviceEntity.fromDomain(device))
    }

    suspend fun renameDevice(id: String, newName: String) {
        deviceDao.updateDeviceName(id, newName)
    }

    suspend fun setFavorite(id: String, isFav: Boolean) {
        deviceDao.setFavorite(id, isFav)
    }

    suspend fun removeDevice(id: String) {
        deviceDao.deleteDeviceById(id)
    }

    suspend fun clearAll() {
        deviceDao.clearAllDevices()
    }
}
