package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.core.database.entity.CategoryEntity
import com.example.core.database.entity.CustomerEntity
import com.example.core.database.entity.ExpenseEntity
import com.example.core.database.entity.IncomeEntity
import com.example.core.database.entity.InventoryItemEntity
import com.example.core.database.entity.InvoiceEntity
import com.example.core.database.entity.PurchaseEntity
import com.example.core.database.entity.SupplierEntity

@Dao
interface GlobalSearchDao {

    @Query("""
        SELECT * FROM inventory_items 
        WHERE name LIKE '%' || :query || '%' 
           OR sku LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%'
           OR brand LIKE '%' || :query || '%'
        ORDER BY name ASC
        LIMIT 50
    """)
    suspend fun searchProducts(query: String): List<InventoryItemEntity>

    @Query("""
        SELECT * FROM categories 
        WHERE name LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
        LIMIT 50
    """)
    suspend fun searchCategories(query: String): List<CategoryEntity>

    @Query("""
        SELECT * FROM customers 
        WHERE name LIKE '%' || :query || '%' 
           OR phone LIKE '%' || :query || '%' 
           OR company LIKE '%' || :query || '%'
           OR email LIKE '%' || :query || '%'
        ORDER BY name ASC
        LIMIT 50
    """)
    suspend fun searchCustomers(query: String): List<CustomerEntity>

    @Query("""
        SELECT * FROM suppliers 
        WHERE supplierName LIKE '%' || :query || '%' 
           OR phone LIKE '%' || :query || '%' 
           OR businessName LIKE '%' || :query || '%'
        ORDER BY supplierName ASC
        LIMIT 50
    """)
    suspend fun searchSuppliers(query: String): List<SupplierEntity>

    @Query("""
        SELECT * FROM invoices 
        WHERE invoiceNumber LIKE '%' || :query || '%' 
           OR customerName LIKE '%' || :query || '%' 
           OR customerPhone LIKE '%' || :query || '%'
        ORDER BY date DESC
        LIMIT 50
    """)
    suspend fun searchInvoices(query: String): List<InvoiceEntity>

    @Query("""
        SELECT * FROM purchases 
        WHERE purchaseNumber LIKE '%' || :query || '%' 
           OR supplierName LIKE '%' || :query || '%'
        ORDER BY purchaseDate DESC
        LIMIT 50
    """)
    suspend fun searchPurchases(query: String): List<PurchaseEntity>

    @Query("""
        SELECT * FROM expenses 
        WHERE expenseNumber LIKE '%' || :query || '%' 
           OR categoryName LIKE '%' || :query || '%' 
           OR payeeName LIKE '%' || :query || '%'
        ORDER BY expenseDate DESC
        LIMIT 50
    """)
    suspend fun searchExpenses(query: String): List<ExpenseEntity>

    @Query("""
        SELECT * FROM income 
        WHERE incomeNumber LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
           OR customerName LIKE '%' || :query || '%'
        ORDER BY incomeDate DESC
        LIMIT 50
    """)
    suspend fun searchIncome(query: String): List<IncomeEntity>
}
