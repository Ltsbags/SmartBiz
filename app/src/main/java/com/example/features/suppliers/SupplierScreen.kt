package com.example.features.suppliers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.features.suppliers.components.AddEditSupplierDialog
import com.example.features.suppliers.components.SupplierCard
import com.example.features.suppliers.components.SupplierDetailsDialog
import com.example.features.suppliers.components.SupplierFilterBottomSheet
import com.example.features.suppliers.components.SupplierSortBottomSheet
import com.example.features.suppliers.components.SupplierSummaryCard

@Composable
fun SupplierScreen(
    viewModel: SupplierViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddSupplierClicked() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("btn_add_supplier")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Supplier")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Summary Card
            SupplierSummaryCard(
                totalSuppliers = uiState.totalSuppliersCount,
                activeSuppliers = uiState.activeSuppliersCount,
                totalOutstanding = uiState.totalOutstanding
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search and Controls Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search supplier name, code...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_search_suppliers")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.toggleFilterSheet(true) },
                    modifier = Modifier.testTag("btn_filter_suppliers")
                ) {
                    Icon(Icons.Outlined.FilterList, contentDescription = "Filter")
                }

                IconButton(
                    onClick = { viewModel.toggleSortSheet(true) },
                    modifier = Modifier.testTag("btn_sort_suppliers")
                ) {
                    Icon(Icons.Outlined.SortByAlpha, contentDescription = "Sort")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredSuppliers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isBlank()) "No suppliers found. Tap + to add one." else "No matching suppliers found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = uiState.filteredSuppliers,
                        key = { it.id }
                    ) { supplier ->
                        SupplierCard(
                            supplier = supplier,
                            onClick = { viewModel.onSupplierSelected(supplier) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showAddEditDialog) {
        AddEditSupplierDialog(
            supplier = uiState.supplierToEdit,
            onDismiss = { viewModel.dismissAddEditDialog() },
            onSave = { viewModel.saveSupplier(it) }
        )
    }

    if (uiState.showDetailsDialog && uiState.selectedSupplier != null) {
        SupplierDetailsDialog(
            supplier = uiState.selectedSupplier!!,
            onDismiss = { viewModel.dismissDetailsDialog() },
            onEdit = { viewModel.onEditSupplierClicked(it) },
            onDelete = { viewModel.deleteSupplier(it) },
            onArchiveToggle = {
                if (it.isArchived) viewModel.restoreSupplier(it) else viewModel.archiveSupplier(it)
            }
        )
    }

    if (uiState.showFilterSheet) {
        SupplierFilterBottomSheet(
            filterState = uiState.filterState,
            onDismiss = { viewModel.toggleFilterSheet(false) },
            onApplyFilter = { viewModel.onFilterChanged(it) }
        )
    }

    if (uiState.showSortSheet) {
        SupplierSortBottomSheet(
            currentSort = uiState.sortOption,
            onDismiss = { viewModel.toggleSortSheet(false) },
            onSortSelected = { viewModel.onSortChanged(it) }
        )
    }
}
