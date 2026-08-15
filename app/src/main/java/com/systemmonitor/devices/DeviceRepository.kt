package com.systemmonitor.devices

import kotlinx.coroutines.flow.Flow

class DeviceRepository(private val deviceDao: DeviceDao) {
    val allDevices: Flow<List<DeviceEntity>> = deviceDao.getAllDevices()

    suspend fun getDevice(deviceId: String): DeviceEntity? {
        return deviceDao.getDeviceById(deviceId)
    }

    suspend fun saveDevice(device: DeviceEntity) {
        deviceDao.insertOrUpdate(device)
    }

    suspend fun revokeDevice(deviceId: String) {
        deviceDao.revokeDevice(deviceId)
    }
}
