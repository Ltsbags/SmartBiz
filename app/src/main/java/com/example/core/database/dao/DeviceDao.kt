package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(device: DeviceEntity)

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE deviceId = :deviceId")
    suspend fun deleteDevice(deviceId: String)

    @Query("SELECT * FROM devices WHERE userId = :userId ORDER BY lastLoginTime DESC")
    fun getDevicesForUser(userId: String): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE userId = :userId ORDER BY lastLoginTime DESC")
    suspend fun getDevicesListForUser(userId: String): List<DeviceEntity>

    @Query("SELECT * FROM devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getDeviceById(deviceId: String): DeviceEntity?

    @Query("UPDATE devices SET isTrusted = :isTrusted WHERE deviceId = :deviceId")
    suspend fun updateTrustStatus(deviceId: String, isTrusted: Boolean)

    @Query("UPDATE devices SET lastLoginTime = :lastLogin WHERE deviceId = :deviceId")
    suspend fun updateLastLoginTime(deviceId: String, lastLogin: Long = System.currentTimeMillis())
}
