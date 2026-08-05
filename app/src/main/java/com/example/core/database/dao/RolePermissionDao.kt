package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.PermissionEntity
import com.example.core.database.entity.RolePermissionCrossRef

@Dao
interface RolePermissionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRolePermissionCrossRef(crossRef: RolePermissionCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRolePermissionCrossRefs(crossRefs: List<RolePermissionCrossRef>)

    @Query("DELETE FROM role_permission_cross_ref WHERE roleId = :roleId")
    suspend fun deletePermissionsForRole(roleId: String)

    @Query("""
        SELECT p.* FROM permissions p
        INNER JOIN role_permission_cross_ref ref ON p.permissionId = ref.permissionId
        WHERE ref.roleId = :roleId
    """)
    suspend fun getPermissionsForRole(roleId: String): List<PermissionEntity>

    @Query("""
        SELECT DISTINCT p.* FROM permissions p
        INNER JOIN role_permission_cross_ref ref ON p.permissionId = ref.permissionId
        WHERE ref.roleId IN (:roleIds)
    """)
    suspend fun getPermissionsForRoles(roleIds: List<String>): List<PermissionEntity>
}
