package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders ORDER BY nextTrigger ASC")
    fun getAllRemindersFlow(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY nextTrigger ASC")
    fun getActiveRemindersFlow(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND nextTrigger <= :currentTime")
    suspend fun getDueReminders(currentTime: Long = System.currentTimeMillis()): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: String): ReminderEntity?

    @Query("UPDATE reminders SET isEnabled = :isEnabled, updatedDate = :updatedAt WHERE id = :id")
    suspend fun toggleReminderStatus(id: String, isEnabled: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: String)
}
