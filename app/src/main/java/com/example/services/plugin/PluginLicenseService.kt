package com.example.services.plugin

import com.example.core.database.dao.PluginDao

data class LicenseValidationResult(
    val isValid: Boolean,
    val licenseType: String,
    val message: String,
    val expiresAt: Long = 0L,
    val isTrialActive: Boolean = false
)

class PluginLicenseService(
    private val pluginDao: PluginDao
) {

    suspend fun validateLicense(pluginId: String, licenseKey: String, licenseType: String): LicenseValidationResult {
        val now = System.currentTimeMillis()
        val plugin = pluginDao.getPluginById(pluginId)

        return when (licenseType.uppercase()) {
            "FREE" -> {
                LicenseValidationResult(
                    isValid = true,
                    licenseType = "FREE",
                    message = "Free Community License Active"
                )
            }
            "TRIAL" -> {
                val installedTime = plugin?.installedAt ?: now
                val trialDurationMs = 14 * 86400000L // 14 Days Trial
                val expiry = installedTime + trialDurationMs
                val isTrialValid = now <= expiry
                val daysLeft = if (isTrialValid) ((expiry - now) / 86400000L).toInt() else 0

                LicenseValidationResult(
                    isValid = isTrialValid,
                    licenseType = "TRIAL",
                    message = if (isTrialValid) "Trial License Active ($daysLeft days remaining)" else "Trial License Expired. Please upgrade.",
                    expiresAt = expiry,
                    isTrialActive = isTrialValid
                )
            }
            "PAID" -> {
                val isValidKey = licenseKey.isNotBlank() && (licenseKey.startsWith("LIC-") || licenseKey.length >= 12)
                val expiry = now + (365 * 86400000L) // 1 Year License
                LicenseValidationResult(
                    isValid = isValidKey,
                    licenseType = "PAID",
                    message = if (isValidKey) "Enterprise License Verified" else "Invalid or Corrupted License Key",
                    expiresAt = if (isValidKey) expiry else 0L
                )
            }
            else -> {
                LicenseValidationResult(
                    isValid = false,
                    licenseType = "UNKNOWN",
                    message = "Unrecognized License Format"
                )
            }
        }
    }

    suspend fun activateLicense(pluginId: String, licenseKey: String, licenseType: String): Boolean {
        val plugin = pluginDao.getPluginById(pluginId) ?: return false
        val validation = validateLicense(pluginId, licenseKey, licenseType)

        if (validation.isValid) {
            val updated = plugin.copy(
                licenseType = validation.licenseType,
                licenseKey = licenseKey,
                isLicenseValid = true,
                licenseExpiryDate = validation.expiresAt,
                updatedAt = System.currentTimeMillis()
            )
            pluginDao.updatePlugin(updated)
            return true
        }
        return false
    }
}
