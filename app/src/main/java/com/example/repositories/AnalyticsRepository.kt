package com.example.repositories

import androidx.compose.ui.graphics.Color
import com.example.core.database.dao.ReportDao
import com.example.features.reports.models.ChartDataPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnalyticsRepository(
    private val reportDao: ReportDao
) {
    suspend fun getDailySalesTrend(startDate: Long, endDate: Long): List<ChartDataPoint> = withContext(Dispatchers.IO) {
        val points = mutableListOf<ChartDataPoint>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = startDate

        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())

        val dayMs = 24 * 60 * 60 * 1000L
        var currentStart = startDate

        // Breakdown into up to 10 intervals
        val totalDays = maxOf(1, ((endDate - startDate) / dayMs).toInt())
        val stepDays = maxOf(1, totalDays / 10)

        while (currentStart < endDate) {
            val currentEnd = minOf(endDate, currentStart + (stepDays * dayMs) - 1L)
            val salesAmt = reportDao.getTotalSalesAmount(currentStart, currentEnd)
            val purchaseAmt = reportDao.getTotalPurchasesAmount(currentStart, currentEnd)

            cal.timeInMillis = currentStart
            val label = sdf.format(cal.time)

            points.add(
                ChartDataPoint(
                    label = label,
                    value = salesAmt.toFloat(),
                    color = Color(0xFF2196F3),
                    dateMs = currentStart,
                    secondaryValue = purchaseAmt.toFloat()
                )
            )

            currentStart += (stepDays * dayMs)
        }

        points
    }

    suspend fun getFinancialTrend(startDate: Long, endDate: Long): List<ChartDataPoint> = withContext(Dispatchers.IO) {
        val salesTrend = getDailySalesTrend(startDate, endDate)
        salesTrend.map { pt ->
            val revenue = pt.value
            val purchases = pt.secondaryValue
            val net = revenue - purchases
            ChartDataPoint(
                label = pt.label,
                value = net,
                color = if (net >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                dateMs = pt.dateMs,
                secondaryValue = revenue
            )
        }
    }
}
