package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.constants.NavRoutes
import com.example.features.auth.AuthScreen
import com.example.features.auth.AuthViewModel
import com.example.features.customers.CustomerScreen
import com.example.features.customers.CustomerViewModel
import com.example.features.dashboard.DashboardScreen
import com.example.features.dashboard.DashboardViewModel
import com.example.features.inventory.InventoryScreen
import com.example.features.inventory.InventoryViewModel
import com.example.features.invoice.InvoiceScreen
import com.example.features.invoice.InvoiceViewModel
import com.example.repositories.AppRepositoryProvider
import com.example.shared.components.AppShell
import com.example.shared.components.CommercialAppBar
import com.example.shared.widgets.PageHeader
import com.example.ui.theme.SmartBizTheme
import kotlinx.coroutines.launch

import com.example.features.cashbook.CashBookScreen
import com.example.features.cashbook.CashBookViewModel
import com.example.features.expenses.ExpenseScreen
import com.example.features.expenses.ExpenseViewModel
import com.example.features.income.IncomeScreen
import com.example.features.income.IncomeViewModel
import com.example.features.purchases.PurchaseScreen
import com.example.features.purchases.PurchaseViewModel
import com.example.features.suppliers.SupplierScreen
import com.example.features.suppliers.SupplierViewModel
import com.example.features.reports.ReportsScreen
import com.example.features.reports.ReportsViewModel
import com.example.features.settings.SettingsScreen
import com.example.features.settings.SettingsViewModel
import com.example.features.search.GlobalSearchScreen
import com.example.features.search.GlobalSearchViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Application Repository Provider
        val repoProvider = AppRepositoryProvider.initialize(applicationContext)

        setContent {
            SmartBizTheme {
                SmartBizApp(repoProvider = repoProvider)
            }
        }
    }
}

@Composable
fun SmartBizApp(repoProvider: AppRepositoryProvider) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(repoProvider.authRepository, context)
    )
    val authState by authViewModel.uiState.collectAsState()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.DASHBOARD

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isLoadingOverlay by remember { mutableStateOf(false) }

    val businessName = remember { repoProvider.settingsRepository.getBusinessName() }

    if (!authState.isAuthenticated && !authState.isCheckingSession) {
        AuthScreen(
            viewModel = authViewModel,
            onAuthenticated = {
                authViewModel.checkSession()
            }
        )
    } else {
        AppShell(
            currentRoute = currentRoute,
            onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            snackbarHostState = snackbarHostState,
            topBar = {
                CommercialAppBar(
                    businessName = authState.currentUser?.businessName ?: businessName,
                    currentDateText = "Today",
                    notificationCount = 3,
                    onSearchClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Search feature active across invoices & products")
                        }
                    },
                    onNotificationClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("3 new store alerts: 2 low stock items, 1 payment received")
                        }
                    },
                    onProfileClick = {
                        navController.navigate(NavRoutes.SETTINGS) {
                            popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            },
            isLoadingOverlayVisible = isLoadingOverlay
        ) { paddingModifier ->
            NavHost(
                navController = navController,
                startDestination = NavRoutes.DASHBOARD,
                modifier = paddingModifier.fillMaxSize()
            ) {
            composable(NavRoutes.DASHBOARD) {
                val viewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(
                        repoProvider.invoiceRepository,
                        repoProvider.inventoryRepository,
                        repoProvider.customerRepository,
                        repoProvider.settingsRepository
                    )
                )
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToInvoices = {
                        navController.navigate(NavRoutes.INVOICES) {
                            popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToInventory = {
                        navController.navigate(NavRoutes.INVENTORY) {
                            popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToCustomers = {
                        navController.navigate(NavRoutes.CUSTOMERS) {
                            popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToPurchases = {
                        navController.navigate(NavRoutes.PURCHASES) {
                            popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSuppliers = {
                        navController.navigate(NavRoutes.SUPPLIERS) {
                            popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToExpenses = {
                        navController.navigate(NavRoutes.EXPENSES) {
                            popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToIncome = {
                        navController.navigate(NavRoutes.INCOME) {
                            popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToCashBook = {
                        navController.navigate(NavRoutes.CASH_BOOK) {
                            popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToReports = {
                        navController.navigate(NavRoutes.REPORTS) {
                            popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSearch = {
                        navController.navigate(NavRoutes.SEARCH)
                    }
                )
            }

            composable(NavRoutes.PURCHASES) {
                val viewModel: PurchaseViewModel = viewModel(
                    factory = PurchaseViewModel.Factory(
                        repoProvider.purchaseRepository,
                        repoProvider.supplierRepository,
                        repoProvider.inventoryRepository
                    )
                )
                PurchaseScreen(viewModel = viewModel)
            }

            composable(NavRoutes.SUPPLIERS) {
                val viewModel: SupplierViewModel = viewModel(
                    factory = SupplierViewModel.Factory(
                        repoProvider.supplierRepository
                    )
                )
                SupplierScreen(viewModel = viewModel)
            }

            composable(NavRoutes.EXPENSES) {
                val viewModel: ExpenseViewModel = viewModel(
                    factory = ExpenseViewModel.Factory(
                        repoProvider.expenseRepository,
                        repoProvider.expenseCategoryRepository
                    )
                )
                ExpenseScreen(viewModel = viewModel)
            }

            composable(NavRoutes.INCOME) {
                val viewModel: IncomeViewModel = viewModel(
                    factory = IncomeViewModel.Factory(
                        repoProvider.incomeRepository,
                        repoProvider.customerRepository
                    )
                )
                IncomeScreen(viewModel = viewModel)
            }

            composable(NavRoutes.CASH_BOOK) {
                val viewModel: CashBookViewModel = viewModel(
                    factory = CashBookViewModel.Factory(
                        repoProvider.cashBookRepository
                    )
                )
                CashBookScreen(viewModel = viewModel)
            }

            composable(NavRoutes.INVOICES) {
                val viewModel: InvoiceViewModel = viewModel(
                    factory = InvoiceViewModel.Factory(
                        repoProvider.invoiceRepository,
                        repoProvider.inventoryRepository,
                        repoProvider.customerRepository,
                        repoProvider.settingsRepository
                    )
                )
                InvoiceScreen(viewModel = viewModel)
            }

            composable(NavRoutes.INVENTORY) {
                val viewModel: InventoryViewModel = viewModel(
                    factory = InventoryViewModel.Factory(
                        repoProvider.inventoryRepository,
                        repoProvider.categoryRepository,
                        repoProvider.settingsRepository
                    )
                )
                InventoryScreen(viewModel = viewModel)
            }

            composable(NavRoutes.CUSTOMERS) {
                val viewModel: CustomerViewModel = viewModel(
                    factory = CustomerViewModel.Factory(
                        repoProvider.customerRepository,
                        repoProvider.settingsRepository
                    )
                )
                CustomerScreen(viewModel = viewModel)
            }

            composable(NavRoutes.REPORTS) {
                val viewModel: ReportsViewModel = viewModel(
                    factory = ReportsViewModel.Factory(
                        repoProvider.reportsRepository,
                        repoProvider.analyticsRepository,
                        repoProvider.insightsService
                    )
                )
                ReportsScreen(viewModel = viewModel)
            }

            composable(NavRoutes.SETTINGS) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        repoProvider.settingsRepository
                    )
                )
                SettingsScreen(viewModel = viewModel)
            }

            composable(NavRoutes.SEARCH) {
                val viewModel: GlobalSearchViewModel = viewModel(
                    factory = GlobalSearchViewModel.Factory(
                        repoProvider.globalSearchRepository
                    )
                )
                GlobalSearchScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.REALTIME_DASHBOARD) {
                val viewModel: com.example.features.realtime.RealtimeViewModel = viewModel(
                    factory = com.example.features.realtime.RealtimeViewModel.Factory(
                        repoProvider.realtimeRepository,
                        repoProvider.presenceRepository,
                        repoProvider.connectionService
                    )
                )
                com.example.features.realtime.screens.RealtimeDashboardScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.COMMUNICATION_ENGINE) {
                val viewModel: com.example.features.communication.viewmodel.CommunicationViewModel = viewModel(
                    factory = com.example.features.communication.viewmodel.CommunicationViewModel.Factory(
                        repoProvider.communicationRepository,
                        repoProvider.communicationEngineService
                    )
                )
                com.example.features.communication.screens.CommunicationContainerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.PLUGIN_MARKETPLACE) {
                val viewModel: com.example.features.plugin.PluginViewModel = viewModel(
                    factory = com.example.features.plugin.PluginViewModel.Factory(
                        repoProvider.pluginRepository
                    )
                )
                com.example.features.plugin.PluginMarketplaceScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.WORKFLOW_AUTOMATION) {
                val viewModel: com.example.features.workflow.WorkflowViewModel = viewModel(
                    factory = com.example.features.workflow.WorkflowViewModel.Factory(
                        repoProvider.workflowRepository
                    )
                )
                com.example.features.workflow.screens.WorkflowContainerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.GLOBALIZATION_PLATFORM) {
                com.example.features.globalization.ui.GlobalizationDashboardScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.PERFORMANCE_HEALTH_DASHBOARD) {
                com.example.features.scalability.ui.ScalabilityDashboardScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
}
