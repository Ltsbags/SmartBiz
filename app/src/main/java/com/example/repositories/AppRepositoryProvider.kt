package com.example.repositories

import android.content.Context
import com.example.core.database.DatabaseHelper
import com.example.core.services.SharedPreferencesService

class AppRepositoryProvider private constructor(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)
    private val prefsService = SharedPreferencesService.getInstance(context)

    val invoiceRepository: InvoiceRepository = InvoiceRepository(
        dbHelper.invoiceDao,
        dbHelper.inventoryDao,
        dbHelper.customerDao,
        dbHelper.customerLedgerDao
    )
    val inventoryRepository: InventoryRepository = InventoryRepository(dbHelper.inventoryDao)
    val customerRepository: CustomerRepository = CustomerRepository(dbHelper.customerDao, dbHelper.customerLedgerDao)
    val categoryRepository: CategoryRepository = CategoryRepository(dbHelper.categoryDao)
    val settingsRepository: SettingsRepository = SettingsRepository(prefsService)
    val supplierRepository: SupplierRepository = SupplierRepository(dbHelper.supplierDao, dbHelper.purchaseDao)
    val purchaseRepository: PurchaseRepository = PurchaseRepository(dbHelper.purchaseDao, dbHelper.inventoryDao, dbHelper.supplierDao)
    val expenseCategoryRepository: ExpenseCategoryRepository = ExpenseCategoryRepository(dbHelper.expenseCategoryDao)
    val expenseRepository: ExpenseRepository = ExpenseRepository(dbHelper.expenseDao, dbHelper.cashBookDao)
    val incomeRepository: IncomeRepository = IncomeRepository(dbHelper.incomeDao, dbHelper.cashBookDao)
    val cashBookRepository: CashBookRepository = CashBookRepository(dbHelper.cashBookDao)

    companion object {
        @Volatile
        private var INSTANCE: AppRepositoryProvider? = null

        fun initialize(context: Context): AppRepositoryProvider {
            return INSTANCE ?: synchronized(this) {
                val instance = AppRepositoryProvider(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        fun getInstance(): AppRepositoryProvider {
            return INSTANCE ?: throw IllegalStateException("AppRepositoryProvider must be initialized first")
        }
    }
}
