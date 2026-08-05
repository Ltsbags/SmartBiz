package com.example.repositories

import com.example.core.database.dao.DeviceDao
import com.example.core.database.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

class DeviceRepository(private val deviceDao: DeviceDao) {

    fun getDevicesForUser(userId: String): Flow<List<DeviceEntity>> {
        return deviceDao.getDevicesForUser(userId)
    }

    suspend fun getDevicesList(userId: String): List<DeviceEntity> {
        return deviceDao.getDevicesListForUser(userId)
    }

    suspend fun registerOrUpdateDevice(device: DeviceEntity) {
        deviceDao.insertOrUpdateDevice(device)
    }

    suspend fun setDeviceTrustStatus(deviceId: String, isTrusted: Boolean) {
        deviceDao.updateTrustStatus(deviceId, isTrusted)
    }

    suspend fun removeDevice(deviceId: String) {
        deviceDao.deleteDevice(deviceId)
    }

    suspend fun updateLastLogin(deviceId: String) {
        deviceDao.updateLastLoginTime(deviceId, System.currentTimeMillis())
    }
}
