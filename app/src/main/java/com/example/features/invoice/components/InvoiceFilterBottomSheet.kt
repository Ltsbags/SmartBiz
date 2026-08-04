package com.example.features.invoice.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.core.theme.Spacing
import com.example.features.invoice.InvoiceFilterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceFilterBottomSheet(
    currentFilterState: InvoiceFilterState,
    onApplyFilter: (InvoiceFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var statusFilter by remember { mutableStateOf(currentFilterState.statusFilter) }
    var paymentStatusFilter by remember { mutableStateOf(currentFilterState.paymentStatusFilter) }
    var dateFilter by remember { mutableStateOf(currentFilterState.dateFilter) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.l)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Filter Invoices",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = {
                        statusFilter = "ALL"
                        paymentStatusFilter = "ALL"
                        dateFilter = "ALL"
                    },
                    modifier = Modifier.testTag("btn_reset_filters")
                ) {
                    Text("Reset")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            Text("Status", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier.padding(vertical = Spacing.xs)
            ) {
                listOf("ALL", "DRAFT", "COMPLETED", "CANCELLED").forEach { st ->
                    FilterChip(
                        selected = statusFilter == st,
                        onClick = { statusFilter = st },
                        label = { Text(st) },
                        modifier = Modifier.testTag("filter_status_$st")
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            Text("Payment Status", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier.padding(vertical = Spacing.xs)
            ) {
                listOf("ALL", "PAID", "UNPAID", "PARTIAL").forEach { pst ->
                    FilterChip(
                        selected = paymentStatusFilter == pst,
                        onClick = { paymentStatusFilter = pst },
                        label = { Text(pst) },
                        modifier = Modifier.testTag("filter_payment_$pst")
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            Text("Date Range", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier.padding(vertical = Spacing.xs)
            ) {
                listOf("ALL" to "All Time", "TODAY" to "Today", "THIS_WEEK" to "This Week", "THIS_MONTH" to "This Month").forEach { (dtKey, dtLabel) ->
                    FilterChip(
                        selected = dateFilter == dtKey,
                        onClick = { dateFilter = dtKey },
                        label = { Text(dtLabel) },
                        modifier = Modifier.testTag("filter_date_$dtKey")
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.l))

            Button(
                onClick = {
                    onApplyFilter(
                        InvoiceFilterState(
                            statusFilter = statusFilter,
                            paymentStatusFilter = paymentStatusFilter,
                            dateFilter = dateFilter
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("btn_apply_filters")
            ) {
                Text("Apply Filters")
            }
        }
    }
}
