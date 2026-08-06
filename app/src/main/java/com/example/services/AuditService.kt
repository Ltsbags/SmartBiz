package com.example.services

import com.example.core.database.entity.AuditLogEntity
import com.example.core.utils.EntityDiffHelper
import com.example.repositories.AuditRepository

class AuditService(
    private val auditRepository: AuditRepository,
    private val userRepository: com.example.repositories.UserRepository? = null
) {

    suspend fun logEvent(
        module: String,
        action: String,
        description: String,
        severity: String = "INFO", // INFO, WARNING, CRITICAL
        entityName: String = "",
        entityId: String = "",
        oldObj: Any? = null,
        newObj: Any? = null,
        userId: String? = null,
        userName: String? = null,
        businessId: String = "default_biz",
        branchId: String = "main_branch"
    ): Long {
        var activeUserId = userId ?: "system"
        var activeUserName = userName ?: "System User"

        if (userId == null && userRepository != null) {
            val user = userRepository.getPrimaryUser()
            if (user != null) {
                activeUserId = user.userId
                activeUserName = user.fullName.ifBlank { user.displayName.ifBlank { user.mobileNumber } }
            }
        }

        val diff = EntityDiffHelper.compareObjects(oldObj, newObj)

        val audit = AuditLogEntity(
            businessId = businessId,
            branchId = branchId,
            userId = activeUserId,
            userName = activeUserName,
            module = module,
            entityName = entityName,
            entityId = entityId,
            action = action,
            severity = severity,
            oldValueJson = diff.oldValueJson,
            newValueJson = diff.newValueJson,
            description = description,
            ipAddress = "127.0.0.1",
            deviceName = "Android POS",
            appVersion = "1.0.0",
            timestamp = System.currentTimeMillis()
        )

        return auditRepository.recordAudit(audit)
    }

    suspend fun logAuthEvent(action: String, description: String, severity: String = "INFO", userId: String? = null) {
        logEvent(
            module = "AUTH",
            action = action,
            description = description,
            severity = severity,
            userId = userId
        )
    }

    suspend fun logAuditEvent(
        module: String,
        action: String,
        description: String,
        severity: String = "INFO",
        entityName: String = "",
        entityId: String = "",
        userId: String? = null,
        userName: String? = null
    ): Long {
        return logEvent(
            module = module,
            action = action,
            description = description,
            severity = severity,
            entityName = entityName,
            entityId = entityId,
            userId = userId,
            userName = userName
        )
    }

    suspend fun logBusinessEvent(action: String, description: String, module: String, entityName: String = "", entityId: String = "", severity: String = "INFO", oldObj: Any? = null, newObj: Any? = null) {
        logEvent(
            module = module,
            action = action,
            description = description,
            severity = severity,
            entityName = entityName,
            entityId = entityId,
            oldObj = oldObj,
            newObj = newObj
        )
    }
}
