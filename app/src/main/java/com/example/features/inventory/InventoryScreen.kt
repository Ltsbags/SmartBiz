package com.example.features.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Category
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
import androidx.compose.material3.Surface
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
import com.example.core.database.entity.InventoryItemEntity
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing
import com.example.features.inventory.components.AddEditProductDialog
import com.example.features.inventory.components.CategoryManagementDialog
import com.example.features.inventory.components.FilterBottomSheet
import com.example.features.inventory.components.InventorySummaryCard
import com.example.features.inventory.components.ProductCard
import com.example.features.inventory.components.ProductDetailsDialog
import com.example.features.inventory.components.SortBottomSheet
import com.example.shared.widgets.EmptyStateWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<InventoryItemEntity?>(null) }

    var showDetailsDialog by remember { mutableStateOf(false) }
    var selectedProductDetails by remember { mutableStateOf<InventoryItemEntity?>(null) }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    var productToDelete by remember { mutableStateOf<InventoryItemEntity?>(null) }

    val filterSheetState = rememberModalBottomSheetState()
    val sortSheetState = rememberModalBottomSheetState()

    // Handle Snackbar messages with Undo action
    uiState.userMessage?.let { msg ->
        LaunchedEffect(msg) {
            val result = if (uiState.lastDeletedProduct != null && msg.contains("deleted")) {
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
                        text = "Inventory Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showCategoryDialog = true },
                        modifier = Modifier.testTag("manage_categories_btn")
                    ) {
                        Icon(Icons.Default.Category, contentDescription = "Manage Categories")
                    }
                    IconButton(
                        onClick = { viewModel.toggleViewMode() },
                        modifier = Modifier.testTag("toggle_view_btn")
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
                    productToEdit = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        },
        modifier = modifier.testTag("inventory_screen")
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
                    placeholder = { Text("Search products, SKU, barcode...") },
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
                        .testTag("search_bar")
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier.testTag("filter_btn")
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (uiState.selectedCategory != null || uiState.filterLowStockOnly || uiState.filterOutOfStockOnly || uiState.filterArchivedOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { showSortSheet = true },
                    modifier = Modifier.testTag("sort_btn")
                ) {
                    Icon(
                        Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Categories Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { viewModel.setSelectedCategory(null) },
                    label = { Text("All") }
                )
                uiState.categories.forEach { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category.name,
                        onClick = { viewModel.setSelectedCategory(category.name) },
                        label = { Text(category.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            // Summary Card Header
            InventorySummaryCard(
                totalProductsCount = uiState.totalProductsCount,
                lowStockCount = uiState.lowStockCount,
                outOfStockCount = uiState.outOfStockCount,
                totalInventoryValue = uiState.totalInventoryValue,
                currencySymbol = uiState.currencySymbol
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            // Products Grid / List Display
            if (uiState.products.isEmpty()) {
                EmptyStateWidget(
                    title = "No Products Found",
                    description = if (uiState.searchQuery.isNotEmpty()) "No results matching '${uiState.searchQuery}'" else "Tap '+' to add your first inventory item.",
                    actionLabel = "Add Product",
                    onActionClick = {
                        productToEdit = null
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
                        items(uiState.products, key = { it.id }) { item ->
                            ProductCard(
                                item = item,
                                currencySymbol = uiState.currencySymbol,
                                isGridView = true,
                                onItemClick = {
                                    selectedProductDetails = item
                                    showDetailsDialog = true
                                },
                                onEditClick = {
                                    productToEdit = item
                                    showAddEditDialog = true
                                },
                                onDuplicateClick = { viewModel.duplicateProduct(item) },
                                onArchiveClick = { viewModel.toggleArchiveProduct(item) },
                                onDeleteClick = { productToDelete = item }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(Spacing.m),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.products, key = { it.id }) { item ->
                            ProductCard(
                                item = item,
                                currencySymbol = uiState.currencySymbol,
                                isGridView = false,
                                onItemClick = {
                                    selectedProductDetails = item
                                    showDetailsDialog = true
                                },
                                onEditClick = {
                                    productToEdit = item
                                    showAddEditDialog = true
                                },
                                onDuplicateClick = { viewModel.duplicateProduct(item) },
                                onArchiveClick = { viewModel.toggleArchiveProduct(item) },
                                onDeleteClick = { productToDelete = item }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Dialog Modal
    if (showAddEditDialog) {
        AddEditProductDialog(
            productToEdit = productToEdit,
            categories = uiState.categories,
            onSave = { product ->
                viewModel.saveProduct(product)
                showAddEditDialog = false
            },
            onDismiss = { showAddEditDialog = false }
        )
    }

    // Product Details Dialog Modal
    if (showDetailsDialog && selectedProductDetails != null) {
        ProductDetailsDialog(
            item = selectedProductDetails!!,
            currencySymbol = uiState.currencySymbol,
            onEdit = {
                productToEdit = selectedProductDetails
                showAddEditDialog = true
            },
            onDuplicate = { viewModel.duplicateProduct(selectedProductDetails!!) },
            onArchive = { viewModel.toggleArchiveProduct(selectedProductDetails!!) },
            onDelete = { productToDelete = selectedProductDetails },
            onDismiss = { showDetailsDialog = false }
        )
    }

    // Category Management Dialog Modal
    if (showCategoryDialog) {
        CategoryManagementDialog(
            categories = uiState.categories,
            onAddCategory = { name, color -> viewModel.addCategory(name, color) },
            onDeleteCategory = { category -> viewModel.deleteCategory(category) },
            onDismiss = { showCategoryDialog = false }
        )
    }

    // Filter Bottom Sheet Modal
    if (showFilterSheet) {
        FilterBottomSheet(
            sheetState = filterSheetState,
            categories = uiState.categories,
            selectedCategory = uiState.selectedCategory,
            filterLowStockOnly = uiState.filterLowStockOnly,
            filterOutOfStockOnly = uiState.filterOutOfStockOnly,
            filterArchivedOnly = uiState.filterArchivedOnly,
            onCategorySelect = { viewModel.setSelectedCategory(it) },
            onLowStockToggle = { viewModel.setFilterLowStockOnly(it) },
            onOutOfStockToggle = { viewModel.setFilterOutOfStockOnly(it) },
            onArchivedToggle = { viewModel.setFilterArchivedOnly(it) },
            onResetFilters = { viewModel.resetFilters() },
            onDismiss = { showFilterSheet = false }
        )
    }

    // Sort Bottom Sheet Modal
    if (showSortSheet) {
        SortBottomSheet(
            sheetState = sortSheetState,
            currentSort = uiState.sortBy,
            onSortSelect = { viewModel.setSortOption(it) },
            onDismiss = { showSortSheet = false }
        )
    }

    // Delete Confirmation Dialog Modal
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product") },
            text = { Text("Are you sure you want to delete '${product.name}'? You can undo this action immediately after deletion.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(product)
                        productToDelete = null
                        if (selectedProductDetails?.id == product.id) {
                            showDetailsDialog = false
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
