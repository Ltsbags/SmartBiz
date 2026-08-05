package com.example.services

import com.example.repositories.AuditRepository
import com.example.repositories.InvoiceRepository
import com.example.repositories.NotificationRepository
import com.example.repositories.PurchaseRepository
import com.example.repositories.SecurityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class UnifiedActivityItem(
    val id: String,
    val source: String, // AUDIT_LOG, NOTIFICATION, SECURITY_EVENT, TRANSACTION
    val title: String,
    val description: String,
    val category: String, // SALES, PURCHASES, INVENTORY, SECURITY, SYSTEM, USER
    val severity: String = "INFO", // INFO, WARNING, HIGH, CRITICAL
    val timestamp: Long,
    val actor: String = "System"
)

class ActivityAggregatorService(
    private val auditRepository: AuditRepository,
    private val notificationRepository: NotificationRepository,
    private val securityRepository: SecurityRepository,
    private val invoiceRepository: InvoiceRepository,
    private val purchaseRepository: PurchaseRepository
) {

    val unifiedActivityFeed: Flow<List<UnifiedActivityItem>> = combine(
        auditRepository.allAuditLogs,
        notificationRepository.allNotificationsFlow,
        securityRepository.allEvents,
        invoiceRepository.allInvoices
    ) { auditLogs, notifications, securityEvents, invoices ->
        val items = mutableListOf<UnifiedActivityItem>()

        // 1. Audit Logs
        auditLogs.forEach { log ->
            items.add(
                UnifiedActivityItem(
                    id = log.id,
                    source = "AUDIT_LOG",
                    title = "${log.action} on ${log.entityType}",
                    description = log.details ?: "Audit event recorded",
                    category = log.module ?: "SYSTEM",
                    severity = "INFO",
                    timestamp = log.timestamp,
                    actor = log.userName ?: "User"
                )
            )
        }

        // 2. Notifications
        notifications.forEach { notif ->
            items.add(
                UnifiedActivityItem(
                    id = notif.id,
                    source = "NOTIFICATION",
                    title = notif.title,
                    description = notif.message,
                    category = notif.type,
                    severity = notif.severity,
                    timestamp = notif.createdDate,
                    actor = "System"
                )
            )
        }

        // 3. Security Events
        securityEvents.forEach { sec ->
            items.add(
                UnifiedActivityItem(
                    id = sec.id,
                    source = "SECURITY_EVENT",
                    title = sec.eventType,
                    description = sec.description,
                    category = "SECURITY",
                    severity = sec.severity,
                    timestamp = sec.timestamp,
                    actor = sec.userId ?: "System"
                )
            )
        }

        // 4. Invoices / Transactions
        invoices.forEach { inv ->
            items.add(
                UnifiedActivityItem(
                    id = inv.id,
                    source = "TRANSACTION",
                    title = "Invoice #${inv.invoiceNumber}",
                    description = "Amount: ₹${inv.totalAmount} • Status: ${inv.status}",
                    category = "SALES",
                    severity = "INFO",
                    timestamp = inv.invoiceDate,
                    actor = inv.createdUserId ?: "Sales Agent"
                )
            )
        }

        items.sortedByDescending { it.timestamp }
    }
}
