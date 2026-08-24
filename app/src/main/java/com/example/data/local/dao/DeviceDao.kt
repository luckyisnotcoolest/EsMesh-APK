package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY isFavorite DESC, lastSeen DESC")
    fun getAllDevicesFlow(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :id LIMIT 1")
    suspend fun getDeviceById(id: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE ipAddress = :ip LIMIT 1")
    suspend fun getDeviceByIp(ip: String): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(device: DeviceEntity)

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE id = :id")
    suspend fun deleteDeviceById(id: String)

    @Query("UPDATE devices SET name = :newName WHERE id = :id")
    suspend fun updateDeviceName(id: String, newName: String)

    @Query("UPDATE devices SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: String, isFav: Boolean)

    @Query("DELETE FROM devices")
    suspend fun clearAllDevices()
}
