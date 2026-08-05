package com.example.repositories

import com.example.core.database.dao.DashboardLayoutDao
import com.example.core.database.dao.DashboardWidgetDao
import com.example.core.database.entity.DashboardLayoutEntity
import com.example.core.database.entity.DashboardWidgetEntity
import kotlinx.coroutines.flow.Flow

class DashboardWidgetRepository(
    private val widgetDao: DashboardWidgetDao,
    private val layoutDao: DashboardLayoutDao
) {

    val allWidgetsFlow: Flow<List<DashboardWidgetEntity>> = widgetDao.getAllWidgetsFlow()
    val enabledWidgetsFlow: Flow<List<DashboardWidgetEntity>> = widgetDao.getEnabledWidgetsFlow()

    suspend fun addWidget(widget: DashboardWidgetEntity): Long = widgetDao.insertWidget(widget)

    suspend fun addWidgets(widgets: List<DashboardWidgetEntity>) = widgetDao.insertWidgets(widgets)

    suspend fun updateWidget(widget: DashboardWidgetEntity) = widgetDao.updateWidget(widget)

    suspend fun getWidgetByKey(key: String): DashboardWidgetEntity? = widgetDao.getWidgetByKey(key)

    suspend fun toggleWidgetEnabled(key: String, isEnabled: Boolean) = widgetDao.toggleWidgetEnabled(key, isEnabled)

    suspend fun toggleWidgetPinned(key: String, isPinned: Boolean) = widgetDao.toggleWidgetPinned(key, isPinned)

    suspend fun updateWidgetOrder(key: String, order: Int) = widgetDao.updateWidgetOrder(key, order)

    fun getLayoutFlow(userId: String = "default_user", businessId: String = "default_biz"): Flow<DashboardLayoutEntity?> =
        layoutDao.getLayoutFlow(userId, businessId)

    suspend fun saveLayout(layout: DashboardLayoutEntity): Long = layoutDao.insertLayout(layout)
}
