package com.example.features.customers

data class CustomerFilterState(
    val customerType: String? = null,
    val hasOutstandingOnly: Boolean = false,
    val showArchivedOnly: Boolean = false,
    val selectedCity: String? = null,
    val selectedState: String? = null
) {
    val isFilteringActive: Boolean
        get() = customerType != null || hasOutstandingOnly || showArchivedOnly || selectedCity != null || selectedState != null
}
