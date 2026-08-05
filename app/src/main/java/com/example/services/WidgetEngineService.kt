package com.example.services

import com.example.core.database.entity.DashboardWidgetEntity
import com.example.repositories.DashboardWidgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WidgetEngineService(
    private val widgetRepository: DashboardWidgetRepository
) {

    val enabledWidgetsFlow: Flow<List<DashboardWidgetEntity>> = widgetRepository.enabledWidgetsFlow
    val allWidgetsFlow: Flow<List<DashboardWidgetEntity>> = widgetRepository.allWidgetsFlow

    suspend fun initializeDefaultWidgetsIfEmpty() {
        val defaultWidgets = listOf(
            DashboardWidgetEntity(widgetKey = "QUICK_ACTIONS", title = "Quick Actions", category = "SYSTEM", sortOrder = 1, isPinned = true, widthSpan = 1),
            DashboardWidgetEntity(widgetKey = "BUSINESS_HEALTH", title = "Business Health Score", category = "INSIGHTS", sortOrder = 2, isPinned = true, widthSpan = 1),
            DashboardWidgetEntity(widgetKey = "TODAYS_SALES", title = "Today's Sales", category = "FINANCE", sortOrder = 3, isPinned = false, widthSpan = 2),
            DashboardWidgetEntity(widgetKey = "TODAYS_PURCHASES", title = "Today's Purchases", category = "FINANCE", sortOrder = 4, isPinned = false, widthSpan = 2),
            DashboardWidgetEntity(widgetKey = "TODAYS_EXPENSES", title = "Today's Expenses", category = "FINANCE", sortOrder = 5, isPinned = false, widthSpan = 2),
            DashboardWidgetEntity(widgetKey = "TODAYS_INCOME", title = "Today's Income", category = "FINANCE", sortOrder = 6, isPinned = false, widthSpan = 2),
            DashboardWidgetEntity(widgetKey = "OUTSTANDING_RECEIVABLES", title = "Outstanding Receivables", category = "FINANCE", sortOrder = 7, isPinned = false, widthSpan = 2),
            DashboardWidgetEntity(widgetKey = "OUTSTANDING_PAYABLES", title = "Outstanding Payables", category = "FINANCE", sortOrder = 8, isPinned = false, widthSpan = 2),
            DashboardWidgetEntity(widgetKey = "INVENTORY_VALUE", title = "Total Inventory Value", category = "INVENTORY", sortOrder = 9, isPinned = false, widthSpan = 2),
            DashboardWidgetEntity(widgetKey = "LOW_STOCK", title = "Low Stock Alert", category = "INVENTORY", sortOrder = 10, isPinned = false, widthSpan = 2),
            DashboardWidgetEntity(widgetKey = "OUT_OF_STOCK", title = "Out of Stock Items", category = "INVENTORY", sortOrder = 11, isPinned = false, widthSpan = 2),
            DashboardWidgetEntity(widgetKey = "PENDING_TASKS", title = "Pending Tasks", category = "TASKS", sortOrder = 12, isPinned = false, widthSpan = 1),
            DashboardWidgetEntity(widgetKey = "RECENT_ACTIVITIES", title = "Unified Activity Timeline", category = "ACTIVITY", sortOrder = 13, isPinned = false, widthSpan = 1),
            DashboardWidgetEntity(widgetKey = "TOP_PRODUCTS", title = "Top Selling Products", category = "INSIGHTS", sortOrder = 14, isPinned = false, widthSpan = 1),
            DashboardWidgetEntity(widgetKey = "TOP_CUSTOMERS", title = "Top Customers", category = "INSIGHTS", sortOrder = 15, isPinned = false, widthSpan = 1),
            DashboardWidgetEntity(widgetKey = "TOP_SUPPLIERS", title = "Top Suppliers", category = "INSIGHTS", sortOrder = 16, isPinned = false, widthSpan = 1),
            DashboardWidgetEntity(widgetKey = "NOTIFICATIONS", title = "System Alerts & Notifications", category = "SYSTEM", sortOrder = 17, isPinned = false, widthSpan = 1)
        )

        val existing = widgetRepository.getWidgetByKey("QUICK_ACTIONS")
        if (existing == null) {
            widgetRepository.addWidgets(defaultWidgets)
        }
    }

    fun getFilteredWidgetsFlow(userRole: String = "ADMIN", userPermissions: List<String> = emptyList()): Flow<List<DashboardWidgetEntity>> {
        val roleHierarchy = mapOf("USER" to 1, "MANAGER" to 2, "ADMIN" to 3, "OWNER" to 4)
        val userRoleLevel = roleHierarchy[userRole.uppercase()] ?: 1

        return enabledWidgetsFlow.map { widgets ->
            widgets.filter { widget ->
                val minRoleLevel = roleHierarchy[widget.minRole.uppercase()] ?: 1
                val hasRoleAccess = userRoleLevel >= minRoleLevel
                val hasPermissionAccess = widget.requiredPermission == null || userPermissions.contains(widget.requiredPermission)
                hasRoleAccess && hasPermissionAccess
            }
        }
    }

    suspend fun toggleWidget(widgetKey: String, isEnabled: Boolean) {
        widgetRepository.toggleWidgetEnabled(widgetKey, isEnabled)
    }

    suspend fun togglePin(widgetKey: String, isPinned: Boolean) {
        widgetRepository.toggleWidgetPinned(widgetKey, isPinned)
    }

    suspend fun reorderWidgets(widgetKeysInOrder: List<String>) {
        widgetKeysInOrder.forEachIndexed { index, key ->
            widgetRepository.updateWidgetOrder(key, index + 1)
        }
    }
}
