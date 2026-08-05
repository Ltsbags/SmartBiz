package com.example.shared.widgets

enum class SortOrder(val label: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    NAME_ASC("A - Z"),
    NAME_DESC("Z - A"),
    AMOUNT_HIGH("Highest Amount"),
    AMOUNT_LOW("Lowest Amount"),
    STOCK_HIGH("Highest Stock"),
    STOCK_LOW("Lowest Stock")
}

data class FilterCriteria(
    val startDate: Long? = null,
    val endDate: Long? = null,
    val status: String? = null,
    val category: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val isLowStockOnly: Boolean = false,
    val isActiveOnly: Boolean = true
)
