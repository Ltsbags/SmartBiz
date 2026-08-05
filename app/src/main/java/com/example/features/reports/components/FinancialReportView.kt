package com.example.features.reports.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.features.reports.models.FinancialAnalyticsData
import com.example.features.reports.widgets.ChartType
import com.example.features.reports.widgets.InteractiveChartWidget
import com.example.features.reports.widgets.KpiCard

@Composable
fun FinancialReportView(
    data: FinancialAnalyticsData,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Net Operating Profit",
                    value = "$currencySymbol${String.format("%.2f", data.netProfit)}",
                    subtitle = "${String.format("%.1f", data.netMarginPercentage)}% net margin",
                    icon = Icons.Default.TrendingUp,
                    accentColor = if (data.netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(6.dp))
                KpiCard(
                    title = "Gross Profit",
                    value = "$currencySymbol${String.format("%.2f", data.grossProfit)}",
                    subtitle = "Sales minus Cost of Goods",
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Operating Expenses",
                    value = "$currencySymbol${String.format("%.2f", data.totalExpenses)}",
                    icon = Icons.Default.MoneyOff,
                    accentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(6.dp))
                KpiCard(
                    title = "Net Cash Flow",
                    value = "$currencySymbol${String.format("%.2f", data.netCashFlow)}",
                    subtitle = "In: $currencySymbol${String.format("%.0f", data.totalCashIn)} | Out: $currencySymbol${String.format("%.0f", data.totalCashOut)}",
                    icon = Icons.Default.AccountBalance,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            InteractiveChartWidget(
                title = "Expense Breakdown by Category",
                chartType = ChartType.DONUT,
                pieSegments = data.expenseBreakdown,
                currencySymbol = currencySymbol
            )

            Spacer(modifier = Modifier.height(16.dp))

            InteractiveChartWidget(
                title = "Other Income Breakdown",
                chartType = ChartType.PIE,
                pieSegments = data.incomeBreakdown,
                currencySymbol = currencySymbol
            )
        }
    }
}
