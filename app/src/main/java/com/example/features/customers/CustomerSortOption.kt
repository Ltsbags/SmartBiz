package com.example.features.customers

enum class CustomerSortOption(val displayName: String) {
    NAME_AZ("Name (A to Z)"),
    NAME_ZA("Name (Z to A)"),
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    HIGHEST_OUTSTANDING("Highest Outstanding"),
    LOWEST_OUTSTANDING("Lowest Outstanding")
}
