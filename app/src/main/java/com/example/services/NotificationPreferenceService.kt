package com.example.services

import com.example.core.database.entity.NotificationPreferenceEntity
import com.example.repositories.NotificationPreferenceRepository
import kotlinx.coroutines.flow.Flow

class NotificationPreferenceService(
    private val preferenceRepository: NotificationPreferenceRepository
) {

    val allPreferencesFlow: Flow<List<NotificationPreferenceEntity>> = preferenceRepository.allPreferencesFlow

    suspend fun initializeDefaultPreferencesIfEmpty() {
        val defaults = listOf(
            NotificationPreferenceEntity("low_stock_alerts", "Low Stock Alerts", "Notify when inventory items drop below reorder level", "INVENTORY", true),
            NotificationPreferenceEntity("payment_alerts", "Payment & Due Alerts", "Notify when customer payments or supplier invoices are overdue", "FINANCE", true),
            NotificationPreferenceEntity("business_summary", "Daily Business Summary", "Receive automated daily performance summaries", "REPORTS", true),
            NotificationPreferenceEntity("security_alerts", "Security & Access Alerts", "Notify on critical security events or app lock incidents", "SECURITY", true),
            NotificationPreferenceEntity("system_alerts", "System & Backup Alerts", "Notify when database optimization or backup is recommended", "SYSTEM", true),
            NotificationPreferenceEntity("reminder_sounds", "Notification Sounds", "Play sound alert on high priority notifications", "SYSTEM", true),
            NotificationPreferenceEntity("vibration", "Vibration Alerts", "Vibrate device on critical alerts", "SYSTEM", true)
        )

        for (def in defaults) {
            val existing = preferenceRepository.getPreferenceByKey(def.key)
            if (existing == null) {
                preferenceRepository.savePreference(def)
            }
        }
    }

    suspend fun isCategoryEnabled(key: String): Boolean {
        val pref = preferenceRepository.getPreferenceByKey(key)
        return pref?.isEnabled ?: true
    }

    suspend fun togglePreference(key: String, isEnabled: Boolean) {
        preferenceRepository.updatePreferenceStatus(key, isEnabled)
    }
}
