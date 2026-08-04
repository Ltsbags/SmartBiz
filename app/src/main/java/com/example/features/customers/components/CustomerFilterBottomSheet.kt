package com.example.features.customers.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.core.theme.Spacing
import com.example.features.customers.CustomerFilterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFilterBottomSheet(
    sheetState: SheetState,
    currentFilters: CustomerFilterState,
    cities: List<String>,
    states: List<String>,
    onApplyFilters: (CustomerFilterState) -> Unit,
    onResetFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(currentFilters.customerType) }
    var hasOutstandingOnly by remember { mutableStateOf(currentFilters.hasOutstandingOnly) }
    var showArchivedOnly by remember { mutableStateOf(currentFilters.showArchivedOnly) }
    var selectedCity by remember { mutableStateOf(currentFilters.selectedCity) }
    var selectedState by remember { mutableStateOf(currentFilters.selectedState) }

    val customerTypes = listOf("Retail", "Wholesale", "Distributor", "Corporate", "Other")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("customer_filter_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.l)
                .padding(bottom = Spacing.xl)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Customers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = {
                        selectedType = null
                        hasOutstandingOnly = false
                        showArchivedOnly = false
                        selectedCity = null
                        selectedState = null
                        onResetFilters()
                    },
                    modifier = Modifier.testTag("reset_filters_btn")
                ) {
                    Text("Reset All")
                }
            }

            // Customer Type Filter
            Text(
                text = "Customer Type",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                    label = { Text("All Types") }
                )
                customerTypes.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = if (selectedType == type) null else type },
                        label = { Text(type) }
                    )
                }
            }

            // Balance Filter Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Outstanding Balance Only", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(text = "Show clients with pending receivables", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = hasOutstandingOnly,
                    onCheckedChange = { hasOutstandingOnly = it }
                )
            }

            // Active vs Archived Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Show Archived Customers", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(text = "View soft-deleted or inactive customer profiles", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = showArchivedOnly,
                    onCheckedChange = { showArchivedOnly = it }
                )
            }

            // City Filter
            if (cities.isNotEmpty()) {
                Text(
                    text = "City",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    FilterChip(
                        selected = selectedCity == null,
                        onClick = { selectedCity = null },
                        label = { Text("All Cities") }
                    )
                    cities.forEach { city ->
                        FilterChip(
                            selected = selectedCity == city,
                            onClick = { selectedCity = if (selectedCity == city) null else city },
                            label = { Text(city) }
                        )
                    }
                }
            }

            // State Filter
            if (states.isNotEmpty()) {
                Text(
                    text = "State",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    FilterChip(
                        selected = selectedState == null,
                        onClick = { selectedState = null },
                        label = { Text("All States") }
                    )
                    states.forEach { state ->
                        FilterChip(
                            selected = selectedState == state,
                            onClick = { selectedState = if (selectedState == state) null else state },
                            label = { Text(state) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            Button(
                onClick = {
                    onApplyFilters(
                        CustomerFilterState(
                            customerType = selectedType,
                            hasOutstandingOnly = hasOutstandingOnly,
                            showArchivedOnly = showArchivedOnly,
                            selectedCity = selectedCity,
                            selectedState = selectedState
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_filters_btn")
            ) {
                Text("Apply Filters")
            }
        }
    }
}
