package com.example.features.reports.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.features.reports.models.CustomerAnalyticsData
import com.example.features.reports.widgets.KpiCard
import com.example.features.reports.widgets.ReportRowItem

@Composable
fun CustomerReportView(
    data: CustomerAnalyticsData,
    searchQuery: String,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    val filteredCustomers = data.topCustomersBySpending.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(modifier = modifier) {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Total Outstanding Receivables",
                    value = "$currencySymbol${String.format("%.2f", data.totalOutstandingReceivables)}",
                    subtitle = "Unpaid customer balance",
                    icon = Icons.Default.MoneyOff,
                    accentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(6.dp))
                KpiCard(
                    title = "Key Customers",
                    value = "${data.topCustomersBySpending.size}",
                    subtitle = "Active order volume",
                    icon = Icons.Default.People,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Customer Spending & Outstanding Balance",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (filteredCustomers.isEmpty()) {
            item {
                Text(
                    text = "No customer spending records found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(filteredCustomers) { cust ->
                ReportRowItem(
                    title = cust.name,
                    subtitle = "Outstanding Balance: $currencySymbol${String.format("%.2f", cust.outstandingBalance)}",
                    metricText = "$currencySymbol${String.format("%.2f", cust.totalSpent)}",
                    secondaryMetricText = "Total Purchased",
                    testTag = "cust_report_${cust.customerId}"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
