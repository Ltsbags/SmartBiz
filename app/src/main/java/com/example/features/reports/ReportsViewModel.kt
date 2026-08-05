package com.example.features.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.features.reports.models.DateFilterOption
import com.example.features.reports.models.ReportExportType
import com.example.repositories.AnalyticsRepository
import com.example.repositories.ReportsRepository
import com.example.services.InsightsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportsViewModel(
    private val reportsRepository: ReportsRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val insightsService: InsightsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReportData()
    }

    fun onDateFilterSelected(option: DateFilterOption, customStartMs: Long? = null, customEndMs: Long? = null) {
        _uiState.update {
            it.copy(
                selectedDateOption = option,
                customStartDateMs = customStartMs,
                customEndDateMs = customEndMs
            )
        }
        loadReportData()
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun triggerExport(exportType: ReportExportType) {
        val message = "Exporting ${uiState.value.selectedDateOption.displayName} report as ${exportType.name}..."
        _uiState.update { it.copy(userMessage = message) }
    }

    fun loadReportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val state = _uiState.value
                val (startMs, endMs) = state.selectedDateOption.getTimeRange(
                    state.customStartDateMs,
                    state.customEndDateMs
                )

                val sales = reportsRepository.getSalesAnalytics(startMs, endMs)
                val purchase = reportsRepository.getPurchaseAnalytics(startMs, endMs)
                val inventory = reportsRepository.getInventoryAnalytics(startMs, endMs)
                val customer = reportsRepository.getCustomerAnalytics(startMs, endMs)
                val supplier = reportsRepository.getSupplierAnalytics(startMs, endMs)
                val financial = reportsRepository.getFinancialAnalytics(startMs, endMs)
                val gst = reportsRepository.getGstSummary(startMs, endMs)

                val insights = insightsService.generateLocalInsights(startMs, endMs)
                val salesTrend = analyticsRepository.getDailySalesTrend(startMs, endMs)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        salesData = sales,
                        purchaseData = purchase,
                        inventoryData = inventory,
                        customerData = customer,
                        supplierData = supplier,
                        financialData = financial,
                        gstData = gst,
                        insights = insights,
                        salesTrend = salesTrend
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = "Error compiling reports: ${e.message}"
                    )
                }
            }
        }
    }

    class Factory(
        private val reportsRepository: ReportsRepository,
        private val analyticsRepository: AnalyticsRepository,
        private val insightsService: InsightsService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportsViewModel(reportsRepository, analyticsRepository, insightsService) as T
        }
    }
}
