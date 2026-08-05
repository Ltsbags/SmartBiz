package com.example.repositories

import com.example.core.database.dao.GlobalSearchDao
import com.example.core.services.SharedPreferencesService

enum class SearchResultType {
    PRODUCT,
    CATEGORY,
    CUSTOMER,
    SUPPLIER,
    INVOICE,
    PURCHASE,
    EXPENSE,
    INCOME
}

data class SearchResultItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val type: SearchResultType,
    val details: String = "",
    val entityRef: Any? = null
)

class GlobalSearchRepository(
    private val globalSearchDao: GlobalSearchDao,
    private val prefsService: SharedPreferencesService
) {

    suspend fun searchAll(query: String): List<SearchResultItem> {
        if (query.isBlank()) return emptyList()

        val results = mutableListOf<SearchResultItem>()

        val products = globalSearchDao.searchProducts(query)
        products.forEach {
            results.add(
                SearchResultItem(
                    id = it.id,
                    title = it.name,
                    subtitle = "Product • ${it.category} • SKU: ${it.sku}",
                    type = SearchResultType.PRODUCT,
                    details = "Stock: ${it.stockQuantity} ${it.unit} • Price: $${it.unitPrice}",
                    entityRef = it
                )
            )
        }

        val customers = globalSearchDao.searchCustomers(query)
        customers.forEach {
            results.add(
                SearchResultItem(
                    id = it.id,
                    title = it.name,
                    subtitle = "Customer • Phone: ${it.phone}",
                    type = SearchResultType.CUSTOMER,
                    details = "Company: ${it.company} • Balance: $${it.outstandingBalance}",
                    entityRef = it
                )
            )
        }

        val invoices = globalSearchDao.searchInvoices(query)
        invoices.forEach {
            results.add(
                SearchResultItem(
                    id = it.id,
                    title = "Invoice #${it.invoiceNumber}",
                    subtitle = "Invoice • Customer: ${it.customerName}",
                    type = SearchResultType.INVOICE,
                    details = "Total: $${it.totalAmount} • Status: ${it.paymentStatus}",
                    entityRef = it
                )
            )
        }

        val suppliers = globalSearchDao.searchSuppliers(query)
        suppliers.forEach {
            results.add(
                SearchResultItem(
                    id = it.id,
                    title = it.supplierName,
                    subtitle = "Supplier • Phone: ${it.phone}",
                    type = SearchResultType.SUPPLIER,
                    details = "Business: ${it.businessName}",
                    entityRef = it
                )
            )
        }

        val purchases = globalSearchDao.searchPurchases(query)
        purchases.forEach {
            results.add(
                SearchResultItem(
                    id = it.id,
                    title = "Purchase #${it.purchaseNumber}",
                    subtitle = "Purchase • Supplier: ${it.supplierName}",
                    type = SearchResultType.PURCHASE,
                    details = "Total: $${it.totalAmount}",
                    entityRef = it
                )
            )
        }

        val expenses = globalSearchDao.searchExpenses(query)
        expenses.forEach {
            results.add(
                SearchResultItem(
                    id = it.id,
                    title = "Expense #${it.expenseNumber}",
                    subtitle = "Expense • Category: ${it.categoryName}",
                    type = SearchResultType.EXPENSE,
                    details = "Payee: ${it.payeeName} • Amount: $${it.totalAmount}",
                    entityRef = it
                )
            )
        }

        val income = globalSearchDao.searchIncome(query)
        income.forEach {
            results.add(
                SearchResultItem(
                    id = it.id,
                    title = "Income #${it.incomeNumber}",
                    subtitle = "Income • Category: ${it.category}",
                    type = SearchResultType.INCOME,
                    details = "Customer: ${it.customerName} • Amount: $${it.amount}",
                    entityRef = it
                )
            )
        }

        val categories = globalSearchDao.searchCategories(query)
        categories.forEach {
            results.add(
                SearchResultItem(
                    id = it.id,
                    title = it.name,
                    subtitle = "Category • Product Category",
                    type = SearchResultType.CATEGORY,
                    details = it.description,
                    entityRef = it
                )
            )
        }

        return results
    }

    fun getRecentSearches(): List<String> {
        val historyStr = prefsService.getCustomString("recent_searches_history", "")
        if (historyStr.isBlank()) return emptyList()
        return historyStr.split("|||").filter { it.isNotBlank() }
    }

    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        val current = getRecentSearches().toMutableList()
        current.remove(query)
        current.add(0, query)
        val updated = current.take(10).joinToString("|||")
        prefsService.putCustomString("recent_searches_history", updated)
    }

    fun clearRecentSearches() {
        prefsService.putCustomString("recent_searches_history", "")
    }
}
