package com.example.features.commandcenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.BusinessHealthEntity
import com.example.core.database.entity.DashboardWidgetEntity
import com.example.core.database.entity.TaskCenterEntity
import com.example.repositories.CustomerRepository
import com.example.repositories.ExpenseRepository
import com.example.repositories.IncomeRepository
import com.example.repositories.InventoryRepository
import com.example.repositories.InvoiceRepository
import com.example.repositories.PurchaseRepository
import com.example.repositories.SettingsRepository
import com.example.repositories.SupplierRepository
import com.example.services.ActivityAggregatorService
import com.example.services.BusinessHealthService
import com.example.services.TaskEngineService
import com.example.services.UnifiedActivityItem
import com.example.services.WidgetEngineService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CommandMetricsState(
    val businessName: String = "Enterprise Inc.",
    val currencySymbol: String = "₹",
    val todaySales: Double = 0.0,
    val todayPurchases: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val todayIncome: Double = 0.0,
    val outstandingReceivables: Double = 0.0,
    val outstandingPayables: Double = 0.0,
    val inventoryValue: Double = 0.0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val totalProducts: Int = 0,
    val pendingTaskCount: Int = 0,
    val isLoading: Boolean = false
)

class CommandCenterViewModel(
    private val widgetEngineService: WidgetEngineService,
    private val taskEngineService: TaskEngineService,
    private val businessHealthService: BusinessHealthService,
    private val activityAggregatorService: ActivityAggregatorService,
    private val invoiceRepository: InvoiceRepository,
    private val purchaseRepository: PurchaseRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val inventoryRepository: InventoryRepository,
    private val customerRepository: CustomerRepository,
    private val supplierRepository: SupplierRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val activeWidgets: StateFlow<List<DashboardWidgetEntity>> = widgetEngineService.getFilteredWidgetsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks: StateFlow<List<TaskCenterEntity>> = taskEngineService.pendingTasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestHealth: StateFlow<BusinessHealthEntity?> = businessHealthService.latestHealthFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentActivities: StateFlow<List<UnifiedActivityItem>> = activityAggregatorService.unifiedActivityFeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val metricsState: StateFlow<CommandMetricsState> = combine(
        invoiceRepository.totalPaidRevenue,
        purchaseRepository.totalPurchasesAmount,
        expenseRepository.totalExpensesAmount,
        incomeRepository.totalIncomeAmount,
        customerRepository.totalOutstandingBalance,
        inventoryRepository.allItems,
        inventoryRepository.lowStockItems,
        inventoryRepository.outOfStockItems,
        inventoryRepository.totalInventoryValue,
        taskEngineService.pendingTaskCountFlow
    ) { arrayOfValues ->
        val sales = arrayOfValues[0] as Double? ?: 0.0
        val purchases = arrayOfValues[1] as Double? ?: 0.0
        val expenses = arrayOfValues[2] as Double? ?: 0.0
        val income = arrayOfValues[3] as Double? ?: 0.0
        val receivables = arrayOfValues[4] as Double? ?: 0.0
        val allProds = arrayOfValues[5] as List<*>
        val lowStock = arrayOfValues[6] as List<*>
        val outOfStock = arrayOfValues[7] as List<*>
        val invVal = arrayOfValues[8] as Double? ?: 0.0
        val taskCount = arrayOfValues[9] as Int

        CommandMetricsState(
            businessName = settingsRepository.getBusinessName(),
            currencySymbol = settingsRepository.getCurrencySymbol(),
            todaySales = sales,
            todayPurchases = purchases,
            todayExpenses = expenses,
            todayIncome = income,
            outstandingReceivables = receivables,
            outstandingPayables = purchases * 0.15,
            inventoryValue = invVal,
            lowStockCount = lowStock.size,
            outOfStockCount = outOfStock.size,
            totalProducts = allProds.size,
            pendingTaskCount = taskCount,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CommandMetricsState(isLoading = true))

    init {
        viewModelScope.launch {
            widgetEngineService.initializeDefaultWidgetsIfEmpty()
            taskEngineService.evaluateAndGenerateTasks()
            businessHealthService.calculateAndStoreBusinessHealth()
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            taskEngineService.evaluateAndGenerateTasks()
            businessHealthService.calculateAndStoreBusinessHealth()
        }
    }

    fun toggleWidget(key: String, isEnabled: Boolean) {
        viewModelScope.launch {
            widgetEngineService.toggleWidget(key, isEnabled)
        }
    }

    fun togglePin(key: String, isPinned: Boolean) {
        viewModelScope.launch {
            widgetEngineService.togglePin(key, isPinned)
        }
    }

    class Factory(
        private val widgetEngineService: WidgetEngineService,
        private val taskEngineService: TaskEngineService,
        private val businessHealthService: BusinessHealthService,
        private val activityAggregatorService: ActivityAggregatorService,
        private val invoiceRepository: InvoiceRepository,
        private val purchaseRepository: PurchaseRepository,
        private val expenseRepository: ExpenseRepository,
        private val incomeRepository: IncomeRepository,
        private val inventoryRepository: InventoryRepository,
        private val customerRepository: CustomerRepository,
        private val supplierRepository: SupplierRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CommandCenterViewModel(
                widgetEngineService,
                taskEngineService,
                businessHealthService,
                activityAggregatorService,
                invoiceRepository,
                purchaseRepository,
                expenseRepository,
                incomeRepository,
                inventoryRepository,
                customerRepository,
                supplierRepository,
                settingsRepository
            ) as T
        }
    }
}
