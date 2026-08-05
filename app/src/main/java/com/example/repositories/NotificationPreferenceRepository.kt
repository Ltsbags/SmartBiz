package com.example.repositories

import com.example.core.database.dao.NotificationPreferenceDao
import com.example.core.database.entity.NotificationPreferenceEntity
import kotlinx.coroutines.flow.Flow

class NotificationPreferenceRepository(
    private val preferenceDao: NotificationPreferenceDao
) {

    val allPreferencesFlow: Flow<List<NotificationPreferenceEntity>> = preferenceDao.getAllPreferencesFlow()

    suspend fun savePreference(preference: NotificationPreferenceEntity): Long {
        return preferenceDao.insertPreference(preference)
    }

    suspend fun savePreferences(preferences: List<NotificationPreferenceEntity>) {
        preferenceDao.insertPreferences(preferences)
    }

    suspend fun getPreferenceByKey(key: String): NotificationPreferenceEntity? {
        return preferenceDao.getPreferenceByKey(key)
    }

    suspend fun updatePreferenceStatus(key: String, isEnabled: Boolean) {
        preferenceDao.updatePreferenceStatus(key, isEnabled)
    }
}
