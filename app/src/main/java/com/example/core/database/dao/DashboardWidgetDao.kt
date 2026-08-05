package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.DashboardWidgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardWidgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidget(widget: DashboardWidgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidgets(widgets: List<DashboardWidgetEntity>)

    @Update
    suspend fun updateWidget(widget: DashboardWidgetEntity)

    @Query("SELECT * FROM dashboard_widgets ORDER BY isPinned DESC, sortOrder ASC")
    fun getAllWidgetsFlow(): Flow<List<DashboardWidgetEntity>>

    @Query("SELECT * FROM dashboard_widgets WHERE isEnabled = 1 ORDER BY isPinned DESC, sortOrder ASC")
    fun getEnabledWidgetsFlow(): Flow<List<DashboardWidgetEntity>>

    @Query("SELECT * FROM dashboard_widgets WHERE widgetKey = :key LIMIT 1")
    suspend fun getWidgetByKey(key: String): DashboardWidgetEntity?

    @Query("UPDATE dashboard_widgets SET isEnabled = :isEnabled WHERE widgetKey = :key")
    suspend fun toggleWidgetEnabled(key: String, isEnabled: Boolean)

    @Query("UPDATE dashboard_widgets SET isPinned = :isPinned WHERE widgetKey = :key")
    suspend fun toggleWidgetPinned(key: String, isPinned: Boolean)

    @Query("UPDATE dashboard_widgets SET sortOrder = :order WHERE widgetKey = :key")
    suspend fun updateWidgetOrder(key: String, order: Int)

    @Query("DELETE FROM dashboard_widgets WHERE id = :id")
    suspend fun deleteWidget(id: String)
}
