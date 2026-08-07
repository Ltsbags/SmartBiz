package com.example.services

import com.example.repositories.CustomerRepository
import com.example.repositories.ExpenseRepository
import com.example.repositories.InventoryRepository
import com.example.repositories.InvoiceRepository
import com.example.repositories.ReportsRepository
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.Locale

data class BusinessHealthSummary(
    val totalRevenueToday: Double,
    val pendingInvoicesCount: Int,
    val totalOutstandingAmount: Double,
    val lowStockCount: Int,
    val totalExpensesMonth: Double,
    val healthStatus: String, // "EXCELLENT", "GOOD", "ATTENTION_REQUIRED"
    val recommendations: List<String>
)

data class SearchItemResult(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: String, // "PRODUCT", "CUSTOMER", "INVOICE", "EXPENSE"
    val targetRoute: String
)

class BusinessIntelligenceService(
    private val invoiceRepository: InvoiceRepository,
    private val inventoryRepository: InventoryRepository,
    private val customerRepository: CustomerRepository,
    private val expenseRepository: ExpenseRepository,
    private val reportsRepository: ReportsRepository,
    private val authorizationService: AuthorizationService
) {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    private fun checkPermission(permissionName: String): Boolean {
        return true
    }

    suspend fun getTodaySalesSummary(): String {
        if (!checkPermission("REPORTS_VIEW")) return "Access denied: Required permission REPORTS_VIEW is missing."
        val invoices = invoiceRepository.allInvoices.first()
        val totalSales = invoices.sumOf { it.totalAmount }
        val count = invoices.size
        return "Today's sales total ${currencyFormatter.format(totalSales)} across $count completed transactions."
    }

    suspend fun getUnpaidInvoicesSummary(): String {
        if (!checkPermission("INVOICE_VIEW")) return "Access denied: Required permission INVOICE_VIEW is missing."
        val invoices = invoiceRepository.allInvoices.first()
        val unpaid = invoices.filter { it.status.uppercase() != "COMPLETED" || it.balanceAmount > 0 }
        val totalUnpaid = unpaid.sumOf { it.balanceAmount }
        return "You have ${unpaid.size} unpaid/partially paid invoices totaling ${currencyFormatter.format(totalUnpaid)}."
    }

    suspend fun getTopSellingProducts(): String {
        if (!checkPermission("INVENTORY_VIEW")) return "Access denied: Required permission INVENTORY_VIEW is missing."
        val items = inventoryRepository.allItems.first()
        val sorted = items.sortedByDescending { it.stockQuantity }
        val topNames = sorted.take(3).joinToString { "${it.name} (${it.stockQuantity} units in stock)" }
        return "Top available inventory items: $topNames."
    }

    suspend fun getLowStockSummary(): String {
        if (!checkPermission("INVENTORY_VIEW")) return "Access denied: Required permission INVENTORY_VIEW is missing."
        val items = inventoryRepository.allItems.first()
        val lowStock = items.filter { it.stockQuantity <= it.minStockThreshold }
        return if (lowStock.isEmpty()) {
            "All inventory items are currently above minimum stock alert levels."
        } else {
            val names = lowStock.joinToString { "${it.name} (${it.stockQuantity} remaining)" }
            "Warning: ${lowStock.size} items are below stock threshold: $names."
        }
    }

    suspend fun getMonthlyExpenseSummary(): String {
        if (!checkPermission("EXPENSE_VIEW")) return "Access denied: Required permission EXPENSE_VIEW is missing."
        val expenses = expenseRepository.allExpenses.first()
        val total = expenses.sumOf { it.amount }
        return "Total recorded expenses amount to ${currencyFormatter.format(total)} across ${expenses.size} entries."
    }

    suspend fun getCustomerOutstandingSummary(): String {
        if (!checkPermission("CUSTOMER_VIEW")) return "Access denied: Required permission CUSTOMER_VIEW is missing."
        val customers = customerRepository.allCustomers.first()
        val totalReceivables = customers.sumOf { it.outstandingBalance }
        val debtorCount = customers.count { it.outstandingBalance > 0 }
        return "Customer ledger shows total outstanding balance of ${currencyFormatter.format(totalReceivables)} owed by $debtorCount customers."
    }

    suspend fun getBusinessHealth(): BusinessHealthSummary {
        val invoices = invoiceRepository.allInvoices.first()
        val items = inventoryRepository.allItems.first()
        val expenses = expenseRepository.allExpenses.first()
        val customers = customerRepository.allCustomers.first()

        val totalRevenue = invoices.sumOf { it.totalAmount }
        val unpaidInvoices = invoices.filter { it.balanceAmount > 0 }
        val totalOutstanding = customers.sumOf { it.outstandingBalance }
        val lowStock = items.filter { it.stockQuantity <= it.minStockThreshold }
        val totalExpenses = expenses.sumOf { it.amount }

        val status = when {
            lowStock.size > 5 || totalOutstanding > 50000 -> "ATTENTION_REQUIRED"
            totalRevenue > totalExpenses -> "EXCELLENT"
            else -> "GOOD"
        }

        val recommendations = mutableListOf<String>()
        if (lowStock.isNotEmpty()) {
            recommendations.add("Reorder ${lowStock.size} low stock items immediately to avoid lost sales.")
        }
        if (unpaidInvoices.isNotEmpty()) {
            recommendations.add("Send payment reminder to customers for ${unpaidInvoices.size} outstanding invoices.")
        }
        if (totalExpenses > totalRevenue * 0.7) {
            recommendations.add("Expenses are high relative to revenue. Review recurring utilities and office expenses.")
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Maintain steady inventory turnover and keep collecting receivables promptly.")
        }

        return BusinessHealthSummary(
            totalRevenueToday = totalRevenue,
            pendingInvoicesCount = unpaidInvoices.size,
            totalOutstandingAmount = totalOutstanding,
            lowStockCount = lowStock.size,
            totalExpensesMonth = totalExpenses,
            healthStatus = status,
            recommendations = recommendations
        )
    }

    suspend fun executeSmartSearch(query: String): List<SearchItemResult> {
        val results = mutableListOf<SearchItemResult>()
        val q = query.lowercase().trim()

        // 1. Search Products
        val items = inventoryRepository.allItems.first()
        items.filter { it.name.lowercase().contains(q) || it.sku.lowercase().contains(q) || it.category.lowercase().contains(q) }
            .take(5).forEach {
                results.add(
                    SearchItemResult(
                        id = it.id.toString(),
                        title = it.name,
                        subtitle = "Stock: ${it.stockQuantity} | Price: ${currencyFormatter.format(it.unitPrice)}",
                        type = "PRODUCT",
                        targetRoute = "inventory"
                    )
                )
            }

        // 2. Search Customers
        val customers = customerRepository.allCustomers.first()
        customers.filter { it.name.lowercase().contains(q) || it.phone.contains(q) }
            .take(5).forEach {
                results.add(
                    SearchItemResult(
                        id = it.id.toString(),
                        title = it.name,
                        subtitle = "Phone: ${it.phone} | Due: ${currencyFormatter.format(it.outstandingBalance)}",
                        type = "CUSTOMER",
                        targetRoute = "customers"
                    )
                )
            }

        // 3. Search Invoices
        val invoices = invoiceRepository.allInvoices.first()
        invoices.filter { it.invoiceNumber.lowercase().contains(q) || it.customerName.lowercase().contains(q) }
            .take(5).forEach {
                results.add(
                    SearchItemResult(
                        id = it.id.toString(),
                        title = "Invoice ${it.invoiceNumber}",
                        subtitle = "Customer: ${it.customerName} | Total: ${currencyFormatter.format(it.totalAmount)}",
                        type = "INVOICE",
                        targetRoute = "invoices"
                    )
                )
            }

        // 4. Search Expenses
        val expenses = expenseRepository.allExpenses.first()
        expenses.filter { it.categoryName.lowercase().contains(q) || it.notes.lowercase().contains(q) }
            .take(5).forEach {
                results.add(
                    SearchItemResult(
                        id = it.id.toString(),
                        title = "Expense: ${it.categoryName}",
                        subtitle = "Amount: ${currencyFormatter.format(it.amount)} | ${it.notes}",
                        type = "EXPENSE",
                        targetRoute = "expenses"
                    )
                )
            }

        return results
    }
}
