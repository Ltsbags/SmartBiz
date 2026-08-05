package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class ProductSalesAggregate(
    val productId: Long,
    val productName: String,
    val sku: String,
    val totalQty: Double,
    val totalRevenue: Double
)

data class CategorySalesAggregate(
    val categoryName: String,
    val totalRevenue: Double
)

data class CustomerSpendingAggregate(
    val customerId: Long,
    val customerName: String,
    val totalSpent: Double,
    val outstandingBalance: Double
)

data class SupplierPurchaseAggregate(
    val supplierId: Long,
    val supplierName: String,
    val totalPurchased: Double,
    val outstandingPayable: Double
)

data class CategoryExpenseAggregate(
    val categoryName: String,
    val totalExpense: Double
)

data class CategoryIncomeAggregate(
    val categoryName: String,
    val totalIncome: Double
)

@Dao
interface ReportDao {

    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0.0) FROM invoices 
        WHERE date >= :startDate AND date <= :endDate AND status != 'CANCELLED'
    """)
    suspend fun getTotalSalesAmount(startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COUNT(*) FROM invoices 
        WHERE date >= :startDate AND date <= :endDate AND status != 'CANCELLED'
    """)
    suspend fun getSalesCount(startDate: Long, endDate: Long): Int

    @Query("""
        SELECT COALESCE(SUM(paidAmount), 0.0) FROM invoices 
        WHERE date >= :startDate AND date <= :endDate AND status != 'CANCELLED'
    """)
    suspend fun getPaidSalesAmount(startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(balanceAmount), 0.0) FROM invoices 
        WHERE status != 'CANCELLED'
    """)
    suspend fun getTotalOutstandingReceivables(): Double

    @Query("""
        SELECT COALESCE(SUM(taxAmount), 0.0) FROM invoices 
        WHERE date >= :startDate AND date <= :endDate AND status != 'CANCELLED'
    """)
    suspend fun getTotalSalesGst(startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0.0) FROM purchases 
        WHERE purchaseDate >= :startDate AND purchaseDate <= :endDate AND status != 'CANCELLED'
    """)
    suspend fun getTotalPurchasesAmount(startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COUNT(*) FROM purchases 
        WHERE purchaseDate >= :startDate AND purchaseDate <= :endDate AND status != 'CANCELLED'
    """)
    suspend fun getPurchasesCount(startDate: Long, endDate: Long): Int

    @Query("""
        SELECT COALESCE(SUM(paidAmount), 0.0) FROM purchases 
        WHERE purchaseDate >= :startDate AND purchaseDate <= :endDate AND status != 'CANCELLED'
    """)
    suspend fun getPaidPurchasesAmount(startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(balanceAmount), 0.0) FROM purchases 
        WHERE status != 'CANCELLED'
    """)
    suspend fun getTotalOutstandingPayables(): Double

    @Query("""
        SELECT COALESCE(SUM(taxAmount), 0.0) FROM purchases 
        WHERE purchaseDate >= :startDate AND purchaseDate <= :endDate AND status != 'CANCELLED'
    """)
    suspend fun getTotalPurchasesGst(startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0.0) FROM expenses 
        WHERE expenseDate >= :startDate AND expenseDate <= :endDate
    """)
    suspend fun getTotalExpensesAmount(startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM income 
        WHERE incomeDate >= :startDate AND incomeDate <= :endDate
    """)
    suspend fun getTotalIncomeAmount(startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM cash_book_entries 
        WHERE entryDate >= :startDate AND entryDate <= :endDate AND entryType = 'CASH_IN'
    """)
    suspend fun getTotalCashIn(startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM cash_book_entries 
        WHERE entryDate >= :startDate AND entryDate <= :endDate AND entryType = 'CASH_OUT'
    """)
    suspend fun getTotalCashOut(startDate: Long, endDate: Long): Double

    @Query("""
        SELECT COALESCE(SUM(stockQuantity * purchasePrice), 0.0) FROM inventory_items
    """)
    suspend fun getTotalInventoryValuation(): Double

    @Query("""
        SELECT COUNT(*) FROM inventory_items WHERE stockQuantity <= minStockThreshold AND stockQuantity > 0
    """)
    suspend fun getLowStockCount(): Int

    @Query("""
        SELECT COUNT(*) FROM inventory_items WHERE stockQuantity <= 0
    """)
    suspend fun getOutOfStockCount(): Int

    @Query("""
        SELECT 
            i.productId AS productId,
            i.productName AS productName,
            i.sku AS sku,
            SUM(i.quantity) AS totalQty,
            SUM(i.lineTotal) AS totalRevenue
        FROM invoice_items i
        INNER JOIN invoices inv ON i.invoiceId = inv.id
        WHERE inv.date >= :startDate AND inv.date <= :endDate AND inv.status != 'CANCELLED'
        GROUP BY i.productId, i.productName, i.sku
        ORDER BY totalRevenue DESC
        LIMIT :limit
    """)
    suspend fun getTopSellingProducts(startDate: Long, endDate: Long, limit: Int = 10): List<ProductSalesAggregate>

    @Query("""
        SELECT 
            e.categoryName AS categoryName,
            SUM(e.totalAmount) AS totalExpense
        FROM expenses e
        WHERE e.expenseDate >= :startDate AND e.expenseDate <= :endDate
        GROUP BY e.categoryName
        ORDER BY totalExpense DESC
    """)
    suspend fun getExpenseCategoryBreakdown(startDate: Long, endDate: Long): List<CategoryExpenseAggregate>

    @Query("""
        SELECT 
            inc.category AS categoryName,
            SUM(inc.amount) AS totalIncome
        FROM income inc
        WHERE inc.incomeDate >= :startDate AND inc.incomeDate <= :endDate
        GROUP BY inc.category
        ORDER BY totalIncome DESC
    """)
    suspend fun getIncomeCategoryBreakdown(startDate: Long, endDate: Long): List<CategoryIncomeAggregate>

    @Query("""
        SELECT 
            c.id AS customerId,
            c.name AS customerName,
            COALESCE(SUM(inv.totalAmount), 0.0) AS totalSpent,
            c.outstandingBalance AS outstandingBalance
        FROM customers c
        LEFT JOIN invoices inv ON c.id = inv.customerId AND inv.status != 'CANCELLED' AND inv.date >= :startDate AND inv.date <= :endDate
        GROUP BY c.id, c.name, c.outstandingBalance
        ORDER BY totalSpent DESC
        LIMIT :limit
    """)
    suspend fun getTopCustomers(startDate: Long, endDate: Long, limit: Int = 10): List<CustomerSpendingAggregate>

    @Query("""
        SELECT 
            s.id AS supplierId,
            s.supplierName AS supplierName,
            COALESCE(SUM(p.totalAmount), 0.0) AS totalPurchased,
            s.outstandingBalance AS outstandingPayable
        FROM suppliers s
        LEFT JOIN purchases p ON s.id = p.supplierId AND p.status != 'CANCELLED' AND p.purchaseDate >= :startDate AND p.purchaseDate <= :endDate
        GROUP BY s.id, s.supplierName, s.outstandingBalance
        ORDER BY totalPurchased DESC
        LIMIT :limit
    """)
    suspend fun getTopSuppliers(startDate: Long, endDate: Long, limit: Int = 10): List<SupplierPurchaseAggregate>
}
