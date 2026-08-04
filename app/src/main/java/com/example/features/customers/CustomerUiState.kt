package com.example.features.customers

import com.example.core.database.entity.CustomerEntity

data class CustomerUiState(
    val customers: List<CustomerEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCustomerType: String? = null,
    val filterState: CustomerFilterState = CustomerFilterState(),
    val sortOption: CustomerSortOption = CustomerSortOption.NAME_AZ,
    val isGridView: Boolean = false,
    val totalCustomersCount: Int = 0,
    val activeCustomersCount: Int = 0,
    val totalOutstanding: Double = 0.0,
    val newCustomersThisMonthCount: Int = 0,
    val currencySymbol: String = "₹",
    val userMessage: String? = null,
    val lastDeletedCustomer: CustomerEntity? = null,
    val allCities: List<String> = emptyList(),
    val allStates: List<String> = emptyList(),
    val isLoading: Boolean = false
)
