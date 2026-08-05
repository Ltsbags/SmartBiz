package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.core.database.entity.PermissionEntity
import com.example.core.database.entity.RoleEntity
import com.example.core.database.entity.UserRoleCrossRef
import com.example.core.database.model.UserWithRoles
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRoleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserRoleCrossRef(crossRef: UserRoleCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserRoleCrossRefs(crossRefs: List<UserRoleCrossRef>)

    @Query("DELETE FROM user_role_cross_ref WHERE userId = :userId")
    suspend fun deleteRolesForUser(userId: String)

    @Query("DELETE FROM user_role_cross_ref WHERE userId = :userId AND roleId = :roleId")
    suspend fun deleteUserRole(userId: String, roleId: String)

    @Query("""
        SELECT r.* FROM roles r
        INNER JOIN user_role_cross_ref ref ON r.roleId = ref.roleId
        WHERE ref.userId = :userId
    """)
    fun getRolesForUser(userId: String): Flow<List<RoleEntity>>

    @Query("""
        SELECT r.* FROM roles r
        INNER JOIN user_role_cross_ref ref ON r.roleId = ref.roleId
        WHERE ref.userId = :userId
    """)
    suspend fun getRolesForUserList(userId: String): List<RoleEntity>

    @Query("""
        SELECT DISTINCT p.* FROM permissions p
        INNER JOIN role_permission_cross_ref rpref ON p.permissionId = rpref.permissionId
        INNER JOIN user_role_cross_ref uref ON rpref.roleId = uref.roleId
        WHERE uref.userId = :userId
    """)
    suspend fun getPermissionsForUser(userId: String): List<PermissionEntity>

    @Transaction
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserWithRoles(userId: String): UserWithRoles?
}
