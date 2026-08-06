package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.PrivacySettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrivacySettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePrivacySettings(settings: PrivacySettingsEntity): Long

    @Query("SELECT * FROM privacy_settings WHERE userId = :userId LIMIT 1")
    suspend fun getPrivacySettingsForUser(userId: String): PrivacySettingsEntity?

    @Query("SELECT * FROM privacy_settings WHERE userId = :userId LIMIT 1")
    fun getPrivacySettingsFlowForUser(userId: String): Flow<PrivacySettingsEntity?>
}
