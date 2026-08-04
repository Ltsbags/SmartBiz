package com.example.features.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.CustomerEntity
import com.example.core.database.entity.CustomerLedgerEntity
import com.example.repositories.CustomerRepository
import com.example.repositories.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val customerRepository: CustomerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedType = MutableStateFlow<String?>(null)
    private val filterState = MutableStateFlow(CustomerFilterState())
    private val sortOption = MutableStateFlow(CustomerSortOption.NAME_AZ)
    private val isGridView = MutableStateFlow(false)
    private val userMessage = MutableStateFlow<String?>(null)
    private var lastDeletedCustomer: CustomerEntity? = null

    val uiState: StateFlow<CustomerUiState> = combine(
        customerRepository.allCustomers,
        customerRepository.customerCount,
        customerRepository.activeCustomerCount,
        customerRepository.totalOutstandingBalance,
        customerRepository.getNewCustomersThisMonthCount(),
        searchQuery,
        selectedType,
        filterState,
        sortOption,
        isGridView,
        userMessage
    ) { arrayOfValues ->
        @Suppress("UNCHECKED_CAST")
        val allCust = arrayOfValues[0] as List<CustomerEntity>
        val totalCount = arrayOfValues[1] as Int
        val activeCount = arrayOfValues[2] as Int
        val outstandingSum = arrayOfValues[3] as Double? ?: 0.0
        val newThisMonth = arrayOfValues[4] as Int
        val query = arrayOfValues[5] as String
        val typeChip = arrayOfValues[6] as String?
        val filters = arrayOfValues[7] as CustomerFilterState
        val sort = arrayOfValues[8] as CustomerSortOption
        val grid = arrayOfValues[9] as Boolean
        val msg = arrayOfValues[10] as String?

        // Distinct cities and states for filtering
        val cities = allCust.map { it.city }.filter { it.isNotBlank() }.distinct().sorted()
        val states = allCust.map { it.state }.filter { it.isNotBlank() }.distinct().sorted()

        // Filter logic
        val filteredList = allCust.filter { cust ->
            // Search match (Name, Company/Business Name, Phone, GST, Code)
            val matchSearch = query.isBlank() ||
                    cust.name.contains(query, ignoreCase = true) ||
                    cust.company.contains(query, ignoreCase = true) ||
                    cust.phone.contains(query, ignoreCase = true) ||
                    cust.gstNumber.contains(query, ignoreCase = true) ||
                    cust.customerCode.contains(query, ignoreCase = true)

            // Type Chip filter
            val matchTypeChip = typeChip == null || cust.customerType.equals(typeChip, ignoreCase = true)

            // Sheet Filter: Type
            val matchFilterType = filters.customerType == null || cust.customerType.equals(filters.customerType, ignoreCase = true)

            // Sheet Filter: Outstanding balance
            val matchOutstanding = !filters.hasOutstandingOnly || cust.outstandingBalance > 0.0

            // Sheet Filter: Active vs Archived
            val matchArchived = if (filters.showArchivedOnly) cust.isArchived else !cust.isArchived

            // Sheet Filter: City
            val matchCity = filters.selectedCity == null || cust.city.equals(filters.selectedCity, ignoreCase = true)

            // Sheet Filter: State
            val matchState = filters.selectedState == null || cust.state.equals(filters.selectedState, ignoreCase = true)

            matchSearch && matchTypeChip && matchFilterType && matchOutstanding && matchArchived && matchCity && matchState
        }

        // Sort logic
        val sortedList = when (sort) {
            CustomerSortOption.NAME_AZ -> filteredList.sortedBy { it.name.lowercase() }
            CustomerSortOption.NAME_ZA -> filteredList.sortedByDescending { it.name.lowercase() }
            CustomerSortOption.NEWEST -> filteredList.sortedByDescending { it.createdDate }
            CustomerSortOption.OLDEST -> filteredList.sortedBy { it.createdDate }
            CustomerSortOption.HIGHEST_OUTSTANDING -> filteredList.sortedByDescending { it.outstandingBalance }
            CustomerSortOption.LOWEST_OUTSTANDING -> filteredList.sortedBy { it.outstandingBalance }
        }

        CustomerUiState(
            customers = sortedList,
            searchQuery = query,
            selectedCustomerType = typeChip,
            filterState = filters,
            sortOption = sort,
            isGridView = grid,
            totalCustomersCount = totalCount,
            activeCustomersCount = activeCount,
            totalOutstanding = outstandingSum,
            newCustomersThisMonthCount = newThisMonth,
            currencySymbol = settingsRepository.getCurrencySymbol(),
            userMessage = msg,
            lastDeletedCustomer = lastDeletedCustomer,
            allCities = cities,
            allStates = states,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CustomerUiState(isLoading = true)
    )

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSelectedTypeChip(type: String?) {
        selectedType.value = if (selectedType.value == type) null else type
    }

    fun setFilterState(newFilters: CustomerFilterState) {
        filterState.value = newFilters
    }

    fun resetFilters() {
        filterState.value = CustomerFilterState()
        selectedType.value = null
        searchQuery.value = ""
    }

    fun setSortOption(option: CustomerSortOption) {
        sortOption.value = option
    }

    fun toggleViewMode() {
        isGridView.value = !isGridView.value
    }

    fun clearUserMessage() {
        userMessage.value = null
    }

    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                customerRepository.insertCustomer(customer)
                userMessage.value = "Customer '${customer.name}' created successfully"
            } else {
                customerRepository.updateCustomer(customer)
                userMessage.value = "Customer '${customer.name}' updated successfully"
            }
        }
    }

    fun toggleArchiveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            customerRepository.toggleArchiveStatus(customer)
            val actionText = if (customer.isArchived) "restored" else "archived"
            userMessage.value = "Customer '${customer.name}' $actionText"
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            lastDeletedCustomer = customer
            customerRepository.deleteCustomer(customer)
            userMessage.value = "Customer '${customer.name}' deleted"
        }
    }

    fun undoDelete() {
        val customerToRestore = lastDeletedCustomer ?: return
        viewModelScope.launch {
            customerRepository.insertCustomer(customerToRestore)
            userMessage.value = "Customer '${customerToRestore.name}' restored"
            lastDeletedCustomer = null
        }
    }

    fun getLedgerForCustomer(customerId: Long): Flow<List<CustomerLedgerEntity>> {
        return customerRepository.getLedgerForCustomer(customerId)
    }

    class Factory(
        private val customerRepository: CustomerRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CustomerViewModel(customerRepository, settingsRepository) as T
        }
    }
}
