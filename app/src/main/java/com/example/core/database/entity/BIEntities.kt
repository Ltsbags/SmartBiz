package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Custom Report / Dashboard layout definition.
 */
@Entity(tableName = "bi_report_definitions")
data class ReportDefinitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val title: String,
    val description: String = "",
    val category: String = "EXECUTIVE", // EXECUTIVE, SALES, FINANCIAL, INVENTORY, CUSTOM
    val configJson: String = "{}",
    val isCustom: Boolean = false,
    val createdBy: String = "System",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Saved / Exported snapshot of a generated report.
 */
@Entity(tableName = "bi_saved_report_snapshots")
data class SavedReportSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportDefinitionId: Long = 0,
    val reportTitle: String,
    val parametersJson: String = "{}",
    val format: String = "JSON", // JSON, CSV, PDF
    val snapshotDataJson: String = "{}",
    val generatedAt: Long = System.currentTimeMillis(),
    val branchId: String = "MAIN"
)

/**
 * KPI Metric definition for Executive Dashboard & KPI Designer.
 */
@Entity(tableName = "bi_kpi_definitions")
data class KpiDefinitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val category: String = "FINANCIAL", // FINANCIAL, SALES, OPERATIONS, CUSTOMER
    val targetValue: Double = 0.0,
    val warningThreshold: Double = 0.0,
    val criticalThreshold: Double = 0.0,
    val calculationType: String = "SUM", // SUM, AVERAGE, COUNT, MARGIN_PCT, GROWTH_PCT
    val timeWindow: String = "MONTHLY", // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    val formatType: String = "CURRENCY", // CURRENCY, PERCENTAGE, COUNT, NUMBER
    val iconName: String = "TrendingUp",
    val branchId: String = "ALL",
    val isActive: Boolean = true
)

/**
 * Pre-aggregated daily metrics materialized cache table.
 * Ensures heavy analytical queries do NOT execute directly on raw operational tables.
 */
@Entity(tableName = "bi_aggregated_daily_metrics")
data class AggregatedDailyMetricsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String, // YYYY-MM-DD
    val timestamp: Long,
    val branchId: String = "MAIN",
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalGstCollected: Double = 0.0,
    val totalGstPaid: Double = 0.0,
    val netProfit: Double = 0.0,
    val activeCustomersCount: Int = 0,
    val newInvoicesCount: Int = 0,
    val topCategory: String = "General",
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Multi-branch consolidated metrics for multi-branch enterprise reports.
 */
@Entity(tableName = "bi_branch_metrics")
data class BranchMetricsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val branchId: String,
    val branchName: String,
    val branchCode: String,
    val dateStr: String, // YYYY-MM-DD
    val salesAmount: Double = 0.0,
    val expenseAmount: Double = 0.0,
    val netProfitAmount: Double = 0.0,
    val inventoryValuation: Double = 0.0,
    val status: String = "ACTIVE", // ACTIVE, OFFLINE_SYNCED, PENDING
    val syncTimestamp: Long = System.currentTimeMillis()
)

/**
 * Forecasting foundation snapshot table.
 * Prepares architecture for future AI forecasting seamlessly.
 */
@Entity(tableName = "bi_forecasting_snapshots")
data class ForecastingSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val forecastType: String, // REVENUE, SALES_VOLUME, EXPENSE, CASHFLOW, INVENTORY_DEMAND
    val forecastDate: Long, // Target future date timestamp
    val historicalStartDate: Long,
    val historicalEndDate: Long,
    val predictedValue: Double,
    val lowerBound: Double = 0.0,
    val upperBound: Double = 0.0,
    val confidenceInterval: Double = 0.95,
    val growthRatePercentage: Double = 0.0,
    val aiModelMetadataJson: String = "{\"algorithm\":\"HoltWintersExponentialSmoothing\",\"features\":[\"seasonality\",\"trend\"]}",
    val createdAt: Long = System.currentTimeMillis()
)
