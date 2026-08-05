package com.example.services

import com.example.core.database.dao.ReportDao
import com.example.features.reports.models.BusinessInsight
import com.example.features.reports.models.InsightPriority
import com.example.features.reports.models.InsightType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InsightsService(
    private val reportDao: ReportDao
) {
    suspend fun generateLocalInsights(startDate: Long, endDate: Long): List<BusinessInsight> = withContext(Dispatchers.IO) {
        val insights = mutableListOf<BusinessInsight>()

        // 1. Top Selling Product Insight
        val topProducts = reportDao.getTopSellingProducts(startDate, endDate, 1)
        if (topProducts.isNotEmpty()) {
            val topP = topProducts.first()
            insights.add(
                BusinessInsight(
                    id = "top_selling_product",
                    title = "Top Revenue Product",
                    description = "'${topP.productName}' generated $${String.format("%.2f", topP.totalRevenue)} with ${topP.totalQty.toInt()} units sold.",
                    recommendation = "Ensure stock levels remain above reorder thresholds to prevent stockouts.",
                    type = InsightType.SALES,
                    priority = InsightPriority.HIGH,
                    metricValue = "$${String.format("%.2f", topP.totalRevenue)}"
                )
            )
        }

        // 2. High Value Customer
        val topCustomers = reportDao.getTopCustomers(startDate, endDate, 1)
        if (topCustomers.isNotEmpty()) {
            val topC = topCustomers.first()
            insights.add(
                BusinessInsight(
                    id = "top_customer",
                    title = "Highest Spending Customer",
                    description = "'${topC.customerName}' accounted for $${String.format("%.2f", topC.totalSpent)} in revenue.",
                    recommendation = "Consider offering priority trade credit terms or volume loyalty discounts.",
                    type = InsightType.CUSTOMER,
                    priority = InsightPriority.MEDIUM,
                    metricValue = "$${String.format("%.2f", topC.totalSpent)}"
                )
            )
        }

        // 3. Top Vendor / Supplier
        val topSuppliers = reportDao.getTopSuppliers(startDate, endDate, 1)
        if (topSuppliers.isNotEmpty()) {
            val topS = topSuppliers.first()
            insights.add(
                BusinessInsight(
                    id = "top_supplier",
                    title = "Primary Goods Supplier",
                    description = "'${topS.supplierName}' delivered $${String.format("%.2f", topS.totalPurchased)} in inventory purchases.",
                    recommendation = "Negotiate early payment cash discounts with supplier.",
                    type = InsightType.SUPPLIER,
                    priority = InsightPriority.MEDIUM,
                    metricValue = "$${String.format("%.2f", topS.totalPurchased)}"
                )
            )
        }

        // 4. Low Stock Alert
        val lowStockCount = reportDao.getLowStockCount()
        val outOfStockCount = reportDao.getOutOfStockCount()
        if (lowStockCount > 0 || outOfStockCount > 0) {
            insights.add(
                BusinessInsight(
                    id = "stock_warning",
                    title = "Inventory Replenishment Required",
                    description = "$lowStockCount items are below reorder threshold and $outOfStockCount items are currently out of stock.",
                    recommendation = "Generate PO orders immediately for low stock items to avoid lost sales.",
                    type = InsightType.INVENTORY,
                    priority = InsightPriority.HIGH,
                    metricValue = "$lowStockCount Low / $outOfStockCount Empty"
                )
            )
        }

        // 5. Profitability & Cash Flow
        val sales = reportDao.getTotalSalesAmount(startDate, endDate)
        val expenses = reportDao.getTotalExpensesAmount(startDate, endDate)
        val purchases = reportDao.getTotalPurchasesAmount(startDate, endDate)
        val netProfit = sales - purchases - expenses

        if (sales > 0) {
            val margin = (netProfit / sales) * 100
            insights.add(
                BusinessInsight(
                    id = "profit_margin",
                    title = "Net Profitability Margin",
                    description = "Operating net profit is $${String.format("%.2f", netProfit)} (${String.format("%.1f", margin)}% margin).",
                    recommendation = if (margin > 15) "Operating health is strong." else "Review expense categories to optimize operational overhead.",
                    type = InsightType.FINANCIAL,
                    priority = if (margin < 10) InsightPriority.HIGH else InsightPriority.LOW,
                    metricValue = "${String.format("%.1f", margin)}%"
                )
            )
        }

        insights
    }
}
