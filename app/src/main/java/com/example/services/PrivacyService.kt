package com.example.services

import com.example.core.database.entity.PrivacySettingsEntity
import com.example.repositories.AuditRepository
import com.example.repositories.PrivacyRepository

class PrivacyService(
    private val privacyRepository: PrivacyRepository,
    private val auditService: AuditService
) {

    suspend fun getPrivacySettings(userId: String = "DEFAULT_USER"): PrivacySettingsEntity {
        return privacyRepository.getPrivacySettings(userId)
    }

    suspend fun updatePrivacySettings(settings: PrivacySettingsEntity) {
        privacyRepository.savePrivacySettings(settings)
        auditService.logAuditEvent(
            userName = settings.userId,
            module = "PRIVACY_SETTINGS",
            action = "UPDATE_SETTINGS",
            description = "Updated privacy control configurations (Financial Masking: ${settings.hideFinancialValues}, GST Masking: ${settings.maskGstNumbers})",
            severity = "INFO"
        )
    }

    fun maskMobile(phone: String, enabled: Boolean): String {
        if (!enabled || phone.length < 6) return phone
        val start = phone.take(2)
        val end = phone.takeLast(2)
        val masked = "*".repeat(phone.length - 4)
        return "$start$masked$end"
    }

    fun maskGst(gst: String, enabled: Boolean): String {
        if (!enabled || gst.length < 8) return gst
        val start = gst.take(2)
        val end = gst.takeLast(3)
        val masked = "X".repeat(gst.length - 5)
        return "$start$masked$end"
    }

    fun maskEmail(email: String, enabled: Boolean): String {
        if (!enabled || !email.contains("@")) return email
        val parts = email.split("@")
        val name = parts[0]
        val domain = parts[1]
        val maskedName = if (name.length <= 2) name else name.take(2) + "***"
        return "$maskedName@$domain"
    }

    fun formatFinancialValue(value: Double, currencySymbol: String = "$", hideValues: Boolean): String {
        return if (hideValues) "$currencySymbol ••••••" else "$currencySymbol%.2f".format(value)
    }
}
