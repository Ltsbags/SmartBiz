package com.example.features.purchases

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
import com.example.features.purchases.components.AddEditPurchaseDialog
import com.example.features.purchases.components.PurchaseCard
import com.example.features.purchases.components.PurchaseDetailsDialog
import com.example.features.purchases.components.PurchaseFilterBottomSheet
import com.example.features.purchases.components.PurchaseSortBottomSheet
import com.example.features.purchases.components.PurchaseSummaryCard

@Composable
fun PurchaseScreen(
    viewModel: PurchaseViewModel,
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
                onClick = { viewModel.onAddPurchaseClicked() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("btn_add_purchase")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Purchase Order")
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

            // Summary Header
            PurchaseSummaryCard(
                totalOrders = uiState.totalPurchasesCount,
                totalAmount = uiState.totalPurchaseAmount,
                pendingAmount = uiState.totalPendingAmount
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search and Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search PO #, supplier...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_search_purchases")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.toggleFilterSheet(true) },
                    modifier = Modifier.testTag("btn_filter_purchases")
                ) {
                    Icon(Icons.Outlined.FilterList, contentDescription = "Filter")
                }

                IconButton(
                    onClick = { viewModel.toggleSortSheet(true) },
                    modifier = Modifier.testTag("btn_sort_purchases")
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
            } else if (uiState.filteredPurchases.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isBlank()) "No purchase orders recorded yet. Tap + to create one." else "No matching purchase orders.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = uiState.filteredPurchases,
                        key = { it.purchase.id }
                    ) { item ->
                        PurchaseCard(
                            purchaseWithItems = item,
                            onClick = { viewModel.onPurchaseSelected(item) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showAddEditDialog) {
        AddEditPurchaseDialog(
            purchaseWithItems = uiState.purchaseToEdit,
            suppliers = uiState.suppliers,
            availableProducts = uiState.availableProducts,
            onDismiss = { viewModel.dismissAddEditDialog() },
            onSave = { purchase, items -> viewModel.savePurchase(purchase, items) }
        )
    }

    if (uiState.showDetailsDialog && uiState.selectedPurchase != null) {
        PurchaseDetailsDialog(
            purchaseWithItems = uiState.selectedPurchase!!,
            onDismiss = { viewModel.dismissDetailsDialog() },
            onEdit = { viewModel.onEditPurchaseClicked(it) },
            onMarkAsReceived = { viewModel.markAsReceived(it) },
            onCancel = { viewModel.cancelPurchase(it) },
            onDelete = { viewModel.deletePurchase(it) }
        )
    }

    if (uiState.showFilterSheet) {
        PurchaseFilterBottomSheet(
            filterState = uiState.filterState,
            onDismiss = { viewModel.toggleFilterSheet(false) },
            onApplyFilter = { viewModel.onFilterChanged(it) }
        )
    }

    if (uiState.showSortSheet) {
        PurchaseSortBottomSheet(
            currentSort = uiState.sortOption,
            onDismiss = { viewModel.toggleSortSheet(false) },
            onSortSelected = { viewModel.onSortChanged(it) }
        )
    }
}
