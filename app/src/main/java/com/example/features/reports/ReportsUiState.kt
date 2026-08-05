package com.example.features.reports

import com.example.features.reports.models.BusinessInsight
import com.example.features.reports.models.ChartDataPoint
import com.example.features.reports.models.CustomerAnalyticsData
import com.example.features.reports.models.DateFilterOption
import com.example.features.reports.models.FinancialAnalyticsData
import com.example.features.reports.models.GstSummaryData
import com.example.features.reports.models.InventoryAnalyticsData
import com.example.features.reports.models.PurchaseAnalyticsData
import com.example.features.reports.models.SalesAnalyticsData
import com.example.features.reports.models.SupplierAnalyticsData

data class ReportsUiState(
    val selectedDateOption: DateFilterOption = DateFilterOption.THIS_MONTH,
    val customStartDateMs: Long? = null,
    val customEndDateMs: Long? = null,
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val salesData: SalesAnalyticsData = SalesAnalyticsData(),
    val purchaseData: PurchaseAnalyticsData = PurchaseAnalyticsData(),
    val inventoryData: InventoryAnalyticsData = InventoryAnalyticsData(),
    val customerData: CustomerAnalyticsData = CustomerAnalyticsData(),
    val supplierData: SupplierAnalyticsData = SupplierAnalyticsData(),
    val financialData: FinancialAnalyticsData = FinancialAnalyticsData(),
    val gstData: GstSummaryData = GstSummaryData(),
    val insights: List<BusinessInsight> = emptyList(),
    val salesTrend: List<ChartDataPoint> = emptyList(),
    val userMessage: String? = null
)
