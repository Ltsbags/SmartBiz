package com.example.features.reports.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.features.reports.models.GstSummaryData
import com.example.features.reports.widgets.KpiCard
import com.example.features.reports.widgets.SummaryTile

@Composable
fun GstSummaryView(
    data: GstSummaryData,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            KpiCard(
                title = "Output GST Collected (Sales)",
                value = "$currencySymbol${String.format("%.2f", data.outputGstSales)}",
                icon = Icons.Default.ArrowUpward,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.padding(6.dp))
            KpiCard(
                title = "Input GST Credit (Purchases)",
                value = "$currencySymbol${String.format("%.2f", data.inputGstPurchases)}",
                icon = Icons.Default.ArrowDownward,
                accentColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "GST Settlement Summary Foundation",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                SummaryTile(
                    label = "Total Taxable Sales Turnover",
                    value = "$currencySymbol${String.format("%.2f", data.totalTaxableSales)}"
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SummaryTile(
                    label = "Total Taxable Procurement Turnover",
                    value = "$currencySymbol${String.format("%.2f", data.totalTaxablePurchases)}"
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SummaryTile(
                    label = "Net GST Liability / Payable",
                    value = "$currencySymbol${String.format("%.2f", data.netGstPayable)}",
                    valueColor = if (data.netGstPayable >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
