package com.example.repositories

import com.example.core.database.dao.DeviceDao
import com.example.core.database.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

class TrustedDeviceRepository(
    private val deviceDao: DeviceDao
) {
    fun getAllDevicesFlow(): Flow<List<DeviceEntity>> {
        return deviceDao.getAllDevicesFlow()
    }

    fun getDevicesForUser(userId: String): Flow<List<DeviceEntity>> {
        return deviceDao.getDevicesForUser(userId)
    }

    suspend fun getDeviceById(deviceId: String): DeviceEntity? {
        return deviceDao.getDeviceById(deviceId)
    }

    suspend fun registerOrUpdateDevice(device: DeviceEntity) {
        deviceDao.insertOrUpdateDevice(device)
    }

    suspend fun setTrustStatus(deviceId: String, isTrusted: Boolean) {
        deviceDao.updateTrustStatus(deviceId, isTrusted)
    }

    suspend fun renameDevice(deviceId: String, newName: String) {
        deviceDao.renameDevice(deviceId, newName)
    }

    suspend fun removeDevice(deviceId: String) {
        deviceDao.deleteDevice(deviceId)
    }

    suspend fun updateLastActive(deviceId: String) {
        deviceDao.updateLastActiveTime(deviceId, System.currentTimeMillis())
    }
}
