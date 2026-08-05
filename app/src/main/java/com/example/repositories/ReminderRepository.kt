package com.example.repositories

import com.example.core.database.dao.ReminderDao
import com.example.core.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val reminderDao: ReminderDao
) {

    val allRemindersFlow: Flow<List<ReminderEntity>> = reminderDao.getAllRemindersFlow()
    val activeRemindersFlow: Flow<List<ReminderEntity>> = reminderDao.getActiveRemindersFlow()

    suspend fun addReminder(reminder: ReminderEntity): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: ReminderEntity) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun toggleReminderStatus(id: String, isEnabled: Boolean) {
        reminderDao.toggleReminderStatus(id, isEnabled)
    }

    suspend fun deleteReminder(id: String) {
        reminderDao.deleteReminder(id)
    }

    suspend fun getDueReminders(currentTime: Long = System.currentTimeMillis()): List<ReminderEntity> {
        return reminderDao.getDueReminders(currentTime)
    }

    suspend fun getReminderById(id: String): ReminderEntity? {
        return reminderDao.getReminderById(id)
    }
}
