package com.example.features.reports.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.features.reports.models.SalesAnalyticsData
import com.example.features.reports.widgets.KpiCard
import com.example.features.reports.widgets.ReportRowItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp

@Composable
fun SalesReportView(
    data: SalesAnalyticsData,
    searchQuery: String,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    val filteredProducts = data.topSellingProducts.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.sku.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(modifier = modifier) {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Total Sales Revenue",
                    value = "$currencySymbol${String.format("%.2f", data.totalRevenue)}",
                    subtitle = "${data.invoiceCount} invoices generated",
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(6.dp))
                KpiCard(
                    title = "Avg Invoice Value",
                    value = "$currencySymbol${String.format("%.2f", data.averageInvoiceValue)}",
                    subtitle = "Per sales order",
                    icon = Icons.Default.ReceiptLong,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Paid Revenue",
                    value = "$currencySymbol${String.format("%.2f", data.paidRevenue)}",
                    icon = Icons.Default.TrendingUp,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(6.dp))
                KpiCard(
                    title = "Uncollected Receivables",
                    value = "$currencySymbol${String.format("%.2f", data.outstandingRevenue)}",
                    icon = Icons.Default.Star,
                    accentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Top Selling Products",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (filteredProducts.isEmpty()) {
            item {
                Text(
                    text = "No sales products recorded for this period.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(filteredProducts) { item ->
                ReportRowItem(
                    title = item.name,
                    subtitle = "SKU: ${item.sku} • Sold: ${item.quantitySold.toInt()} units",
                    metricText = "$currencySymbol${String.format("%.2f", item.totalRevenue)}",
                    testTag = "sales_item_${item.productId}"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
