package com.example.features.reports.models

import androidx.compose.ui.graphics.Color

data class ChartDataPoint(
    val label: String,
    val value: Float,
    val color: Color = Color.Unspecified,
    val dateMs: Long = 0L,
    val secondaryValue: Float = 0f
)

data class PieChartSegment(
    val category: String,
    val value: Float,
    val percentage: Float,
    val color: Color
)

enum class InsightPriority {
    HIGH, MEDIUM, LOW
}

enum class InsightType {
    SALES, INVENTORY, FINANCIAL, CUSTOMER, SUPPLIER
}

data class BusinessInsight(
    val id: String,
    val title: String,
    val description: String,
    val recommendation: String,
    val type: InsightType,
    val priority: InsightPriority,
    val metricValue: String? = null
)

data class SalesAnalyticsData(
    val totalRevenue: Double = 0.0,
    val invoiceCount: Int = 0,
    val averageInvoiceValue: Double = 0.0,
    val paidRevenue: Double = 0.0,
    val outstandingRevenue: Double = 0.0,
    val totalGstCollected: Double = 0.0,
    val topSellingProducts: List<TopProductSummary> = emptyList(),
    val topCategories: List<CategoryRevenueSummary> = emptyList(),
    val topCustomers: List<CustomerRevenueSummary> = emptyList(),
    val salesTrend: List<ChartDataPoint> = emptyList()
)

data class PurchaseAnalyticsData(
    val totalPurchases: Double = 0.0,
    val purchaseCount: Int = 0,
    val averagePurchaseValue: Double = 0.0,
    val paidPurchases: Double = 0.0,
    val pendingPurchases: Double = 0.0,
    val totalGstPaid: Double = 0.0,
    val topSuppliers: List<SupplierPurchaseSummary> = emptyList(),
    val purchaseTrend: List<ChartDataPoint> = emptyList()
)

data class InventoryAnalyticsData(
    val totalValuation: Double = 0.0,
    val totalItemsCount: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val fastMovingProducts: List<TopProductSummary> = emptyList(),
    val slowMovingProducts: List<TopProductSummary> = emptyList(),
    val deadStockProducts: List<TopProductSummary> = emptyList(),
    val categoryValuations: List<PieChartSegment> = emptyList()
)

data class CustomerAnalyticsData(
    val totalCustomers: Int = 0,
    val totalOutstandingReceivables: Double = 0.0,
    val topCustomersBySpending: List<CustomerRevenueSummary> = emptyList(),
    val recentCustomersCount: Int = 0
)

data class SupplierAnalyticsData(
    val totalSuppliers: Int = 0,
    val totalOutstandingPayables: Double = 0.0,
    val topSuppliersByVolume: List<SupplierPurchaseSummary> = emptyList()
)

data class FinancialAnalyticsData(
    val totalRevenue: Double = 0.0,
    val totalCostOfGoods: Double = 0.0,
    val grossProfit: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val otherIncome: Double = 0.0,
    val netProfit: Double = 0.0,
    val netMarginPercentage: Double = 0.0,
    val totalCashIn: Double = 0.0,
    val totalCashOut: Double = 0.0,
    val netCashFlow: Double = 0.0,
    val expenseBreakdown: List<PieChartSegment> = emptyList(),
    val incomeBreakdown: List<PieChartSegment> = emptyList(),
    val monthlyFinancialTrends: List<ChartDataPoint> = emptyList()
)

data class GstSummaryData(
    val outputGstSales: Double = 0.0,
    val inputGstPurchases: Double = 0.0,
    val netGstPayable: Double = 0.0,
    val totalTaxableSales: Double = 0.0,
    val totalTaxablePurchases: Double = 0.0
)

data class TopProductSummary(
    val productId: Long,
    val name: String,
    val sku: String,
    val quantitySold: Double,
    val totalRevenue: Double,
    val stockLeft: Int
)

data class CategoryRevenueSummary(
    val categoryName: String,
    val totalRevenue: Double,
    val percentage: Float
)

data class CustomerRevenueSummary(
    val customerId: Long,
    val name: String,
    val totalSpent: Double,
    val outstandingBalance: Double
)

data class SupplierPurchaseSummary(
    val supplierId: Long,
    val name: String,
    val totalPurchased: Double,
    val outstandingPayable: Double
)

enum class ReportExportType {
    PDF, EXCEL, CSV
}

data class ReportExportData(
    val title: String,
    val dateFilterName: String,
    val generatedDateFormatted: String,
    val headers: List<String>,
    val rows: List<List<String>>,
    val summaryLabelsAndValues: List<Pair<String, String>>
)
