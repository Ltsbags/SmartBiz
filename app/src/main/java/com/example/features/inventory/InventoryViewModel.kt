package com.example.features.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.CategoryEntity
import com.example.core.database.entity.InventoryItemEntity
import com.example.features.inventory.components.InventorySortOption
import com.example.repositories.CategoryRepository
import com.example.repositories.InventoryRepository
import com.example.repositories.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InventoryUiState(
    val products: List<InventoryItemEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val filterLowStockOnly: Boolean = false,
    val filterOutOfStockOnly: Boolean = false,
    val filterArchivedOnly: Boolean = false,
    val sortBy: InventorySortOption = InventorySortOption.NAME_ASC,
    val isGridView: Boolean = false,
    val isLoading: Boolean = false,
    val currencySymbol: String = "$",
    val totalProductsCount: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val totalInventoryValue: Double = 0.0,
    val userMessage: String? = null,
    val lastDeletedProduct: InventoryItemEntity? = null
)

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _filterLowStockOnly = MutableStateFlow(false)
    private val _filterOutOfStockOnly = MutableStateFlow(false)
    private val _filterArchivedOnly = MutableStateFlow(false)
    private val _sortBy = MutableStateFlow(InventorySortOption.NAME_ASC)
    private val _isGridView = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _lastDeletedProduct = MutableStateFlow<InventoryItemEntity?>(null)

    val uiState: StateFlow<InventoryUiState> = combine(
        inventoryRepository.allItems,
        inventoryRepository.archivedItems,
        categoryRepository.allCategories,
        _searchQuery,
        _selectedCategory,
        _filterLowStockOnly,
        _filterOutOfStockOnly,
        _filterArchivedOnly,
        _sortBy,
        _isGridView,
        _userMessage
    ) { arrayOfValues ->
        val activeItemsList = arrayOfValues[0] as List<InventoryItemEntity>
        val archivedItemsList = arrayOfValues[1] as List<InventoryItemEntity>
        val categoriesList = arrayOfValues[2] as List<CategoryEntity>
        val query = arrayOfValues[3] as String
        val catFilter = arrayOfValues[4] as String?
        val lowStockOnly = arrayOfValues[5] as Boolean
        val outOfStockOnly = arrayOfValues[6] as Boolean
        val showArchivedOnly = arrayOfValues[7] as Boolean
        val sort = arrayOfValues[8] as InventorySortOption
        val grid = arrayOfValues[9] as Boolean
        val msg = arrayOfValues[10] as String?

        val currency = settingsRepository.getCurrencySymbol()

        // Compute metrics from active items
        val totalProducts = activeItemsList.size
        val lowStock = activeItemsList.count { it.stockQuantity in 1..it.minStockThreshold }
        val outOfStock = activeItemsList.count { it.stockQuantity <= 0 }
        val totalValue = activeItemsList.sumOf { it.stockQuantity * it.unitPrice }

        // Source list based on archive filter
        val sourceList = if (showArchivedOnly) archivedItemsList else activeItemsList

        // Filter
        val filtered = sourceList.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.sku.contains(query, ignoreCase = true) ||
                    item.barcode.contains(query, ignoreCase = true) ||
                    item.brand.contains(query, ignoreCase = true)

            val matchesCategory = catFilter == null || item.category.equals(catFilter, ignoreCase = true)
            val matchesLowStock = !lowStockOnly || item.stockQuantity <= item.minStockThreshold
            val matchesOutOfStock = !outOfStockOnly || item.stockQuantity <= 0

            matchesQuery && matchesCategory && matchesLowStock && matchesOutOfStock
        }

        // Sort
        val sorted = when (sort) {
            InventorySortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            InventorySortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            InventorySortOption.NEWEST -> filtered.sortedByDescending { it.createdDate }
            InventorySortOption.OLDEST -> filtered.sortedBy { it.createdDate }
            InventorySortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.unitPrice }
            InventorySortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.unitPrice }
            InventorySortOption.STOCK_HIGH_LOW -> filtered.sortedByDescending { it.stockQuantity }
            InventorySortOption.STOCK_LOW_HIGH -> filtered.sortedBy { it.stockQuantity }
        }

        InventoryUiState(
            products = sorted,
            categories = categoriesList,
            searchQuery = query,
            selectedCategory = catFilter,
            filterLowStockOnly = lowStockOnly,
            filterOutOfStockOnly = outOfStockOnly,
            filterArchivedOnly = showArchivedOnly,
            sortBy = sort,
            isGridView = grid,
            currencySymbol = currency,
            totalProductsCount = totalProducts,
            lowStockCount = lowStock,
            outOfStockCount = outOfStock,
            totalInventoryValue = totalValue,
            userMessage = msg,
            lastDeletedProduct = _lastDeletedProduct.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InventoryUiState(isLoading = true)
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setFilterLowStockOnly(enabled: Boolean) {
        _filterLowStockOnly.value = enabled
    }

    fun setFilterOutOfStockOnly(enabled: Boolean) {
        _filterOutOfStockOnly.value = enabled
    }

    fun setFilterArchivedOnly(enabled: Boolean) {
        _filterArchivedOnly.value = enabled
    }

    fun setSortOption(sortOption: InventorySortOption) {
        _sortBy.value = sortOption
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _filterLowStockOnly.value = false
        _filterOutOfStockOnly.value = false
        _filterArchivedOnly.value = false
        _sortBy.value = InventorySortOption.NAME_ASC
    }

    fun saveProduct(product: InventoryItemEntity) {
        viewModelScope.launch {
            if (product.id == 0L) {
                inventoryRepository.insertItem(product)
                _userMessage.value = "Product added successfully"
            } else {
                inventoryRepository.updateItem(product)
                _userMessage.value = "Product updated successfully"
            }
        }
    }

    fun deleteProduct(product: InventoryItemEntity) {
        viewModelScope.launch {
            _lastDeletedProduct.value = product
            inventoryRepository.deleteItem(product)
            _userMessage.value = "${product.name} deleted"
        }
    }

    fun undoDelete() {
        val lastDeleted = _lastDeletedProduct.value ?: return
        viewModelScope.launch {
            inventoryRepository.insertItem(lastDeleted)
            _lastDeletedProduct.value = null
            _userMessage.value = "Restored ${lastDeleted.name}"
        }
    }

    fun duplicateProduct(product: InventoryItemEntity) {
        viewModelScope.launch {
            inventoryRepository.duplicateItem(product)
            _userMessage.value = "Duplicated ${product.name}"
        }
    }

    fun toggleArchiveProduct(product: InventoryItemEntity) {
        viewModelScope.launch {
            val newArchiveState = !product.isArchived
            inventoryRepository.setArchived(product.id, newArchiveState)
            _userMessage.value = if (newArchiveState) "Archived ${product.name}" else "Restored ${product.name}"
        }
    }

    fun addCategory(name: String, colorHex: String) {
        viewModelScope.launch {
            categoryRepository.insertCategory(CategoryEntity(name = name, colorHex = colorHex))
            _userMessage.value = "Category '$name' created"
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
            _userMessage.value = "Category '${category.name}' deleted"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    class Factory(
        private val inventoryRepository: InventoryRepository,
        private val categoryRepository: CategoryRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InventoryViewModel(inventoryRepository, categoryRepository, settingsRepository) as T
        }
    }
}
