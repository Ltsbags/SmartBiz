package com.example.repositories

import androidx.compose.ui.graphics.Color
import com.example.core.database.dao.ReportDao
import com.example.features.reports.models.CategoryRevenueSummary
import com.example.features.reports.models.CustomerAnalyticsData
import com.example.features.reports.models.CustomerRevenueSummary
import com.example.features.reports.models.FinancialAnalyticsData
import com.example.features.reports.models.GstSummaryData
import com.example.features.reports.models.InventoryAnalyticsData
import com.example.features.reports.models.PieChartSegment
import com.example.features.reports.models.PurchaseAnalyticsData
import com.example.features.reports.models.SalesAnalyticsData
import com.example.features.reports.models.SupplierAnalyticsData
import com.example.features.reports.models.SupplierPurchaseSummary
import com.example.features.reports.models.TopProductSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportsRepository(
    private val reportDao: ReportDao
) {
    suspend fun getSalesAnalytics(startDate: Long, endDate: Long): SalesAnalyticsData = withContext(Dispatchers.IO) {
        val totalRevenue = reportDao.getTotalSalesAmount(startDate, endDate)
        val invoiceCount = reportDao.getSalesCount(startDate, endDate)
        val avgInvoice = if (invoiceCount > 0) totalRevenue / invoiceCount else 0.0
        val paidRevenue = reportDao.getPaidSalesAmount(startDate, endDate)
        val outstanding = reportDao.getTotalOutstandingReceivables()
        val gstCollected = reportDao.getTotalSalesGst(startDate, endDate)

        val topProductsAgg = reportDao.getTopSellingProducts(startDate, endDate, 10)
        val topProducts = topProductsAgg.map { agg ->
            TopProductSummary(
                productId = agg.productId,
                name = agg.productName,
                sku = agg.sku,
                quantitySold = agg.totalQty,
                totalRevenue = agg.totalRevenue,
                stockLeft = 0
            )
        }

        val topCustomersAgg = reportDao.getTopCustomers(startDate, endDate, 10)
        val topCustomers = topCustomersAgg.map { agg ->
            CustomerRevenueSummary(
                customerId = agg.customerId,
                name = agg.customerName,
                totalSpent = agg.totalSpent,
                outstandingBalance = agg.outstandingBalance
            )
        }

        SalesAnalyticsData(
            totalRevenue = totalRevenue,
            invoiceCount = invoiceCount,
            averageInvoiceValue = avgInvoice,
            paidRevenue = paidRevenue,
            outstandingRevenue = outstanding,
            totalGstCollected = gstCollected,
            topSellingProducts = topProducts,
            topCustomers = topCustomers
        )
    }

    suspend fun getPurchaseAnalytics(startDate: Long, endDate: Long): PurchaseAnalyticsData = withContext(Dispatchers.IO) {
        val totalPurchases = reportDao.getTotalPurchasesAmount(startDate, endDate)
        val purchaseCount = reportDao.getPurchasesCount(startDate, endDate)
        val avgPurchase = if (purchaseCount > 0) totalPurchases / purchaseCount else 0.0
        val paidPurchases = reportDao.getPaidPurchasesAmount(startDate, endDate)
        val pendingPurchases = reportDao.getTotalOutstandingPayables()
        val gstPaid = reportDao.getTotalPurchasesGst(startDate, endDate)

        val topSuppliersAgg = reportDao.getTopSuppliers(startDate, endDate, 10)
        val topSuppliers = topSuppliersAgg.map { agg ->
            SupplierPurchaseSummary(
                supplierId = agg.supplierId,
                name = agg.supplierName,
                totalPurchased = agg.totalPurchased,
                outstandingPayable = agg.outstandingPayable
            )
        }

        PurchaseAnalyticsData(
            totalPurchases = totalPurchases,
            purchaseCount = purchaseCount,
            averagePurchaseValue = avgPurchase,
            paidPurchases = paidPurchases,
            pendingPurchases = pendingPurchases,
            totalGstPaid = gstPaid,
            topSuppliers = topSuppliers
        )
    }

    suspend fun getInventoryAnalytics(startDate: Long, endDate: Long): InventoryAnalyticsData = withContext(Dispatchers.IO) {
        val totalValuation = reportDao.getTotalInventoryValuation()
        val lowStock = reportDao.getLowStockCount()
        val outOfStock = reportDao.getOutOfStockCount()

        val fastMoving = reportDao.getTopSellingProducts(startDate, endDate, 5).map {
            TopProductSummary(
                productId = it.productId,
                name = it.productName,
                sku = it.sku,
                quantitySold = it.totalQty,
                totalRevenue = it.totalRevenue,
                stockLeft = 0
            )
        }

        InventoryAnalyticsData(
            totalValuation = totalValuation,
            lowStockCount = lowStock,
            outOfStockCount = outOfStock,
            fastMovingProducts = fastMoving
        )
    }

    suspend fun getCustomerAnalytics(startDate: Long, endDate: Long): CustomerAnalyticsData = withContext(Dispatchers.IO) {
        val outstanding = reportDao.getTotalOutstandingReceivables()
        val topCusts = reportDao.getTopCustomers(startDate, endDate, 10).map {
            CustomerRevenueSummary(
                customerId = it.customerId,
                name = it.customerName,
                totalSpent = it.totalSpent,
                outstandingBalance = it.outstandingBalance
            )
        }

        CustomerAnalyticsData(
            totalOutstandingReceivables = outstanding,
            topCustomersBySpending = topCusts
        )
    }

    suspend fun getSupplierAnalytics(startDate: Long, endDate: Long): SupplierAnalyticsData = withContext(Dispatchers.IO) {
        val outstanding = reportDao.getTotalOutstandingPayables()
        val topSupps = reportDao.getTopSuppliers(startDate, endDate, 10).map {
            SupplierPurchaseSummary(
                supplierId = it.supplierId,
                name = it.supplierName,
                totalPurchased = it.totalPurchased,
                outstandingPayable = it.outstandingPayable
            )
        }

        SupplierAnalyticsData(
            totalOutstandingPayables = outstanding,
            topSuppliersByVolume = topSupps
        )
    }

    suspend fun getFinancialAnalytics(startDate: Long, endDate: Long): FinancialAnalyticsData = withContext(Dispatchers.IO) {
        val totalRevenue = reportDao.getTotalSalesAmount(startDate, endDate)
        val totalPurchases = reportDao.getTotalPurchasesAmount(startDate, endDate)
        val grossProfit = totalRevenue - totalPurchases
        val totalExpenses = reportDao.getTotalExpensesAmount(startDate, endDate)
        val otherIncome = reportDao.getTotalIncomeAmount(startDate, endDate)
        val netProfit = grossProfit - totalExpenses + otherIncome

        val netMargin = if (totalRevenue > 0) (netProfit / totalRevenue) * 100.0 else 0.0

        val cashIn = reportDao.getTotalCashIn(startDate, endDate)
        val cashOut = reportDao.getTotalCashOut(startDate, endDate)

        val expensesAgg = reportDao.getExpenseCategoryBreakdown(startDate, endDate)
        val palette = listOf(
            Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFF4CAF50),
            Color(0xFF00BCD4), Color(0xFF9C27B0), Color(0xFF3F51B5)
        )
        val totalExpSum = expensesAgg.sumOf { it.totalExpense }
        val expenseSegments = expensesAgg.mapIndexed { idx, item ->
            val pct = if (totalExpSum > 0) (item.totalExpense / totalExpSum * 100).toFloat() else 0f
            PieChartSegment(
                category = item.categoryName,
                value = item.totalExpense.toFloat(),
                percentage = pct,
                color = palette[idx % palette.size]
            )
        }

        val incomeAgg = reportDao.getIncomeCategoryBreakdown(startDate, endDate)
        val totalIncSum = incomeAgg.sumOf { it.totalIncome }
        val incomeSegments = incomeAgg.mapIndexed { idx, item ->
            val pct = if (totalIncSum > 0) (item.totalIncome / totalIncSum * 100).toFloat() else 0f
            PieChartSegment(
                category = item.categoryName,
                value = item.totalIncome.toFloat(),
                percentage = pct,
                color = palette[idx % palette.size]
            )
        }

        FinancialAnalyticsData(
            totalRevenue = totalRevenue,
            totalCostOfGoods = totalPurchases,
            grossProfit = grossProfit,
            totalExpenses = totalExpenses,
            otherIncome = otherIncome,
            netProfit = netProfit,
            netMarginPercentage = netMargin,
            totalCashIn = cashIn,
            totalCashOut = cashOut,
            netCashFlow = cashIn - cashOut,
            expenseBreakdown = expenseSegments,
            incomeBreakdown = incomeSegments
        )
    }

    suspend fun getGstSummary(startDate: Long, endDate: Long): GstSummaryData = withContext(Dispatchers.IO) {
        val outputGst = reportDao.getTotalSalesGst(startDate, endDate)
        val inputGst = reportDao.getTotalPurchasesGst(startDate, endDate)
        val taxableSales = reportDao.getTotalSalesAmount(startDate, endDate)
        val taxablePurchases = reportDao.getTotalPurchasesAmount(startDate, endDate)

        GstSummaryData(
            outputGstSales = outputGst,
            inputGstPurchases = inputGst,
            netGstPayable = outputGst - inputGst,
            totalTaxableSales = taxableSales,
            totalTaxablePurchases = taxablePurchases
        )
    }
}
