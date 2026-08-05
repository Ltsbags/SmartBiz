package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.NotificationPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationPreferenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: NotificationPreferenceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: List<NotificationPreferenceEntity>)

    @Query("SELECT * FROM notification_preferences")
    fun getAllPreferencesFlow(): Flow<List<NotificationPreferenceEntity>>

    @Query("SELECT * FROM notification_preferences WHERE `key` = :key")
    suspend fun getPreferenceByKey(key: String): NotificationPreferenceEntity?

    @Query("UPDATE notification_preferences SET isEnabled = :isEnabled, updatedDate = :updatedAt WHERE `key` = :key")
    suspend fun updatePreferenceStatus(key: String, isEnabled: Boolean, updatedAt: Long = System.currentTimeMillis())
}
