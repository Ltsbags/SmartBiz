package com.example.core.database

import android.content.Context
import com.example.core.database.dao.CashBookDao
import com.example.core.database.dao.CategoryDao
import com.example.core.database.dao.CustomerDao
import com.example.core.database.dao.CustomerLedgerDao
import com.example.core.database.dao.ExpenseCategoryDao
import com.example.core.database.dao.ExpenseDao
import com.example.core.database.dao.IncomeDao
import com.example.core.database.dao.InventoryDao
import com.example.core.database.dao.InvoiceDao
import com.example.core.database.dao.PurchaseDao
import com.example.core.database.dao.SupplierDao

class DatabaseHelper(context: Context) {
    private val db = AppDatabase.getDatabase(context)

    val invoiceDao: InvoiceDao get() = db.invoiceDao()
    val inventoryDao: InventoryDao get() = db.inventoryDao()
    val customerDao: CustomerDao get() = db.customerDao()
    val customerLedgerDao: CustomerLedgerDao get() = db.customerLedgerDao()
    val categoryDao: CategoryDao get() = db.categoryDao()
    val supplierDao: SupplierDao get() = db.supplierDao()
    val purchaseDao: PurchaseDao get() = db.purchaseDao()
    val expenseCategoryDao: ExpenseCategoryDao get() = db.expenseCategoryDao()
    val expenseDao: ExpenseDao get() = db.expenseDao()
    val incomeDao: IncomeDao get() = db.incomeDao()
    val cashBookDao: CashBookDao get() = db.cashBookDao()

    companion object {
        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseHelper(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
