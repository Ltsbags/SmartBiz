package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.constants.AppConstants
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
import com.example.core.database.entity.CashBookEntryEntity
import com.example.core.database.entity.CategoryEntity
import com.example.core.database.entity.CustomerEntity
import com.example.core.database.entity.CustomerLedgerEntity
import com.example.core.database.entity.ExpenseCategoryEntity
import com.example.core.database.entity.ExpenseEntity
import com.example.core.database.entity.IncomeEntity
import com.example.core.database.entity.InventoryItemEntity
import com.example.core.database.entity.InvoiceEntity
import com.example.core.database.entity.InvoiceItemEntity
import com.example.core.database.entity.PurchaseEntity
import com.example.core.database.entity.PurchaseItemEntity
import com.example.core.database.entity.SupplierEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        InventoryItemEntity::class,
        CustomerEntity::class,
        CustomerLedgerEntity::class,
        CategoryEntity::class,
        SupplierEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        ExpenseCategoryEntity::class,
        ExpenseEntity::class,
        IncomeEntity::class,
        CashBookEntryEntity::class
    ],
    version = AppConstants.DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun invoiceDao(): InvoiceDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerLedgerDao(): CustomerLedgerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun cashBookDao(): CashBookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    AppConstants.DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(true)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }
        }

        private suspend fun seedDatabase(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val dayMs = 86400000L

            // Seed Categories
            db.categoryDao().insertCategory(
                CategoryEntity(
                    name = "Hardware",
                    colorHex = "#2196F3",
                    iconName = "Computer",
                    description = "POS Equipment and Scanners"
                )
            )
            db.categoryDao().insertCategory(
                CategoryEntity(
                    name = "Supplies",
                    colorHex = "#4CAF50",
                    iconName = "LocalOffer",
                    description = "Paper rolls, labels and inks"
                )
            )
            db.categoryDao().insertCategory(
                CategoryEntity(
                    name = "Accessories",
                    colorHex = "#FF9800",
                    iconName = "Category",
                    description = "Drawers, stands and cables"
                )
            )

            // Seed Customers
            val custId1 = db.customerDao().insertCustomer(
                CustomerEntity(
                    customerCode = "CUST-1001",
                    name = "Acme Global Solutions",
                    company = "Acme Corp",
                    phone = "+91 98765 43210",
                    alternateNumber = "+91 98765 43211",
                    email = "billing@acmeglobal.com",
                    gstNumber = "27AAAAA0000A1Z5",
                    panNumber = "AAAAA0000A",
                    billingAddress = "Suite 404, Tech Park, Powai",
                    shippingAddress = "Warehouse 2, MIDC Industrial Area",
                    city = "Mumbai",
                    state = "Maharashtra",
                    pincode = "400076",
                    customerType = "Wholesale",
                    openingBalance = 500.0,
                    totalPurchases = 12450.00,
                    outstandingBalance = 1250.00,
                    creditLimit = 50000.0,
                    paymentTermsDays = 30,
                    notes = "Preferred customer. Always requires GST Tax Invoices.",
                    tags = "VIP, Wholesale, High Volume",
                    createdDate = now - (30 * dayMs)
                )
            )

            db.customerLedgerDao().insertLedger(
                CustomerLedgerEntity(
                    customerId = custId1,
                    transactionType = "OPENING_BALANCE",
                    referenceNumber = "OB-1001",
                    amount = 500.0,
                    balanceAfter = 500.0,
                    description = "Opening balance recorded",
                    transactionDate = now - (30 * dayMs)
                )
            )

            db.customerLedgerDao().insertLedger(
                CustomerLedgerEntity(
                    customerId = custId1,
                    transactionType = "INVOICE",
                    referenceNumber = "INV-2026-001",
                    amount = 750.0,
                    balanceAfter = 1250.0,
                    description = "POS Equipment Order",
                    transactionDate = now - (2 * dayMs)
                )
            )

            val custId2 = db.customerDao().insertCustomer(
                CustomerEntity(
                    customerCode = "CUST-1002",
                    name = "Apex Retail Traders",
                    company = "Apex LLC",
                    phone = "+91 98123 45678",
                    email = "orders@apexretail.com",
                    gstNumber = "07BBBBB1111B2Z3",
                    panNumber = "BBBBB1111B",
                    billingAddress = "Plot 12, Connaught Place",
                    city = "New Delhi",
                    state = "Delhi",
                    pincode = "110001",
                    customerType = "Retail",
                    openingBalance = 0.0,
                    totalPurchases = 8320.00,
                    outstandingBalance = 0.0,
                    creditLimit = 25000.0,
                    paymentTermsDays = 15,
                    notes = "Prompt payer via UPI / NEFT.",
                    tags = "Regular, Retail",
                    createdDate = now - (15 * dayMs)
                )
            )

            val custId3 = db.customerDao().insertCustomer(
                CustomerEntity(
                    customerCode = "CUST-1003",
                    name = "Vanguard Electronics",
                    company = "Vanguard Pvt Ltd",
                    phone = "+91 91234 56789",
                    email = "contact@vanguard.in",
                    gstNumber = "29CCCCC2222C3Z1",
                    billingAddress = "45 MG Road, Indiranagar",
                    city = "Bengaluru",
                    state = "Karnataka",
                    pincode = "560038",
                    customerType = "Distributor",
                    openingBalance = 3200.0,
                    totalPurchases = 24500.00,
                    outstandingBalance = 3200.00,
                    creditLimit = 100000.0,
                    paymentTermsDays = 45,
                    notes = "Regional distributor for South India territory.",
                    tags = "Distributor, Bulk",
                    createdDate = now - (5 * dayMs)
                )
            )

            db.customerLedgerDao().insertLedger(
                CustomerLedgerEntity(
                    customerId = custId3,
                    transactionType = "OPENING_BALANCE",
                    referenceNumber = "OB-1003",
                    amount = 3200.0,
                    balanceAfter = 3200.0,
                    description = "Opening balance imported from legacy system",
                    transactionDate = now - (5 * dayMs)
                )
            )

            // Seed Inventory Items
            db.inventoryDao().insertItem(
                InventoryItemEntity(
                    name = "Wireless Barcode Scanner Pro",
                    sku = "SKU-BCS-901",
                    barcode = "8901234567890",
                    category = "Hardware",
                    brand = "ScanTech",
                    description = "High-speed 2D omnidirectional wireless laser scanner.",
                    unit = "pcs",
                    purchasePrice = 85.00,
                    costPrice = 85.00,
                    unitPrice = 149.99,
                    gstPercentage = 18.0,
                    openingStock = 30,
                    stockQuantity = 24,
                    minStockThreshold = 5,
                    maxStock = 100,
                    location = "Shelf A-1",
                    createdDate = now - (20 * dayMs),
                    updatedDate = now
                )
            )

            db.inventoryDao().insertItem(
                InventoryItemEntity(
                    name = "Thermal Receipt Roll (80mm)",
                    sku = "SKU-TRR-80M",
                    barcode = "8901234567891",
                    category = "Supplies",
                    brand = "PaperMax",
                    description = "BPA-free high-density thermal receipt paper 50 rolls/box.",
                    unit = "box",
                    purchasePrice = 12.00,
                    costPrice = 12.00,
                    unitPrice = 29.50,
                    gstPercentage = 12.0,
                    openingStock = 20,
                    stockQuantity = 3, // Low stock demo
                    minStockThreshold = 10,
                    maxStock = 50,
                    location = "Shelf B-4",
                    createdDate = now - (18 * dayMs),
                    updatedDate = now
                )
            )

            db.inventoryDao().insertItem(
                InventoryItemEntity(
                    name = "Smart POS Terminal V2",
                    sku = "SKU-POS-V20",
                    barcode = "8901234567892",
                    category = "Hardware",
                    brand = "SmartBiz",
                    description = "Dual-screen Android POS with integrated receipt printer & NFC.",
                    unit = "pcs",
                    purchasePrice = 260.00,
                    costPrice = 260.00,
                    unitPrice = 399.00,
                    gstPercentage = 18.0,
                    openingStock = 15,
                    stockQuantity = 12,
                    minStockThreshold = 3,
                    maxStock = 30,
                    location = "Counter 1",
                    createdDate = now - (15 * dayMs),
                    updatedDate = now
                )
            )

            db.inventoryDao().insertItem(
                InventoryItemEntity(
                    name = "Heavy Duty Cash Drawer",
                    sku = "SKU-CDR-100",
                    barcode = "8901234567893",
                    category = "Accessories",
                    brand = "SafeLock",
                    description = "Steel construct cash drawer with 5 bill & 8 coin removable tray.",
                    unit = "pcs",
                    purchasePrice = 45.00,
                    costPrice = 45.00,
                    unitPrice = 89.00,
                    gstPercentage = 18.0,
                    openingStock = 10,
                    stockQuantity = 2, // Low stock demo
                    minStockThreshold = 4,
                    maxStock = 25,
                    location = "Shelf C-2",
                    createdDate = now - (10 * dayMs),
                    updatedDate = now
                )
            )

            db.inventoryDao().insertItem(
                InventoryItemEntity(
                    name = "Bluetooth Label Printer",
                    sku = "SKU-BLP-500",
                    barcode = "8901234567894",
                    category = "Hardware",
                    brand = "PrintMaster",
                    description = "Portable direct thermal barcode and sticker label printer.",
                    unit = "pcs",
                    purchasePrice = 65.00,
                    costPrice = 65.00,
                    unitPrice = 119.00,
                    gstPercentage = 18.0,
                    openingStock = 5,
                    stockQuantity = 0, // Out of stock demo!
                    minStockThreshold = 3,
                    maxStock = 20,
                    location = "Shelf A-3",
                    createdDate = now - (5 * dayMs),
                    updatedDate = now
                )
            )

            // Seed Invoices & Invoice Items
            val inv1Id = db.invoiceDao().insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2026-001",
                    date = now - (2 * dayMs),
                    dueDate = now + (13 * dayMs),
                    customerId = custId1,
                    customerName = "Acme Global Solutions",
                    customerPhone = "+91 98765 43210",
                    customerGst = "27AAAAA0000A1Z5",
                    billingAddress = "Suite 404, Tech Park, Powai, Mumbai",
                    status = "COMPLETED",
                    paymentStatus = "UNPAID",
                    subtotal = 1100.00,
                    discountType = "FLAT",
                    discountValue = 0.0,
                    discountAmount = 0.0,
                    taxAmount = 150.00,
                    roundOff = 0.0,
                    totalAmount = 1250.00,
                    paidAmount = 0.0,
                    balanceAmount = 1250.00,
                    itemsCount = 2,
                    notes = "POS Hardware Installation order",
                    terms = "Payment due in 15 days."
                )
            )

            db.invoiceDao().insertInvoiceItems(
                listOf(
                    InvoiceItemEntity(
                        invoiceId = inv1Id,
                        productId = 1,
                        productName = "Wireless Barcode Scanner Pro",
                        sku = "SKU-BCS-901",
                        quantity = 2.0,
                        unit = "pcs",
                        sellingPrice = 149.99,
                        discount = 0.0,
                        gstPercentage = 18.0,
                        taxAmount = 53.99,
                        lineTotal = 353.97
                    ),
                    InvoiceItemEntity(
                        invoiceId = inv1Id,
                        productId = 3,
                        productName = "Smart POS Terminal V2",
                        sku = "SKU-POS-V20",
                        quantity = 2.0,
                        unit = "pcs",
                        sellingPrice = 399.00,
                        discount = 0.0,
                        gstPercentage = 18.0,
                        taxAmount = 143.64,
                        lineTotal = 941.64
                    )
                )
            )

            val inv2Id = db.invoiceDao().insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2026-002",
                    date = now - (5 * dayMs),
                    dueDate = now + (10 * dayMs),
                    customerId = custId2,
                    customerName = "Apex Retail Traders",
                    customerPhone = "+91 98123 45678",
                    customerGst = "07BBBBB1111B2Z3",
                    billingAddress = "Plot 12, Connaught Place, New Delhi",
                    status = "COMPLETED",
                    paymentStatus = "PAID",
                    subtotal = 743.30,
                    discountType = "PERCENTAGE",
                    discountValue = 5.0,
                    discountAmount = 37.16,
                    taxAmount = 126.36,
                    roundOff = 0.0,
                    totalAmount = 832.50,
                    paidAmount = 832.50,
                    balanceAmount = 0.0,
                    itemsCount = 1,
                    notes = "Paid in full via NEFT",
                    terms = "Thank you for your business!"
                )
            )

            db.invoiceDao().insertInvoiceItems(
                listOf(
                    InvoiceItemEntity(
                        invoiceId = inv2Id,
                        productId = 2,
                        productName = "Thermal Receipt Roll (80mm)",
                        sku = "SKU-TRR-80M",
                        quantity = 25.0,
                        unit = "box",
                        sellingPrice = 29.50,
                        discount = 0.0,
                        gstPercentage = 12.0,
                        taxAmount = 88.50,
                        lineTotal = 826.00
                    )
                )
            )

            // Seed Suppliers
            val suppId1 = db.supplierDao().insertSupplier(
                SupplierEntity(
                    supplierCode = "SUP-1001",
                    supplierName = "ScanTech Electronics Corp",
                    businessName = "ScanTech Global",
                    phone = "+1 (800) 555-0199",
                    email = "supply@scantech.com",
                    gstNumber = "27AAACS1234F1Z9",
                    billingAddress = "450 Industrial Parkway, Building B",
                    city = "San Jose",
                    state = "California",
                    pincode = "95110",
                    openingBalance = 0.0,
                    outstandingBalance = 850.00,
                    paymentTerms = "Net 30",
                    notes = "Primary vendor for barcode scanners & readers."
                )
            )

            val suppId2 = db.supplierDao().insertSupplier(
                SupplierEntity(
                    supplierCode = "SUP-1002",
                    supplierName = "PaperMax Supplies Ltd",
                    businessName = "PaperMax Distribution",
                    phone = "+1 (800) 555-0244",
                    email = "orders@papermax.com",
                    gstNumber = "07AAACP9876E1Z2",
                    billingAddress = "12 Warehouse Way",
                    city = "Chicago",
                    state = "Illinois",
                    pincode = "60601",
                    openingBalance = 0.0,
                    outstandingBalance = 0.0,
                    paymentTerms = "Net 15",
                    notes = "Bulk thermal paper roll manufacturer."
                )
            )

            // Seed Purchase Orders
            val po1Id = db.purchaseDao().insertPurchase(
                PurchaseEntity(
                    purchaseNumber = "PO-2026-001",
                    supplierId = suppId1,
                    supplierName = "ScanTech Electronics Corp",
                    supplierPhone = "+1 (800) 555-0199",
                    purchaseDate = now - (10 * dayMs),
                    status = "RECEIVED",
                    paymentStatus = "PARTIAL",
                    subtotal = 1700.00,
                    discount = 0.0,
                    taxAmount = 306.00,
                    totalAmount = 2006.00,
                    paidAmount = 1156.00,
                    balanceAmount = 850.00,
                    itemsCount = 1,
                    notes = "Stock inward for Wireless Barcode Scanners"
                )
            )

            db.purchaseDao().insertPurchaseItems(
                listOf(
                    PurchaseItemEntity(
                        purchaseId = po1Id,
                        productId = 1,
                        productName = "Wireless Barcode Scanner Pro",
                        sku = "SKU-BCS-901",
                        quantity = 20.0,
                        unit = "pcs",
                        purchasePrice = 85.00,
                        taxPercentage = 18.0,
                        lineTotal = 2006.00
                    )
                )
            )

            val po2Id = db.purchaseDao().insertPurchase(
                PurchaseEntity(
                    purchaseNumber = "PO-2026-002",
                    supplierId = suppId2,
                    supplierName = "PaperMax Supplies Ltd",
                    supplierPhone = "+1 (800) 555-0244",
                    purchaseDate = now - (2 * dayMs),
                    status = "ORDERED",
                    paymentStatus = "UNPAID",
                    subtotal = 600.00,
                    discount = 0.0,
                    taxAmount = 72.00,
                    totalAmount = 672.00,
                    paidAmount = 0.0,
                    balanceAmount = 672.00,
                    itemsCount = 1,
                    notes = "Urgent reorder for Thermal Receipt Rolls"
                )
            )

            db.purchaseDao().insertPurchaseItems(
                listOf(
                    PurchaseItemEntity(
                        purchaseId = po2Id,
                        productId = 2,
                        productName = "Thermal Receipt Roll (80mm)",
                        sku = "SKU-TRR-80M",
                        quantity = 50.0,
                        unit = "box",
                        purchasePrice = 12.00,
                        taxPercentage = 12.0,
                        lineTotal = 672.00
                    )
                )
            )

            // Seed Expense Categories
            val catRent = db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Rent & Lease", colorHex = "#E91E63", iconName = "Business", description = "Storefront and warehouse lease payments", isSystemDefault = true))
            val catUtil = db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Utilities & Power", colorHex = "#FF9800", iconName = "Bolt", description = "Electricity, water, internet and phones", isSystemDefault = true))
            val catSal = db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Salaries & Payroll", colorHex = "#4CAF50", iconName = "People", description = "Employee compensation and bonuses", isSystemDefault = true))
            val catMaint = db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Maintenance & Repairs", colorHex = "#00BCD4", iconName = "Build", description = "Equipment servicing and store upkeep", isSystemDefault = true))
            val catMisc = db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Miscellaneous", colorHex = "#9C27B0", iconName = "MoreHoriz", description = "General operational overheads", isSystemDefault = true))

            // Seed Expenses
            val exp1Id = db.expenseDao().insertExpense(
                ExpenseEntity(
                    expenseNumber = "EXP-2026-001",
                    expenseDate = now - (12 * dayMs),
                    categoryId = catRent,
                    categoryName = "Rent & Lease",
                    amount = 1500.00,
                    taxAmount = 0.0,
                    totalAmount = 1500.00,
                    paymentMode = "BANK_TRANSFER",
                    paymentStatus = "PAID",
                    paidAmount = 1500.00,
                    referenceNumber = "FT-9832104",
                    payeeName = "Commercial Properties Ltd",
                    notes = "Monthly storefront rent for May 2026",
                    createdDate = now - (12 * dayMs)
                )
            )

            val exp2Id = db.expenseDao().insertExpense(
                ExpenseEntity(
                    expenseNumber = "EXP-2026-002",
                    expenseDate = now - (5 * dayMs),
                    categoryId = catUtil,
                    categoryName = "Utilities & Power",
                    amount = 240.00,
                    taxAmount = 24.00,
                    totalAmount = 264.00,
                    paymentMode = "UPI",
                    paymentStatus = "PAID",
                    paidAmount = 264.00,
                    referenceNumber = "UPI-771239",
                    payeeName = "City Power Corp",
                    notes = "Electricity & Fiber internet bill",
                    createdDate = now - (5 * dayMs)
                )
            )

            val exp3Id = db.expenseDao().insertExpense(
                ExpenseEntity(
                    expenseNumber = "EXP-2026-003",
                    expenseDate = now - (1 * dayMs),
                    categoryId = catMaint,
                    categoryName = "Maintenance & Repairs",
                    amount = 120.00,
                    taxAmount = 0.0,
                    totalAmount = 120.00,
                    paymentMode = "CASH",
                    paymentStatus = "PAID",
                    paidAmount = 120.00,
                    referenceNumber = "REC-4410",
                    payeeName = "QuickFix Electronics",
                    notes = "POS Printer thermal head replacement",
                    createdDate = now - (1 * dayMs)
                )
            )

            // Seed Income Entries
            val inc1Id = db.incomeDao().insertIncome(
                IncomeEntity(
                    incomeNumber = "INC-2026-001",
                    incomeDate = now - (8 * dayMs),
                    category = "Consulting Services",
                    customerId = custId1,
                    customerName = "Acme Global Solutions",
                    amount = 850.00,
                    paymentMode = "BANK_TRANSFER",
                    referenceNumber = "NEFT-004812",
                    notes = "POS Software Customization and Staff Onboarding",
                    createdDate = now - (8 * dayMs)
                )
            )

            val inc2Id = db.incomeDao().insertIncome(
                IncomeEntity(
                    incomeNumber = "INC-2026-002",
                    incomeDate = now - (3 * dayMs),
                    category = "Other Income",
                    customerId = null,
                    customerName = "Recycling Trade Corp",
                    amount = 180.00,
                    paymentMode = "CASH",
                    referenceNumber = "CSH-9921",
                    notes = "Sale of old packaging scrap and unused pallets",
                    createdDate = now - (3 * dayMs)
                )
            )

            // Seed Cash Book Entries
            db.cashBookDao().insertEntry(
                CashBookEntryEntity(
                    entryDate = now - (30 * dayMs),
                    entryType = "CASH_IN",
                    sourceType = "OPENING_BALANCE",
                    referenceNumber = "OB-CASH-001",
                    entityName = "Store Cash Drawer",
                    description = "Initial cash balance recorded",
                    amount = 5000.00,
                    paymentMode = "CASH",
                    balanceAfter = 5000.00,
                    createdDate = now - (30 * dayMs)
                )
            )

            db.cashBookDao().insertEntry(
                CashBookEntryEntity(
                    entryDate = now - (12 * dayMs),
                    entryType = "CASH_OUT",
                    sourceType = "EXPENSE",
                    referenceId = exp1Id,
                    referenceNumber = "EXP-2026-001",
                    entityName = "Commercial Properties Ltd",
                    description = "Rent & Lease: Monthly storefront rent",
                    amount = 1500.00,
                    paymentMode = "BANK_TRANSFER",
                    balanceAfter = 3500.00,
                    createdDate = now - (12 * dayMs)
                )
            )

            db.cashBookDao().insertEntry(
                CashBookEntryEntity(
                    entryDate = now - (8 * dayMs),
                    entryType = "CASH_IN",
                    sourceType = "INCOME",
                    referenceId = inc1Id,
                    referenceNumber = "INC-2026-001",
                    entityName = "Acme Global Solutions",
                    description = "Consulting Services: POS Software Customization",
                    amount = 850.00,
                    paymentMode = "BANK_TRANSFER",
                    balanceAfter = 4350.00,
                    createdDate = now - (8 * dayMs)
                )
            )

            db.cashBookDao().insertEntry(
                CashBookEntryEntity(
                    entryDate = now - (5 * dayMs),
                    entryType = "CASH_OUT",
                    sourceType = "EXPENSE",
                    referenceId = exp2Id,
                    referenceNumber = "EXP-2026-002",
                    entityName = "City Power Corp",
                    description = "Utilities & Power: Electricity & Fiber internet",
                    amount = 264.00,
                    paymentMode = "UPI",
                    balanceAfter = 4086.00,
                    createdDate = now - (5 * dayMs)
                )
            )

            db.cashBookDao().insertEntry(
                CashBookEntryEntity(
                    entryDate = now - (3 * dayMs),
                    entryType = "CASH_IN",
                    sourceType = "INCOME",
                    referenceId = inc2Id,
                    referenceNumber = "INC-2026-002",
                    entityName = "Recycling Trade Corp",
                    description = "Other Income: Sale of old packaging scrap",
                    amount = 180.00,
                    paymentMode = "CASH",
                    balanceAfter = 4266.00,
                    createdDate = now - (3 * dayMs)
                )
            )

            db.cashBookDao().insertEntry(
                CashBookEntryEntity(
                    entryDate = now - (1 * dayMs),
                    entryType = "CASH_OUT",
                    sourceType = "EXPENSE",
                    referenceId = exp3Id,
                    referenceNumber = "EXP-2026-003",
                    entityName = "QuickFix Electronics",
                    description = "Maintenance & Repairs: POS Printer thermal head replacement",
                    amount = 120.00,
                    paymentMode = "CASH",
                    balanceAfter = 4146.00,
                    createdDate = now - (1 * dayMs)
                )
            )
        }
    }
}
