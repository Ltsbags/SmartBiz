package com.example.repositories

import com.example.core.database.entity.AuditLogEntity
import com.example.core.database.entity.EntityHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class UnifiedActivityItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val module: String,
    val action: String,
    val severity: String, // INFO, WARNING, CRITICAL
    val userId: String,
    val userName: String,
    val businessId: String,
    val branchId: String,
    val timestamp: Long,
    val oldValueJson: String? = null,
    val newValueJson: String? = null,
    val modifiedFieldsJson: String? = null,
    val isAudit: Boolean = true
)

class ActivityRepository(
    private val auditRepository: AuditRepository,
    private val historyRepository: HistoryRepository
) {

    fun getUnifiedActivityTimelineFlow(limit: Int = 150): Flow<List<UnifiedActivityItem>> {
        return combine(
            auditRepository.getRecentAuditsFlow(limit),
            historyRepository.getAllHistoryFlow()
        ) { audits, histories ->
            val auditItems = audits.map { audit ->
                UnifiedActivityItem(
                    id = audit.auditId,
                    title = "${audit.module}: ${audit.action.replace("_", " ")}",
                    subtitle = audit.description,
                    module = audit.module,
                    action = audit.action,
                    severity = audit.severity,
                    userId = audit.userId,
                    userName = audit.userName,
                    businessId = audit.businessId,
                    branchId = audit.branchId,
                    timestamp = audit.timestamp,
                    oldValueJson = audit.oldValueJson,
                    newValueJson = audit.newValueJson,
                    isAudit = true
                )
            }

            val historyItems = histories.map { history ->
                val severity = when (history.action) {
                    "DELETED", "CANCELLED" -> "WARNING"
                    "RESTORED" -> "INFO"
                    else -> "INFO"
                }
                UnifiedActivityItem(
                    id = history.historyId,
                    title = "${history.entityName} ${history.action.lowercase()}",
                    subtitle = "Entity ID: ${history.entityId}",
                    module = history.entityName.uppercase(),
                    action = history.action,
                    severity = severity,
                    userId = history.userId,
                    userName = history.userName,
                    businessId = history.businessId,
                    branchId = history.branchId,
                    timestamp = history.timestamp,
                    oldValueJson = history.oldValueJson,
                    newValueJson = history.newValueJson,
                    modifiedFieldsJson = history.modifiedFieldsJson,
                    isAudit = false
                )
            }

            (auditItems + historyItems).sortedByDescending { it.timestamp }.take(limit)
        }
    }
}
