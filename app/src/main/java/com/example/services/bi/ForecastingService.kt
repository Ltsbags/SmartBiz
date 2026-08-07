package com.example.services.bi

import com.example.core.database.dao.ForecastingSnapshotDao
import com.example.core.database.entity.ForecastingSnapshotEntity
import java.util.Calendar
import kotlin.math.max
import kotlin.math.pow

data class ForecastResultPoint(
    val dateTimestamp: Long,
    val predictedValue: Double,
    val lowerBound: Double,
    val upperBound: Double,
    val growthRatePct: Double
)

class ForecastingService(
    private val forecastingSnapshotDao: ForecastingSnapshotDao
) {

    /**
     * Generate future trend predictions based on historical daily/monthly values.
     * Uses Holt-Winters / Linear Trend models with confidence bounds.
     */
    suspend fun generateForecast(
        type: String, // REVENUE, SALES_VOLUME, EXPENSE, CASHFLOW
        historicalValues: List<Pair<Long, Double>>, // timestamp to value
        daysToForecast: Int = 30,
        alpha: Double = 0.3, // Level smoothing factor
        beta: Double = 0.1   // Trend smoothing factor
    ): List<ForecastResultPoint> {
        if (historicalValues.isEmpty()) return emptyList()

        // 1. Calculate linear trend and exponential smoothing
        val n = historicalValues.size
        var level = historicalValues.first().second
        var trend = if (n > 1) (historicalValues.last().second - historicalValues.first().second) / n else 0.0

        for (i in 1 until n) {
            val valI = historicalValues[i].second
            val prevLevel = level
            level = alpha * valI + (1 - alpha) * (level + trend)
            trend = beta * (level - prevLevel) + (1 - beta) * trend
        }

        // Variance estimation for confidence interval calculation
        val mean = historicalValues.map { it.second }.average()
        val variance = if (n > 1) {
            historicalValues.map { (it.second - mean).pow(2) }.sum() / (n - 1)
        } else {
            100.0
        }
        val stdDev = Math.sqrt(max(0.0, variance))

        val results = mutableListOf<ForecastResultPoint>()
        val snapshotsToInsert = mutableListOf<ForecastingSnapshotEntity>()

        val lastTimestamp = historicalValues.last().first
        val cal = Calendar.getInstance().apply { timeInMillis = lastTimestamp }

        val histStart = historicalValues.first().first
        val histEnd = lastTimestamp

        val baseGrowth = if (mean > 0) (trend / mean) * 100.0 else 0.0

        for (step in 1..daysToForecast) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
            val futureTimestamp = cal.timeInMillis

            val forecastVal = max(0.0, level + (step * trend))
            // Margin of error increases with forecast horizon
            val marginOfError = 1.96 * stdDev * Math.sqrt(1.0 + (step.toDouble() / n))
            val lower = max(0.0, forecastVal - marginOfError)
            val upper = forecastVal + marginOfError

            val point = ForecastResultPoint(
                dateTimestamp = futureTimestamp,
                predictedValue = forecastVal,
                lowerBound = lower,
                upperBound = upper,
                growthRatePct = baseGrowth
            )
            results.add(point)

            snapshotsToInsert.add(
                ForecastingSnapshotEntity(
                    forecastType = type,
                    forecastDate = futureTimestamp,
                    historicalStartDate = histStart,
                    historicalEndDate = histEnd,
                    predictedValue = forecastVal,
                    lowerBound = lower,
                    upperBound = upper,
                    confidenceInterval = 0.95,
                    growthRatePercentage = baseGrowth,
                    aiModelMetadataJson = "{\"algorithm\":\"HoltWintersExponentialSmoothing\",\"alpha\":$alpha,\"beta\":$beta,\"stdDev\":$stdDev}"
                )
            )
        }

        // Persist snapshots
        forecastingSnapshotDao.deleteForecastsByType(type)
        forecastingSnapshotDao.insertForecastSnapshots(snapshotsToInsert)

        return results
    }
}
