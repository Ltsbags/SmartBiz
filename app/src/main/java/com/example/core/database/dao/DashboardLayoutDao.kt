package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.DashboardLayoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardLayoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayout(layout: DashboardLayoutEntity): Long

    @Update
    suspend fun updateLayout(layout: DashboardLayoutEntity)

    @Query("SELECT * FROM dashboard_layouts WHERE userId = :userId AND businessId = :businessId LIMIT 1")
    fun getLayoutFlow(userId: String, businessId: String): Flow<DashboardLayoutEntity?>

    @Query("SELECT * FROM dashboard_layouts WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultLayout(): DashboardLayoutEntity?
}
