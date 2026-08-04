package com.example.features.invoice

enum class InvoiceSortOption(val label: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    AMOUNT_HIGH_TO_LOW("Amount: High to Low"),
    AMOUNT_LOW_TO_HIGH("Amount: Low to High")
}

data class InvoiceFilterState(
    val statusFilter: String = "ALL", // "ALL", "DRAFT", "COMPLETED", "CANCELLED"
    val paymentStatusFilter: String = "ALL", // "ALL", "PAID", "UNPAID", "PARTIAL"
    val dateFilter: String = "ALL" // "ALL", "TODAY", "THIS_WEEK", "THIS_MONTH"
)
