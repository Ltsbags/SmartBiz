package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PluginDao {

    @Query("SELECT * FROM plugins ORDER BY name ASC")
    fun getAllPlugins(): Flow<List<PluginEntity>>

    @Query("SELECT * FROM plugins WHERE status = 'ENABLED' ORDER BY name ASC")
    fun getEnabledPlugins(): Flow<List<PluginEntity>>

    @Query("SELECT * FROM plugins WHERE id = :id LIMIT 1")
    suspend fun getPluginById(id: String): PluginEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlugin(plugin: PluginEntity)

    @Update
    suspend fun updatePlugin(plugin: PluginEntity)

    @Query("UPDATE plugins SET status = :status, updatedAt = :updatedAt WHERE id = :pluginId")
    suspend fun updatePluginStatus(pluginId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM plugins WHERE id = :pluginId")
    suspend fun deletePlugin(pluginId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistryEntry(entry: PluginRegistryEntity)

    @Query("DELETE FROM plugin_registry WHERE pluginId = :pluginId")
    suspend fun deleteRegistryEntry(pluginId: String)

    @Query("SELECT * FROM plugin_permissions WHERE pluginId = :pluginId AND permissionName = :permissionName LIMIT 1")
    suspend fun getPluginPermission(pluginId: String, permissionName: String): PluginPermissionEntity?

    @Query("SELECT * FROM plugin_permissions WHERE pluginId = :pluginId AND isGranted = 1")
    suspend fun getGrantedPermissionsForPlugin(pluginId: String): List<PluginPermissionEntity>

    @Query("SELECT * FROM plugin_permissions WHERE pluginId = :pluginId")
    fun getPermissionsForPlugin(pluginId: String): Flow<List<PluginPermissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePermission(permission: PluginPermissionEntity)

    @Query("DELETE FROM plugin_permissions WHERE pluginId = :pluginId")
    suspend fun deletePermissionsForPlugin(pluginId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSetting(setting: PluginSettingsEntity)

    @Query("SELECT * FROM plugin_settings WHERE pluginId = :pluginId")
    fun getSettingsForPlugin(pluginId: String): Flow<List<PluginSettingsEntity>>

    @Query("DELETE FROM plugin_settings WHERE pluginId = :pluginId")
    suspend fun deleteSettingsForPlugin(pluginId: String)
}
