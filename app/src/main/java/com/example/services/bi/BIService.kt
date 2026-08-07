package com.example.services.bi

import com.example.core.database.dao.AggregatedMetricsDao
import com.example.core.database.dao.BranchMetricsDao
import com.example.core.database.dao.ForecastingSnapshotDao
import com.example.core.database.dao.KpiDefinitionDao
import com.example.core.database.dao.ReportDao
import com.example.core.database.dao.ReportDefinitionDao
import com.example.core.database.dao.SavedReportSnapshotDao
import com.example.core.database.entity.AggregatedDailyMetricsEntity
import com.example.core.database.entity.BranchMetricsEntity
import com.example.core.database.entity.KpiDefinitionEntity
import com.example.core.database.entity.ReportDefinitionEntity
import com.example.core.database.entity.SavedReportSnapshotEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ExecutiveDashboardSummary(
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val netProfit: Double = 0.0,
    val netProfitMarginPct: Double = 0.0,
    val outstandingReceivables: Double = 0.0,
    val outstandingPayables: Double = 0.0,
    val inventoryValuation: Double = 0.0,
    val salesCount: Int = 0,
    val gstNetCollected: Double = 0.0,
    val kpiEvaluations: List<KpiEvaluation> = emptyList(),
    val topSellingCategories: List<Pair<String, Double>> = emptyList(),
    val branchConsolidation: List<BranchMetricsEntity> = emptyList()
)

class BIService(
    private val reportDao: ReportDao,
    private val aggregatedMetricsDao: AggregatedMetricsDao,
    private val branchMetricsDao: BranchMetricsDao,
    private val kpiDefinitionDao: KpiDefinitionDao,
    private val reportDefinitionDao: ReportDefinitionDao,
    private val savedReportSnapshotDao: SavedReportSnapshotDao,
    private val forecastingSnapshotDao: ForecastingSnapshotDao,
    private val aggregationService: AggregationService,
    private val kpiEngineService: KPIEngineService,
    private val forecastingService: ForecastingService
) {

    val allReportDefinitions: Flow<List<ReportDefinitionEntity>> =
        reportDefinitionDao.getAllReportDefinitions()

    val allSavedSnapshots: Flow<List<SavedReportSnapshotEntity>> =
        savedReportSnapshotDao.getAllSavedSnapshots()

    val activeKpis: Flow<List<KpiDefinitionEntity>> =
        kpiDefinitionDao.getActiveKpis()

    val branchMetrics: Flow<List<BranchMetricsEntity>> =
        branchMetricsDao.getAllBranchMetrics()

    val aggregatedDailyMetrics: Flow<List<AggregatedDailyMetricsEntity>> =
        aggregatedMetricsDao.getAllAggregatedDailyMetrics()

    /**
     * Compute full Executive Dashboard Summary. Uses aggregated cache tables first for optimal offline speed.
     */
    suspend fun getExecutiveDashboardSummary(
        startDate: Long,
        endDate: Long
    ): ExecutiveDashboardSummary {
        // Ensure default KPIs are present
        kpiEngineService.seedDefaultKpis()

        // Sync daily metrics for requested range
        aggregationService.recomputeDailyMetricsForRange(startDate, endDate)

        // Seed branch consolidation
        val branches = listOf(
            "MAIN" to "Headquarters - Central",
            "NORTH" to "North Regional Branch",
            "SOUTH" to "South Plaza Outlet"
        )
        aggregationService.refreshBranchConsolidation(branches)

        // Query aggregated metrics table (never heavy raw ops directly)
        val dailyMetrics = aggregatedMetricsDao.getMetricsForRange(startDate, endDate)

        val totalRev = dailyMetrics.sumOf { it.totalSales }
        val totalExp = dailyMetrics.sumOf { it.totalExpenses }
        val totalPurch = dailyMetrics.sumOf { it.totalPurchases }
        val netProf = (totalRev + dailyMetrics.sumOf { it.totalIncome }) - (totalExp + totalPurch)
        val margin = if (totalRev > 0) (netProf / totalRev) * 100.0 else 0.0
        val gstNet = dailyMetrics.sumOf { it.totalGstCollected - it.totalGstPaid }
        val salesCnt = dailyMetrics.sumOf { it.newInvoicesCount }

        val receivables = reportDao.getTotalOutstandingReceivables()
        val payables = reportDao.getTotalOutstandingPayables()
        val inventoryVal = reportDao.getTotalInventoryValuation()

        // Evaluate KPIs
        val kpis = kpiDefinitionDao.getAllKpisList()
        val prevStartDate = startDate - (endDate - startDate)
        val prevEndDate = startDate
        val prevMetrics = aggregatedMetricsDao.getMetricsForRange(prevStartDate, prevEndDate)
        val prevRev = prevMetrics.sumOf { it.totalSales }

        val kpiEvals = kpis.map { kpi ->
            val currVal = when (kpi.code) {
                "KPI_REVENUE_MONTHLY" -> totalRev
                "KPI_NET_PROFIT_MARGIN" -> margin
                "KPI_MONTHLY_SALES_VOLUME" -> salesCnt.toDouble()
                "KPI_EXPENSE_LIMIT" -> totalExp
                else -> totalRev
            }
            val prevVal = when (kpi.code) {
                "KPI_REVENUE_MONTHLY" -> prevRev
                else -> 0.0
            }
            kpiEngineService.evaluateKpi(kpi, currVal, prevVal)
        }

        // Category breakdown
        val topCatAgg = reportDao.getExpenseCategoryBreakdown(startDate, endDate)
        val topCats = topCatAgg.map { Pair(it.categoryName, it.totalExpense) }

        // Multi-branch
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val branchList = branchMetricsDao.getBranchMetricsForDate(dateStr)

        return ExecutiveDashboardSummary(
            totalRevenue = totalRev,
            totalExpenses = totalExp,
            totalPurchases = totalPurch,
            netProfit = netProf,
            netProfitMarginPct = margin,
            outstandingReceivables = receivables,
            outstandingPayables = payables,
            inventoryValuation = inventoryVal,
            salesCount = salesCnt,
            gstNetCollected = gstNet,
            kpiEvaluations = kpiEvals,
            topSellingCategories = topCats,
            branchConsolidation = branchList
        )
    }

    /**
     * Build & Export Custom Report Snapshot (CSV/JSON text).
     */
    suspend fun generateCustomReportSnapshot(
        title: String,
        category: String,
        startDate: Long,
        endDate: Long,
        format: String = "JSON"
    ): SavedReportSnapshotEntity {
        val summary = getExecutiveDashboardSummary(startDate, endDate)

        val dataContent = if (format == "CSV") {
            """
                Metric,Value
                Total Revenue,${summary.totalRevenue}
                Total Expenses,${summary.totalExpenses}
                Total Purchases,${summary.totalPurchases}
                Net Profit,${summary.netProfit}
                Net Profit Margin %,${String.format("%.2f", summary.netProfitMarginPct)}
                Outstanding Receivables,${summary.outstandingReceivables}
                Outstanding Payables,${summary.outstandingPayables}
                Inventory Valuation,${summary.inventoryValuation}
                Sales Invoices Count,${summary.salesCount}
            """.trimIndent()
        } else {
            """
                {
                  "reportTitle": "$title",
                  "category": "$category",
                  "startDate": $startDate,
                  "endDate": $endDate,
                  "totalRevenue": ${summary.totalRevenue},
                  "totalExpenses": ${summary.totalExpenses},
                  "netProfit": ${summary.netProfit},
                  "marginPct": ${summary.netProfitMarginPct},
                  "receivables": ${summary.outstandingReceivables},
                  "payables": ${summary.outstandingPayables}
                }
            """.trimIndent()
        }

        val snapshot = SavedReportSnapshotEntity(
            reportTitle = title,
            parametersJson = "{\"startDate\":$startDate,\"endDate\":$endDate}",
            format = format,
            snapshotDataJson = dataContent,
            generatedAt = System.currentTimeMillis()
        )

        val id = savedReportSnapshotDao.insertSnapshot(snapshot)
        return snapshot.copy(id = id)
    }

    /**
     * Forecast Revenue / Expenses for next 30 days.
     */
    suspend fun runRevenueAndExpenseForecast(historicalDays: Int = 60, forecastDays: Int = 30): Pair<List<ForecastResultPoint>, List<ForecastResultPoint>> {
        val endDate = System.currentTimeMillis()
        val startDate = endDate - (historicalDays * 86400000L)

        aggregationService.recomputeDailyMetricsForRange(startDate, endDate)
        val metrics = aggregatedMetricsDao.getMetricsForRange(startDate, endDate)

        val salesSeries = metrics.map { Pair(it.timestamp, it.totalSales) }
        val expenseSeries = metrics.map { Pair(it.timestamp, it.totalExpenses) }

        val revenueForecast = forecastingService.generateForecast(
            type = "REVENUE",
            historicalValues = if (salesSeries.isNotEmpty()) salesSeries else listOf(Pair(startDate, 1000.0), Pair(endDate, 1500.0)),
            daysToForecast = forecastDays
        )

        val expenseForecast = forecastingService.generateForecast(
            type = "EXPENSE",
            historicalValues = if (expenseSeries.isNotEmpty()) expenseSeries else listOf(Pair(startDate, 500.0), Pair(endDate, 700.0)),
            daysToForecast = forecastDays
        )

        return Pair(revenueForecast, expenseForecast)
    }

    /**
     * Save or update a custom KPI definition.
     */
    suspend fun saveKpiDefinition(kpi: KpiDefinitionEntity): Long {
        return if (kpi.id == 0L) {
            kpiDefinitionDao.insertKpi(kpi)
        } else {
            kpiDefinitionDao.updateKpi(kpi)
            kpi.id
        }
    }

    /**
     * Save custom report definition template.
     */
    suspend fun saveReportDefinition(def: ReportDefinitionEntity): Long {
        return reportDefinitionDao.insertReportDefinition(def)
    }
}
