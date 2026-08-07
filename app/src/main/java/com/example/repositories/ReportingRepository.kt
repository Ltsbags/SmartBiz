package com.example.repositories

import com.example.core.database.entity.BranchMetricsEntity
import com.example.core.database.entity.KpiDefinitionEntity
import com.example.core.database.entity.ReportDefinitionEntity
import com.example.core.database.entity.SavedReportSnapshotEntity
import com.example.services.bi.BIService
import com.example.services.bi.ExecutiveDashboardSummary
import com.example.services.bi.ForecastResultPoint
import kotlinx.coroutines.flow.Flow

class ReportingRepository(
    private val biService: BIService
) {
    val reportDefinitions: Flow<List<ReportDefinitionEntity>> = biService.allReportDefinitions
    val savedSnapshots: Flow<List<SavedReportSnapshotEntity>> = biService.allSavedSnapshots
    val activeKpis: Flow<List<KpiDefinitionEntity>> = biService.activeKpis
    val branchMetrics: Flow<List<BranchMetricsEntity>> = biService.branchMetrics

    suspend fun getExecutiveSummary(startDate: Long, endDate: Long): ExecutiveDashboardSummary {
        return biService.getExecutiveDashboardSummary(startDate, endDate)
    }

    suspend fun generateCustomReport(
        title: String,
        category: String,
        startDate: Long,
        endDate: Long,
        format: String = "JSON"
    ): SavedReportSnapshotEntity {
        return biService.generateCustomReportSnapshot(title, category, startDate, endDate, format)
    }

    suspend fun runRevenueAndExpenseForecast(
        historicalDays: Int = 60,
        forecastDays: Int = 30
    ): Pair<List<ForecastResultPoint>, List<ForecastResultPoint>> {
        return biService.runRevenueAndExpenseForecast(historicalDays, forecastDays)
    }

    suspend fun saveKpi(kpi: KpiDefinitionEntity): Long {
        return biService.saveKpiDefinition(kpi)
    }

    suspend fun saveReportDefinition(def: ReportDefinitionEntity): Long {
        return biService.saveReportDefinition(def)
    }
}
