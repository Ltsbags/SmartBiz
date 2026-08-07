package com.example.services.bi

import com.example.core.database.dao.KpiDefinitionDao
import com.example.core.database.entity.KpiDefinitionEntity

enum class KpiStatus {
    NORMAL,
    WARNING,
    CRITICAL,
    TARGET_MET
}

data class KpiEvaluation(
    val kpi: KpiDefinitionEntity,
    val currentValue: Double,
    val previousValue: Double = 0.0,
    val progressPercentage: Double,
    val status: KpiStatus,
    val trendPercentage: Double,
    val statusMessage: String
)

class KPIEngineService(
    private val kpiDefinitionDao: KpiDefinitionDao
) {
    /**
     * Evaluates a single KPI given current metric value and previous period value.
     */
    fun evaluateKpi(
        kpi: KpiDefinitionEntity,
        currentValue: Double,
        previousValue: Double = 0.0
    ): KpiEvaluation {
        val target = if (kpi.targetValue > 0) kpi.targetValue else 1.0
        val progress = (currentValue / target) * 100.0

        val trend = if (previousValue > 0) {
            ((currentValue - previousValue) / previousValue) * 100.0
        } else {
            0.0
        }

        val status = when {
            kpi.criticalThreshold > 0 && currentValue <= kpi.criticalThreshold -> KpiStatus.CRITICAL
            kpi.warningThreshold > 0 && currentValue <= kpi.warningThreshold -> KpiStatus.WARNING
            currentValue >= kpi.targetValue && kpi.targetValue > 0 -> KpiStatus.TARGET_MET
            else -> KpiStatus.NORMAL
        }

        val msg = when (status) {
            KpiStatus.TARGET_MET -> "Target exceeded by ${String.format("%.1f", progress - 100)}%"
            KpiStatus.WARNING -> "Attention: Below warning threshold (${kpi.warningThreshold})"
            KpiStatus.CRITICAL -> "Critical: Below minimum operational threshold (${kpi.criticalThreshold})"
            KpiStatus.NORMAL -> "Healthy progress (${String.format("%.1f", progress)}% of target)"
        }

        return KpiEvaluation(
            kpi = kpi,
            currentValue = currentValue,
            previousValue = previousValue,
            progressPercentage = progress.coerceIn(0.0, 500.0),
            status = status,
            trendPercentage = trend,
            statusMessage = msg
        )
    }

    /**
     * Initializes default enterprise KPIs if none exist.
     */
    suspend fun seedDefaultKpis() {
        val existing = kpiDefinitionDao.getAllKpisList()
        if (existing.isEmpty()) {
            val defaults = listOf(
                KpiDefinitionEntity(
                    code = "KPI_REVENUE_MONTHLY",
                    name = "Monthly Revenue Target",
                    category = "FINANCIAL",
                    targetValue = 50000.0,
                    warningThreshold = 25000.0,
                    criticalThreshold = 10000.0,
                    calculationType = "SUM",
                    timeWindow = "MONTHLY",
                    formatType = "CURRENCY",
                    iconName = "AttachMoney"
                ),
                KpiDefinitionEntity(
                    code = "KPI_NET_PROFIT_MARGIN",
                    name = "Net Profit Margin %",
                    category = "FINANCIAL",
                    targetValue = 25.0,
                    warningThreshold = 15.0,
                    criticalThreshold = 8.0,
                    calculationType = "MARGIN_PCT",
                    timeWindow = "MONTHLY",
                    formatType = "PERCENTAGE",
                    iconName = "TrendingUp"
                ),
                KpiDefinitionEntity(
                    code = "KPI_MONTHLY_SALES_VOLUME",
                    name = "Invoice Count Target",
                    category = "SALES",
                    targetValue = 100.0,
                    warningThreshold = 50.0,
                    criticalThreshold = 20.0,
                    calculationType = "COUNT",
                    timeWindow = "MONTHLY",
                    formatType = "COUNT",
                    iconName = "Receipt"
                ),
                KpiDefinitionEntity(
                    code = "KPI_EXPENSE_LIMIT",
                    name = "Monthly Expense Ceiling",
                    category = "FINANCIAL",
                    targetValue = 20000.0,
                    warningThreshold = 18000.0,
                    criticalThreshold = 22000.0,
                    calculationType = "SUM",
                    timeWindow = "MONTHLY",
                    formatType = "CURRENCY",
                    iconName = "MoneyOff"
                )
            )
            defaults.forEach { kpiDefinitionDao.insertKpi(it) }
        }
    }
}
