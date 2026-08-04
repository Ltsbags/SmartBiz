package com.example.features.suppliers.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.features.suppliers.SupplierFilterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierFilterBottomSheet(
    filterState: SupplierFilterState,
    onDismiss: () -> Unit,
    onApplyFilter: (SupplierFilterState) -> Unit
) {
    var status by remember { mutableStateOf(filterState.status) }
    var hasOutstanding by remember { mutableStateOf(filterState.hasOutstanding) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Filter Suppliers",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Status",
                style = androidx.compose.material3.MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("ALL", "ACTIVE", "INACTIVE", "ARCHIVED").forEach { option ->
                    FilterChip(
                        selected = status == option,
                        onClick = { status = option },
                        label = { Text(option) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = hasOutstanding,
                    onCheckedChange = { hasOutstanding = it }
                )
                Text("Only with Pending Balance / Outstanding")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        status = "ALL"
                        hasOutstanding = false
                        onApplyFilter(SupplierFilterState())
                    }
                ) {
                    Text("Reset")
                }

                Button(
                    onClick = {
                        onApplyFilter(
                            SupplierFilterState(
                                status = status,
                                hasOutstanding = hasOutstanding
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
