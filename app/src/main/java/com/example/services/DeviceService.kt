package com.example.services

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.example.core.database.entity.DeviceEntity

class DeviceService(private val context: Context) {

    fun getCurrentDevice(userId: String): DeviceEntity {
        val deviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        val androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        val appVersion = "1.0.0"

        val deviceIdentifier = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "LOCAL_DEVICE_ID"
        } catch (e: Exception) {
            "LOCAL_DEVICE_ID"
        }

        val deviceId = "DEV_${deviceIdentifier.take(8).uppercase()}"

        return DeviceEntity(
            deviceId = deviceId,
            userId = userId,
            deviceName = deviceName,
            androidVersion = androidVersion,
            appVersion = appVersion,
            deviceIdentifier = deviceIdentifier,
            lastLoginTime = System.currentTimeMillis(),
            isTrusted = true,
            registeredDate = System.currentTimeMillis()
        )
    }
}
