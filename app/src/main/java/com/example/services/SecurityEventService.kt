package com.example.services

import com.example.repositories.AuditRepository
import kotlinx.coroutines.flow.Flow

class SecurityEventService(
    private val auditService: AuditService,
    private val auditRepository: AuditRepository
) {

    suspend fun logLoginSuccess(userId: String, userName: String) {
        auditService.logEvent(
            module = "AUTH",
            action = "LOGIN",
            description = "User $userName logged in successfully.",
            severity = "INFO",
            userId = userId,
            userName = userName
        )
    }

    suspend fun logLoginFailure(usernameAttempt: String, reason: String) {
        auditService.logEvent(
            module = "AUTH",
            action = "FAILED_LOGIN",
            description = "Failed login attempt for '$usernameAttempt': $reason",
            severity = "WARNING",
            userId = "unauthenticated",
            userName = usernameAttempt
        )
    }

    suspend fun logLogout(userId: String, userName: String) {
        auditService.logEvent(
            module = "AUTH",
            action = "LOGOUT",
            description = "User $userName logged out.",
            severity = "INFO",
            userId = userId,
            userName = userName
        )
    }

    suspend fun logPinChanged(userId: String, userName: String) {
        auditService.logEvent(
            module = "AUTH",
            action = "PIN_CHANGE",
            description = "Security PIN updated for user $userName.",
            severity = "WARNING",
            userId = userId,
            userName = userName
        )
    }

    suspend fun logBiometricToggled(userId: String, userName: String, enabled: Boolean) {
        val action = if (enabled) "BIOMETRIC_ENABLED" else "BIOMETRIC_DISABLED"
        auditService.logEvent(
            module = "AUTH",
            action = action,
            description = "Biometric authentication ${if (enabled) "enabled" else "disabled"} for user $userName.",
            severity = "INFO",
            userId = userId,
            userName = userName
        )
    }

    suspend fun logPermissionCheckFailed(userId: String, userName: String, permissionCode: String) {
        auditService.logEvent(
            module = "RBAC",
            action = "PERMISSION_DENIED",
            description = "Access denied: Missing permission code '$permissionCode'.",
            severity = "CRITICAL",
            userId = userId,
            userName = userName
        )
    }

    suspend fun logRoleAssigned(userId: String, userName: String, roleCode: String, targetUserId: String) {
        auditService.logEvent(
            module = "RBAC",
            action = "ROLE_ASSIGNED",
            description = "Assigned role '$roleCode' to target user ID '$targetUserId'.",
            severity = "WARNING",
            entityName = "UserRole",
            entityId = targetUserId,
            userId = userId,
            userName = userName
        )
    }

    suspend fun logPermissionChanged(userId: String, userName: String, roleCode: String, details: String) {
        auditService.logEvent(
            module = "RBAC",
            action = "PERMISSION_CHANGED",
            description = "Permissions updated for role '$roleCode': $details",
            severity = "CRITICAL",
            entityName = "RolePermission",
            entityId = roleCode,
            userId = userId,
            userName = userName
        )
    }

    fun getSecurityAuditsFlow(): Flow<List<com.example.core.database.entity.AuditLogEntity>> {
        return auditRepository.getAuditsByModuleFlow("AUTH")
    }

    fun getRbacAuditsFlow(): Flow<List<com.example.core.database.entity.AuditLogEntity>> {
        return auditRepository.getAuditsByModuleFlow("RBAC")
    }
}
