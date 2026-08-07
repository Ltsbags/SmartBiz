package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.CommunicationAutomationRuleEntity
import com.example.core.database.entity.CommunicationLogEntity
import com.example.core.database.entity.CommunicationMessageEntity
import com.example.core.database.entity.CommunicationTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunicationDao {

    // --- Messages ---
    @Query("SELECT * FROM communication_messages ORDER BY createdDate DESC")
    fun getAllMessages(): Flow<List<CommunicationMessageEntity>>

    @Query("SELECT * FROM communication_messages WHERE status = :status ORDER BY createdDate DESC")
    fun getMessagesByStatus(status: String): Flow<List<CommunicationMessageEntity>>

    @Query("SELECT * FROM communication_messages WHERE channel = :channel ORDER BY createdDate DESC")
    fun getMessagesByChannel(channel: String): Flow<List<CommunicationMessageEntity>>

    @Query("SELECT * FROM communication_messages WHERE id = :id")
    suspend fun getMessageById(id: Long): CommunicationMessageEntity?

    @Query("SELECT * FROM communication_messages WHERE status IN ('PENDING', 'QUEUED', 'RETRY') AND scheduledTime <= :now")
    suspend fun getPendingDispatchMessages(now: Long = System.currentTimeMillis()): List<CommunicationMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CommunicationMessageEntity): Long

    @Update
    suspend fun updateMessage(message: CommunicationMessageEntity)

    @Query("UPDATE communication_messages SET status = :status, deliveryStatusDetails = :details, updatedDate = :now WHERE id = :id")
    suspend fun updateMessageStatus(id: Long, status: String, details: String, now: Long = System.currentTimeMillis())

    // --- Templates ---
    @Query("SELECT * FROM communication_templates WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveTemplates(): Flow<List<CommunicationTemplateEntity>>

    @Query("SELECT * FROM communication_templates WHERE templateId = :templateId AND isActive = 1 LIMIT 1")
    suspend fun getTemplateByTemplateId(templateId: String): CommunicationTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: CommunicationTemplateEntity): Long

    @Update
    suspend fun updateTemplate(template: CommunicationTemplateEntity)

    @Query("DELETE FROM communication_templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)

    // --- Automation Rules ---
    @Query("SELECT * FROM communication_automation_rules ORDER BY ruleName ASC")
    fun getAllAutomationRules(): Flow<List<CommunicationAutomationRuleEntity>>

    @Query("SELECT * FROM communication_automation_rules WHERE eventType = :eventType AND isEnabled = 1")
    suspend fun getActiveRulesForEvent(eventType: String): List<CommunicationAutomationRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomationRule(rule: CommunicationAutomationRuleEntity): Long

    @Update
    suspend fun updateAutomationRule(rule: CommunicationAutomationRuleEntity)

    @Query("UPDATE communication_automation_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleAutomationRule(id: Long, isEnabled: Boolean)

    // --- Communication Logs ---
    @Query("SELECT * FROM communication_logs WHERE messageId = :messageId ORDER BY timestamp DESC")
    fun getLogsForMessage(messageId: Long): Flow<List<CommunicationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CommunicationLogEntity): Long
}
