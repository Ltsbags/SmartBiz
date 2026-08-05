package com.example.features.reports.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.features.reports.models.SupplierAnalyticsData
import com.example.features.reports.widgets.KpiCard
import com.example.features.reports.widgets.ReportRowItem

@Composable
fun SupplierReportView(
    data: SupplierAnalyticsData,
    searchQuery: String,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    val filteredSuppliers = data.topSuppliersByVolume.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(modifier = modifier) {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Total Outstanding Payables",
                    value = "$currencySymbol${String.format("%.2f", data.totalOutstandingPayables)}",
                    subtitle = "Unpaid vendor credit",
                    icon = Icons.Default.MoneyOff,
                    accentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(6.dp))
                KpiCard(
                    title = "Active Suppliers",
                    value = "${data.topSuppliersByVolume.size}",
                    subtitle = "Supplies & procurement",
                    icon = Icons.Default.Business,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Supplier Outstanding Payables & Volume",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (filteredSuppliers.isEmpty()) {
            item {
                Text(
                    text = "No supplier records found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(filteredSuppliers) { supp ->
                ReportRowItem(
                    title = supp.name,
                    subtitle = "Purchases Total: $currencySymbol${String.format("%.2f", supp.totalPurchased)}",
                    metricText = "$currencySymbol${String.format("%.2f", supp.outstandingPayable)}",
                    secondaryMetricText = "Pending Payable",
                    testTag = "supp_report_${supp.supplierId}"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
