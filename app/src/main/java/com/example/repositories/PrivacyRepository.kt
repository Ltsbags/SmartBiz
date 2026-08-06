package com.example.repositories

import com.example.core.database.dao.PrivacySettingsDao
import com.example.core.database.entity.PrivacySettingsEntity
import kotlinx.coroutines.flow.Flow

class PrivacyRepository(
    private val privacySettingsDao: PrivacySettingsDao
) {
    fun getPrivacySettingsFlow(userId: String = "DEFAULT_USER"): Flow<PrivacySettingsEntity?> {
        return privacySettingsDao.getPrivacySettingsFlowForUser(userId)
    }

    suspend fun getPrivacySettings(userId: String = "DEFAULT_USER"): PrivacySettingsEntity {
        return privacySettingsDao.getPrivacySettingsForUser(userId) ?: PrivacySettingsEntity(userId = userId)
    }

    suspend fun savePrivacySettings(settings: PrivacySettingsEntity): Long {
        return privacySettingsDao.insertOrUpdatePrivacySettings(settings)
    }
}
