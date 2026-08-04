package com.example.features.customers

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.entity.CustomerEntity
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing
import com.example.features.customers.components.AddEditCustomerDialog
import com.example.features.customers.components.CustomerCard
import com.example.features.customers.components.CustomerDetailsDialog
import com.example.features.customers.components.CustomerFilterBottomSheet
import com.example.features.customers.components.CustomerSortBottomSheet
import com.example.features.customers.components.CustomerSummaryCard
import com.example.shared.widgets.EmptyStateWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    viewModel: CustomerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }

    var showDetailsDialog by remember { mutableStateOf(false) }
    var selectedCustomerDetails by remember { mutableStateOf<CustomerEntity?>(null) }

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    val filterSheetState = rememberModalBottomSheetState()
    val sortSheetState = rememberModalBottomSheetState()

    val customerTypes = listOf("Retail", "Wholesale", "Distributor", "Corporate", "Other")

    // Handle Snackbar messages with Undo action
    uiState.userMessage?.let { msg ->
        LaunchedEffect(msg) {
            val result = if (uiState.lastDeletedCustomer != null && msg.contains("deleted")) {
                snackbarHostState.showSnackbar(
                    message = msg,
                    actionLabel = "UNDO",
                    duration = SnackbarDuration.Short
                )
            } else {
                snackbarHostState.showSnackbar(
                    message = msg,
                    duration = SnackbarDuration.Short
                )
            }
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Customer Directory",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleViewMode() },
                        modifier = Modifier.testTag("toggle_customer_view_btn")
                    ) {
                        Icon(
                            imageVector = if (uiState.isGridView) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = "Toggle Grid/List View"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    customerToEdit = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        },
        modifier = modifier.testTag("customer_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.m)
        ) {
            // Search Bar & Filter/Sort Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.s),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search name, business, mobile, GST...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(Dimensions.radius12),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("customer_search_bar")
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier.testTag("customer_filter_btn")
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter Customers",
                        tint = if (uiState.filterState.isFilteringActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { showSortSheet = true },
                    modifier = Modifier.testTag("customer_sort_btn")
                ) {
                    Icon(
                        Icons.Default.Sort,
                        contentDescription = "Sort Customers",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Customer Type Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                FilterChip(
                    selected = uiState.selectedCustomerType == null,
                    onClick = { viewModel.setSelectedTypeChip(null) },
                    label = { Text("All") }
                )
                customerTypes.forEach { type ->
                    FilterChip(
                        selected = uiState.selectedCustomerType == type,
                        onClick = { viewModel.setSelectedTypeChip(type) },
                        label = { Text(type) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            // Summary Header Card
            CustomerSummaryCard(
                totalCustomersCount = uiState.totalCustomersCount,
                activeCustomersCount = uiState.activeCustomersCount,
                totalOutstanding = uiState.totalOutstanding,
                newThisMonthCount = uiState.newCustomersThisMonthCount,
                currencySymbol = uiState.currencySymbol
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            // Customer List / Grid Display
            if (uiState.customers.isEmpty()) {
                EmptyStateWidget(
                    title = "No Customers Found",
                    description = if (uiState.searchQuery.isNotEmpty())
                        "No results matching '${uiState.searchQuery}'"
                    else
                        "Tap '+' to add your first customer account.",
                    actionLabel = "Add Customer",
                    onActionClick = {
                        customerToEdit = null
                        showAddEditDialog = true
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                if (uiState.isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                        verticalArrangement = Arrangement.spacedBy(Spacing.m),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.customers, key = { it.id }) { customer ->
                            CustomerCard(
                                customer = customer,
                                currencySymbol = uiState.currencySymbol,
                                isGridView = true,
                                onCustomerClick = {
                                    selectedCustomerDetails = customer
                                    showDetailsDialog = true
                                },
                                onEditClick = {
                                    customerToEdit = customer
                                    showAddEditDialog = true
                                },
                                onArchiveClick = { viewModel.toggleArchiveCustomer(customer) },
                                onDeleteClick = { customerToDelete = customer }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(Spacing.m),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.customers, key = { it.id }) { customer ->
                            CustomerCard(
                                customer = customer,
                                currencySymbol = uiState.currencySymbol,
                                isGridView = false,
                                onCustomerClick = {
                                    selectedCustomerDetails = customer
                                    showDetailsDialog = true
                                },
                                onEditClick = {
                                    customerToEdit = customer
                                    showAddEditDialog = true
                                },
                                onArchiveClick = { viewModel.toggleArchiveCustomer(customer) },
                                onDeleteClick = { customerToDelete = customer }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Dialog Modal
    if (showAddEditDialog) {
        AddEditCustomerDialog(
            customerToEdit = customerToEdit,
            onSave = { customer ->
                viewModel.saveCustomer(customer)
                showAddEditDialog = false
            },
            onDismiss = { showAddEditDialog = false }
        )
    }

    // Customer Details Dialog Modal
    if (showDetailsDialog && selectedCustomerDetails != null) {
        CustomerDetailsDialog(
            customer = selectedCustomerDetails!!,
            ledgerFlow = viewModel.getLedgerForCustomer(selectedCustomerDetails!!.id),
            currencySymbol = uiState.currencySymbol,
            onEdit = {
                customerToEdit = selectedCustomerDetails
                showAddEditDialog = true
            },
            onArchiveToggle = { viewModel.toggleArchiveCustomer(selectedCustomerDetails!!) },
            onDelete = { customerToDelete = selectedCustomerDetails },
            onDismiss = { showDetailsDialog = false }
        )
    }

    // Filter Bottom Sheet Modal
    if (showFilterSheet) {
        CustomerFilterBottomSheet(
            sheetState = filterSheetState,
            currentFilters = uiState.filterState,
            cities = uiState.allCities,
            states = uiState.allStates,
            onApplyFilters = { viewModel.setFilterState(it) },
            onResetFilters = { viewModel.resetFilters() },
            onDismiss = { showFilterSheet = false }
        )
    }

    // Sort Bottom Sheet Modal
    if (showSortSheet) {
        CustomerSortBottomSheet(
            sheetState = sortSheetState,
            currentSort = uiState.sortOption,
            onSortSelect = { viewModel.setSortOption(it) },
            onDismiss = { showSortSheet = false }
        )
    }

    // Delete Confirmation Dialog Modal
    customerToDelete?.let { customer ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Delete Customer Account") },
            text = { Text("Are you sure you want to delete '${customer.name}'? You can undo this action immediately after deletion.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomer(customer)
                        customerToDelete = null
                        if (selectedCustomerDetails?.id == customer.id) {
                            showDetailsDialog = false
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
