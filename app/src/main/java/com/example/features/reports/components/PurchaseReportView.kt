package com.example.features.reports.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.features.reports.models.PurchaseAnalyticsData
import com.example.features.reports.widgets.KpiCard
import com.example.features.reports.widgets.ReportRowItem

@Composable
fun PurchaseReportView(
    data: PurchaseAnalyticsData,
    searchQuery: String,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    val filteredSuppliers = data.topSuppliers.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(modifier = modifier) {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Total Procurement",
                    value = "$currencySymbol${String.format("%.2f", data.totalPurchases)}",
                    subtitle = "${data.purchaseCount} PO orders",
                    icon = Icons.Default.ShoppingBag,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(6.dp))
                KpiCard(
                    title = "Paid to Vendors",
                    value = "$currencySymbol${String.format("%.2f", data.paidPurchases)}",
                    icon = Icons.Default.Payment,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Pending Vendor Payables",
                    value = "$currencySymbol${String.format("%.2f", data.pendingPurchases)}",
                    icon = Icons.Default.MoneyOff,
                    accentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(6.dp))
                KpiCard(
                    title = "Input GST Paid",
                    value = "$currencySymbol${String.format("%.2f", data.totalGstPaid)}",
                    icon = Icons.Default.LocalShipping,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Supplier Purchase Summary",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (filteredSuppliers.isEmpty()) {
            item {
                Text(
                    text = "No vendor purchases recorded for this period.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(filteredSuppliers) { supp ->
                ReportRowItem(
                    title = supp.name,
                    subtitle = "Outstanding Payable: $currencySymbol${String.format("%.2f", supp.outstandingPayable)}",
                    metricText = "$currencySymbol${String.format("%.2f", supp.totalPurchased)}",
                    testTag = "purchase_supp_${supp.supplierId}"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
