package com.example.features.purchases.components

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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.features.purchases.PurchaseFilterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseFilterBottomSheet(
    filterState: PurchaseFilterState,
    onDismiss: () -> Unit,
    onApplyFilter: (PurchaseFilterState) -> Unit
) {
    var status by remember { mutableStateOf(filterState.status) }
    var paymentStatus by remember { mutableStateOf(filterState.paymentStatus) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Filter Purchase Orders",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Order Status", style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("ALL", "DRAFT", "ORDERED", "RECEIVED", "CANCELLED").forEach { s ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = { Text(s) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Payment Status", style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("ALL", "UNPAID", "PARTIAL", "PAID").forEach { p ->
                    FilterChip(
                        selected = paymentStatus == p,
                        onClick = { paymentStatus = p },
                        label = { Text(p) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        status = "ALL"
                        paymentStatus = "ALL"
                        onApplyFilter(PurchaseFilterState())
                    }
                ) {
                    Text("Reset")
                }

                Button(
                    onClick = {
                        onApplyFilter(
                            PurchaseFilterState(
                                status = status,
                                paymentStatus = paymentStatus
                            )
                        )
                    }
                ) {
                    Text("Apply Filter")
                }
            }
        }
    }
}
