package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.AggregatedDailyMetricsEntity
import com.example.core.database.entity.BranchMetricsEntity
import com.example.core.database.entity.ForecastingSnapshotEntity
import com.example.core.database.entity.KpiDefinitionEntity
import com.example.core.database.entity.ReportDefinitionEntity
import com.example.core.database.entity.SavedReportSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDefinitionDao {
    @Query("SELECT * FROM bi_report_definitions ORDER BY updatedAt DESC")
    fun getAllReportDefinitions(): Flow<List<ReportDefinitionEntity>>

    @Query("SELECT * FROM bi_report_definitions WHERE category = :category ORDER BY title ASC")
    fun getReportDefinitionsByCategory(category: String): Flow<List<ReportDefinitionEntity>>

    @Query("SELECT * FROM bi_report_definitions WHERE id = :id LIMIT 1")
    suspend fun getReportDefinitionById(id: Long): ReportDefinitionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportDefinition(report: ReportDefinitionEntity): Long

    @Update
    suspend fun updateReportDefinition(report: ReportDefinitionEntity)

    @Query("DELETE FROM bi_report_definitions WHERE id = :id")
    suspend fun deleteReportDefinitionById(id: Long)
}

@Dao
interface SavedReportSnapshotDao {
    @Query("SELECT * FROM bi_saved_report_snapshots ORDER BY generatedAt DESC")
    fun getAllSavedSnapshots(): Flow<List<SavedReportSnapshotEntity>>

    @Query("SELECT * FROM bi_saved_report_snapshots WHERE reportDefinitionId = :defId ORDER BY generatedAt DESC")
    fun getSnapshotsByDefinition(defId: Long): Flow<List<SavedReportSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: SavedReportSnapshotEntity): Long

    @Query("DELETE FROM bi_saved_report_snapshots WHERE id = :id")
    suspend fun deleteSnapshotById(id: Long)
}

@Dao
interface KpiDefinitionDao {
    @Query("SELECT * FROM bi_kpi_definitions WHERE isActive = 1 ORDER BY category, name ASC")
    fun getActiveKpis(): Flow<List<KpiDefinitionEntity>>

    @Query("SELECT * FROM bi_kpi_definitions ORDER BY id ASC")
    suspend fun getAllKpisList(): List<KpiDefinitionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKpi(kpi: KpiDefinitionEntity): Long

    @Update
    suspend fun updateKpi(kpi: KpiDefinitionEntity)

    @Query("DELETE FROM bi_kpi_definitions WHERE id = :id")
    suspend fun deleteKpiById(id: Long)
}

@Dao
interface AggregatedMetricsDao {
    @Query("SELECT * FROM bi_aggregated_daily_metrics ORDER BY timestamp DESC")
    fun getAllAggregatedDailyMetrics(): Flow<List<AggregatedDailyMetricsEntity>>

    @Query("""
        SELECT * FROM bi_aggregated_daily_metrics 
        WHERE timestamp >= :startDate AND timestamp <= :endDate AND branchId = :branchId 
        ORDER BY timestamp ASC
    """)
    suspend fun getMetricsForRange(startDate: Long, endDate: Long, branchId: String = "MAIN"): List<AggregatedDailyMetricsEntity>

    @Query("SELECT * FROM bi_aggregated_daily_metrics WHERE dateStr = :dateStr AND branchId = :branchId LIMIT 1")
    suspend fun getMetricsByDateAndBranch(dateStr: String, branchId: String = "MAIN"): AggregatedDailyMetricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMetric(metric: AggregatedDailyMetricsEntity): Long
}

@Dao
interface BranchMetricsDao {
    @Query("SELECT * FROM bi_branch_metrics ORDER BY branchName ASC")
    fun getAllBranchMetrics(): Flow<List<BranchMetricsEntity>>

    @Query("SELECT * FROM bi_branch_metrics WHERE dateStr = :dateStr")
    suspend fun getBranchMetricsForDate(dateStr: String): List<BranchMetricsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBranchMetric(branchMetric: BranchMetricsEntity): Long
}

@Dao
interface ForecastingSnapshotDao {
    @Query("SELECT * FROM bi_forecasting_snapshots WHERE forecastType = :type ORDER BY forecastDate ASC")
    fun getForecastsByType(type: String): Flow<List<ForecastingSnapshotEntity>>

    @Query("SELECT * FROM bi_forecasting_snapshots ORDER BY createdAt DESC")
    fun getAllForecastSnapshots(): Flow<List<ForecastingSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecastSnapshots(snapshots: List<ForecastingSnapshotEntity>)

    @Query("DELETE FROM bi_forecasting_snapshots WHERE forecastType = :type")
    suspend fun deleteForecastsByType(type: String)
}
