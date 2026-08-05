package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.PermissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PermissionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermission(permission: PermissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermissions(permissions: List<PermissionEntity>)

    @Query("SELECT * FROM permissions ORDER BY category ASC, permissionName ASC")
    fun getAllPermissions(): Flow<List<PermissionEntity>>

    @Query("SELECT * FROM permissions ORDER BY category ASC, permissionName ASC")
    suspend fun getAllPermissionsList(): List<PermissionEntity>

    @Query("SELECT * FROM permissions WHERE category = :category ORDER BY permissionName ASC")
    fun getPermissionsByCategory(category: String): Flow<List<PermissionEntity>>

    @Query("SELECT * FROM permissions WHERE permissionCode = :code LIMIT 1")
    suspend fun getPermissionByCode(code: String): PermissionEntity?

    @Query("SELECT COUNT(*) FROM permissions")
    suspend fun getPermissionCount(): Int
}
