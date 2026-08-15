package com.systemmonitor.devices

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM paired_devices WHERE isRevoked = 0 ORDER BY lastSeen DESC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM paired_devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getDeviceById(deviceId: String): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(device: DeviceEntity)

    @Query("UPDATE paired_devices SET isRevoked = 1 WHERE deviceId = :deviceId")
    suspend fun revokeDevice(deviceId: String)

    @Delete
    suspend fun delete(device: DeviceEntity)
}
