package com.example.repositories

import com.example.core.database.dao.PermissionDao
import com.example.core.database.dao.RoleDao
import com.example.core.database.dao.RolePermissionDao
import com.example.core.database.dao.UserRoleDao
import com.example.core.database.entity.PermissionEntity
import com.example.core.database.entity.RoleEntity
import com.example.core.database.entity.RolePermissionCrossRef
import com.example.core.database.entity.UserRoleCrossRef
import com.example.core.database.model.RoleWithPermissions
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RbacRepository(
    private val roleDao: RoleDao,
    private val permissionDao: PermissionDao,
    private val rolePermissionDao: RolePermissionDao,
    private val userRoleDao: UserRoleDao
) {

    fun getAllRoles(): Flow<List<RoleEntity>> = roleDao.getAllRoles()

    fun getAllRolesWithPermissions(): Flow<List<RoleWithPermissions>> = roleDao.getAllRolesWithPermissions()

    suspend fun getRoleById(roleId: String): RoleEntity? = roleDao.getRoleById(roleId)

    suspend fun getRoleWithPermissions(roleId: String): RoleWithPermissions? = roleDao.getRoleWithPermissions(roleId)

    suspend fun createCustomRole(
        roleName: String,
        roleCode: String,
        description: String,
        permissionIds: List<String>
    ): String {
        val roleId = "ROLE_CUSTOM_${UUID.randomUUID().toString().take(8).uppercase()}"
        val role = RoleEntity(
            roleId = roleId,
            roleName = roleName,
            roleCode = roleCode.uppercase().replace(" ", "_"),
            description = description,
            isSystemRole = false,
            isCustomRole = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        roleDao.insertRole(role)

        val crossRefs = permissionIds.map { permId ->
            RolePermissionCrossRef(roleId = roleId, permissionId = permId)
        }
        rolePermissionDao.insertRolePermissionCrossRefs(crossRefs)

        return roleId
    }

    suspend fun updateRolePermissions(roleId: String, permissionIds: List<String>) {
        rolePermissionDao.deletePermissionsForRole(roleId)
        val crossRefs = permissionIds.map { permId ->
            RolePermissionCrossRef(roleId = roleId, permissionId = permId)
        }
        rolePermissionDao.insertRolePermissionCrossRefs(crossRefs)
    }

    suspend fun deleteCustomRole(roleId: String) {
        val role = roleDao.getRoleById(roleId)
        if (role != null && !role.isSystemRole) {
            rolePermissionDao.deletePermissionsForRole(roleId)
            roleDao.deleteCustomRole(roleId)
        }
    }

    fun getAllPermissions(): Flow<List<PermissionEntity>> = permissionDao.getAllPermissions()

    fun getPermissionsByCategory(category: String): Flow<List<PermissionEntity>> = permissionDao.getPermissionsByCategory(category)

    fun getRolesForUser(userId: String): Flow<List<RoleEntity>> = userRoleDao.getRolesForUser(userId)

    suspend fun getPermissionsForUser(userId: String): List<PermissionEntity> = userRoleDao.getPermissionsForUser(userId)

    suspend fun assignRoleToUser(userId: String, roleId: String, assignedBy: String = "SYSTEM") {
        userRoleDao.insertUserRoleCrossRef(
            UserRoleCrossRef(
                userId = userId,
                roleId = roleId,
                assignedAt = System.currentTimeMillis(),
                assignedBy = assignedBy
            )
        )
    }

    suspend fun revokeRoleFromUser(userId: String, roleId: String) {
        userRoleDao.deleteUserRole(userId, roleId)
    }

    suspend fun setUserRoles(userId: String, roleIds: List<String>, assignedBy: String = "SYSTEM") {
        userRoleDao.deleteRolesForUser(userId)
        val crossRefs = roleIds.map { roleId ->
            UserRoleCrossRef(
                userId = userId,
                roleId = roleId,
                assignedAt = System.currentTimeMillis(),
                assignedBy = assignedBy
            )
        }
        userRoleDao.insertUserRoleCrossRefs(crossRefs)
    }
}
