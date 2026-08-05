package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "dashboard_widgets",
    indices = [
        Index("id"),
        Index("widgetKey"),
        Index("category"),
        Index("isEnabled"),
        Index("isPinned"),
        Index("sortOrder")
    ]
)
data class DashboardWidgetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val widgetKey: String, // e.g. SALES_TODAY, PURCHASES_TODAY, EXPENSES_TODAY, INCOME_TODAY, OUTSTANDING_RECEIVABLES, OUTSTANDING_PAYABLES, INVENTORY_VALUE, LOW_STOCK, OUT_OF_STOCK, RECENT_ACTIVITIES, NOTIFICATIONS, PENDING_TASKS, BUSINESS_HEALTH, TOP_PRODUCTS, TOP_CUSTOMERS, TOP_SUPPLIERS, QUICK_ACTIONS
    val title: String,
    val category: String = "FINANCE", // FINANCE, INVENTORY, ACTIVITY, TASKS, SYSTEM, INSIGHTS
    val isEnabled: Boolean = true,
    val isPinned: Boolean = false,
    val sortOrder: Int = 0,
    val widthSpan: Int = 1, // 1 for full width, 2 for half width / grid
    val minRole: String = "USER", // USER, MANAGER, ADMIN, OWNER
    val requiredPermission: String? = null,
    val configJson: String = "{}"
)
