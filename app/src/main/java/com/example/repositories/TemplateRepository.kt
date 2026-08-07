package com.example.repositories

import com.example.core.database.dao.CommunicationDao
import com.example.core.database.entity.CommunicationTemplateEntity
import kotlinx.coroutines.flow.Flow

class TemplateRepository(
    private val communicationDao: CommunicationDao
) {
    val activeTemplates: Flow<List<CommunicationTemplateEntity>> = communicationDao.getAllActiveTemplates()

    suspend fun getTemplateById(templateId: String): CommunicationTemplateEntity? {
        return communicationDao.getTemplateByTemplateId(templateId)
    }

    suspend fun saveTemplate(template: CommunicationTemplateEntity): Long {
        return communicationDao.insertTemplate(template)
    }

    suspend fun deleteTemplate(id: Long) {
        communicationDao.deleteTemplate(id)
    }
}
