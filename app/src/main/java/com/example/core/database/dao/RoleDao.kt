package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.core.database.entity.RoleEntity
import com.example.core.database.model.RoleWithPermissions
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: RoleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<RoleEntity>)

    @Update
    suspend fun updateRole(role: RoleEntity)

    @Query("DELETE FROM roles WHERE roleId = :roleId AND isSystemRole = 0")
    suspend fun deleteCustomRole(roleId: String)

    @Query("SELECT * FROM roles ORDER BY isSystemRole DESC, roleName ASC")
    fun getAllRoles(): Flow<List<RoleEntity>>

    @Query("SELECT * FROM roles ORDER BY isSystemRole DESC, roleName ASC")
    suspend fun getAllRolesList(): List<RoleEntity>

    @Query("SELECT * FROM roles WHERE roleId = :roleId LIMIT 1")
    suspend fun getRoleById(roleId: String): RoleEntity?

    @Query("SELECT * FROM roles WHERE roleCode = :roleCode LIMIT 1")
    suspend fun getRoleByCode(roleCode: String): RoleEntity?

    @Transaction
    @Query("SELECT * FROM roles WHERE roleId = :roleId LIMIT 1")
    suspend fun getRoleWithPermissions(roleId: String): RoleWithPermissions?

    @Transaction
    @Query("SELECT * FROM roles ORDER BY isSystemRole DESC, roleName ASC")
    fun getAllRolesWithPermissions(): Flow<List<RoleWithPermissions>>

    @Transaction
    @Query("SELECT * FROM roles ORDER BY isSystemRole DESC, roleName ASC")
    suspend fun getAllRolesWithPermissionsList(): List<RoleWithPermissions>

    @Query("SELECT COUNT(*) FROM roles")
    suspend fun getRoleCount(): Int
}
