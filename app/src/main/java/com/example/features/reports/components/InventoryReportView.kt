package com.example.features.reports.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.features.reports.models.InventoryAnalyticsData
import com.example.features.reports.widgets.KpiCard
import com.example.features.reports.widgets.ReportRowItem

@Composable
fun InventoryReportView(
    data: InventoryAnalyticsData,
    searchQuery: String,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    val filteredFastMoving = data.fastMovingProducts.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.sku.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(modifier = modifier) {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Total Inventory Value",
                    value = "$currencySymbol${String.format("%.2f", data.totalValuation)}",
                    subtitle = "Based on purchase cost",
                    icon = Icons.Default.Inventory2,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(6.dp))
                KpiCard(
                    title = "Low Stock Items",
                    value = "${data.lowStockCount}",
                    subtitle = "Needs reordering",
                    icon = Icons.Default.Warning,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Out of Stock Items",
                    value = "${data.outOfStockCount}",
                    subtitle = "Zero inventory",
                    icon = Icons.Default.Warning,
                    accentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(6.dp))
                KpiCard(
                    title = "Fast Moving Items",
                    value = "${data.fastMovingProducts.size}",
                    subtitle = "Top velocity",
                    icon = Icons.Default.Speed,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Fast Moving Stock Movement",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (filteredFastMoving.isEmpty()) {
            item {
                Text(
                    text = "No fast moving stock data available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(filteredFastMoving) { prod ->
                ReportRowItem(
                    title = prod.name,
                    subtitle = "SKU: ${prod.sku} • Revenue: $currencySymbol${String.format("%.2f", prod.totalRevenue)}",
                    metricText = "${prod.quantitySold.toInt()} sold",
                    testTag = "inv_fast_${prod.productId}"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
