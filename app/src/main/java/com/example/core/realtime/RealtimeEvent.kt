package com.example.core.realtime

sealed class RealtimeEvent(
    val eventId: String,
    val eventType: String,
    val module: String,
    val timestamp: Long = System.currentTimeMillis(),
    val severity: String = "INFO"
) {
    data class InvoiceCreated(
        val id: String,
        val invoiceNumber: String,
        val customerName: String,
        val totalAmount: Double,
        val createdBy: String,
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(id, "INVOICE_CREATED", "SALES", time)

    data class InvoiceUpdated(
        val id: String,
        val invoiceNumber: String,
        val status: String,
        val amountPaid: Double,
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(id, "INVOICE_UPDATED", "SALES", time)

    data class PurchaseCreated(
        val id: String,
        val purchaseNumber: String,
        val supplierName: String,
        val totalAmount: Double,
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(id, "PURCHASE_CREATED", "PURCHASE", time)

    data class ProductUpdated(
        val productId: String,
        val productName: String,
        val currentStock: Int,
        val minStockLevel: Int,
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(productId, "PRODUCT_UPDATED", "INVENTORY", time)

    data class StockChanged(
        val productId: String,
        val productName: String,
        val previousStock: Int,
        val newStock: Int,
        val reason: String,
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(productId, "STOCK_CHANGED", "INVENTORY", time, severity = if (newStock <= 5) "WARNING" else "INFO")

    data class CustomerUpdated(
        val customerId: String,
        val customerName: String,
        val currentBalance: Double,
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(customerId, "CUSTOMER_UPDATED", "CUSTOMER", time)

    data class SupplierUpdated(
        val supplierId: String,
        val supplierName: String,
        val outstandingBalance: Double,
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(supplierId, "SUPPLIER_UPDATED", "SUPPLIER", time)

    data class BusinessUpdated(
        val businessId: String,
        val businessName: String,
        val updatedFields: List<String>,
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(businessId, "BUSINESS_UPDATED", "BUSINESS", time)

    data class BranchChanged(
        val branchId: String,
        val branchName: String,
        val activeUsersCount: Int,
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(branchId, "BRANCH_CHANGED", "BUSINESS", time)

    data class NotificationCreated(
        val id: String,
        val title: String,
        val message: String,
        val category: String,
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(id, "NOTIFICATION_CREATED", "NOTIFICATION", time)

    data class AuditEvent(
        val id: String,
        val action: String,
        val performer: String,
        val details: String,
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(id, "AUDIT_EVENT", "AUDIT", time)

    data class SecurityAlert(
        val alertId: String,
        val alertTitle: String,
        val message: String,
        val alertSeverity: String = "HIGH",
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(alertId, "SECURITY_ALERT", "SECURITY", time, severity = alertSeverity)

    data class SyncCompleted(
        val syncBatchId: String,
        val itemsSyncedCount: Int,
        val syncType: String,
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(syncBatchId, "SYNC_COMPLETED", "SYNC", time)

    data class PresenceChanged(
        val userId: String,
        val userName: String,
        val newStatus: String,
        val device: String,
        val time: Long = System.currentTimeMillis()
    ) : RealtimeEvent(userId, "PRESENCE_CHANGED", "PRESENCE", time)

    data class HeartbeatAck(
        val ackId: String,
        val serverTimestamp: Long = System.currentTimeMillis()
    ) : RealtimeEvent(ackId, "HEARTBEAT_ACK", "SYSTEM", serverTimestamp)
}
