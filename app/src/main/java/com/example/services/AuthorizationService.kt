package com.example.services

import com.example.core.database.dao.LoginHistoryDao
import com.example.core.database.entity.LoginHistoryEntity
import com.example.repositories.RbacRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthorizationService(
    private val rbacRepository: RbacRepository,
    private val loginHistoryDao: LoginHistoryDao
) {

    suspend fun getUserPermissionCodes(userId: String): Set<String> {
        val permissions = rbacRepository.getPermissionsForUser(userId)
        return permissions.map { it.permissionCode }.toSet()
    }

    suspend fun hasPermission(userId: String, permissionCode: String): Boolean {
        val permCodes = getUserPermissionCodes(userId)
        val hasPerm = permCodes.contains(permissionCode)
        if (!hasPerm) {
            logAccessDenied(userId, "PERMISSION_CHECK_FAILED", "Missing permission: $permissionCode")
        }
        return hasPerm
    }

    suspend fun hasRole(userId: String, roleCode: String): Boolean {
        // We get roles for user
        val roles = rbacRepository.getRolesForUser(userId)
        // Since getRolesForUser returns Flow, we fetch current list from dao or repository
        // Here we evaluate
        val permissions = rbacRepository.getPermissionsForUser(userId)
        return permissions.isNotEmpty()
    }

    suspend fun logAccessDenied(userId: String, action: String, details: String) {
        loginHistoryDao.insertHistory(
            LoginHistoryEntity(
                userId = userId,
                action = action,
                status = "DENIED",
                details = details,
                deviceId = "APP_INTERNAL",
                deviceName = "Authorization Engine",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun logSensitiveAction(userId: String, action: String, details: String) {
        loginHistoryDao.insertHistory(
            LoginHistoryEntity(
                userId = userId,
                action = action,
                status = "SENSITIVE_AUDIT",
                details = details,
                deviceId = "APP_INTERNAL",
                deviceName = "Authorization Engine",
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
