package com.example.services

import com.example.core.database.entity.DeviceEntity
import com.example.repositories.TrustedDeviceRepository

class TrustedDeviceService(
    private val trustedDeviceRepository: TrustedDeviceRepository,
    private val auditService: AuditService
) {

    suspend fun trustDevice(deviceId: String, actorUserId: String = "ADMIN") {
        trustedDeviceRepository.setTrustStatus(deviceId, true)
        auditService.logAuditEvent(
            userName = actorUserId,
            module = "TRUSTED_DEVICES",
            action = "TRUST_DEVICE",
            description = "Device ID '$deviceId' was granted TRUSTED status.",
            severity = "INFO"
        )
    }

    suspend fun untrustDevice(deviceId: String, actorUserId: String = "ADMIN") {
        trustedDeviceRepository.setTrustStatus(deviceId, false)
        auditService.logAuditEvent(
            userName = actorUserId,
            module = "TRUSTED_DEVICES",
            action = "UNTRUST_DEVICE",
            description = "Device ID '$deviceId' was marked UNTRUSTED.",
            severity = "WARNING"
        )
    }

    suspend fun renameDevice(deviceId: String, newName: String, actorUserId: String = "ADMIN") {
        trustedDeviceRepository.renameDevice(deviceId, newName)
        auditService.logAuditEvent(
            userName = actorUserId,
            module = "TRUSTED_DEVICES",
            action = "RENAME_DEVICE",
            description = "Renamed device ID '$deviceId' to '$newName'",
            severity = "INFO"
        )
    }

    suspend fun removeDevice(deviceId: String, actorUserId: String = "ADMIN") {
        trustedDeviceRepository.removeDevice(deviceId)
        auditService.logAuditEvent(
            userName = actorUserId,
            module = "TRUSTED_DEVICES",
            action = "REMOVE_DEVICE",
            description = "Removed device registration for device ID '$deviceId'",
            severity = "WARNING"
        )
    }

    /**
     * Architecture hook for remote device trust approval by security admins.
     */
    suspend fun requestRemoteApproval(deviceId: String, reason: String): Boolean {
        auditService.logAuditEvent(
            userName = "USER",
            module = "TRUSTED_DEVICES",
            action = "REQUEST_REMOTE_APPROVAL",
            description = "Remote device trust approval requested for '$deviceId'. Reason: $reason",
            severity = "INFO"
        )
        return true
    }
}
