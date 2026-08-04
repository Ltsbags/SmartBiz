package com.example.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.core.constants.AppIcons
import com.example.core.theme.Spacing
import com.example.features.dashboard.components.AnimatedCard
import com.example.features.dashboard.components.DashboardGrid
import com.example.features.dashboard.components.DashboardHeader
import com.example.features.dashboard.components.RecentCustomerCard
import com.example.features.dashboard.components.RecentInvoiceCard
import com.example.features.dashboard.components.StatsCard
import com.example.features.dashboard.components.SummarySection
import com.example.shared.widgets.DashboardShimmerLoading
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToInvoices: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToPurchases: () -> Unit = {},
    onNavigateToSuppliers: () -> Unit = {},
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToCashBook: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    if (state.isLoading) {
        DashboardShimmerLoading()
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.l)
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.xs))
            AnimatedCard(delayMillis = 0) {
                DashboardHeader(
                    businessName = state.businessName,
                    dateText = state.currentDateText.ifEmpty { "Today" }
                )
            }
        }

        // Quick Action Grid
        item {
            AnimatedCard(delayMillis = 50) {
                DashboardGrid(
                    onCreateInvoice = onNavigateToInvoices,
                    onAddProduct = onNavigateToInventory,
                    onAddCustomer = onNavigateToCustomers,
                    onPurchasesClick = onNavigateToPurchases,
                    onSuppliersClick = onNavigateToSuppliers,
                    onExpensesClick = onNavigateToExpenses,
                    onIncomeClick = onNavigateToIncome,
                    onCashBookClick = onNavigateToCashBook,
                    onReportsClick = onNavigateToReports
                )
            }
        }

        // Statistics Cards Section
        item {
            AnimatedCard(delayMillis = 100) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
                    Text(
                        text = "Sales & Inventory Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                    ) {
                        StatsCard(
                            title = "Total Products",
                            value = "${state.totalProducts}",
                            subtitle = "Items in catalog",
                            icon = AppIcons.Inventory,
                            modifier = Modifier.weight(1f)
                        )
                        StatsCard(
                            title = "Stock Value",
                            value = "${state.currencySymbol}${String.format("%.2f", state.inventoryValue)}",
                            subtitle = "Total asset value",
                            icon = AppIcons.Paid,
                            isPositiveTrend = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                    ) {
                        StatsCard(
                            title = "Low Stock Alert",
                            value = "${state.lowStockCount}",
                            subtitle = "Items need restock",
                            icon = AppIcons.LowStock,
                            iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            iconColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                        StatsCard(
                            title = "Out of Stock",
                            value = "${state.outOfStockCount}",
                            subtitle = "Depleted items",
                            icon = AppIcons.Pending,
                            iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                            iconColor = MaterialTheme.colorScheme.error,
                            isPositiveTrend = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Executive Business Summary Section
        item {
            AnimatedCard(delayMillis = 150) {
                SummarySection(
                    totalRevenueText = "${state.currencySymbol}${String.format("%.2f", state.totalRevenue)}",
                    pendingAmountText = "${state.currencySymbol}${String.format("%.2f", state.pendingAmount)}",
                    collectionRate = state.collectionRate,
                    totalCustomersCount = state.totalCustomers,
                    lowStockCount = state.lowStockCount
                )
            }
        }

        // Recent Activity Section
        item {
            AnimatedCard(delayMillis = 200) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (state.recentInvoices.isEmpty()) {
                        RecentInvoiceCard(
                            invoiceNumber = "INV-2026-001",
                            customerName = "Apex Retail Outlets",
                            amount = "${state.currencySymbol}450.00",
                            status = "PAID",
                            dateText = "Today, 10:30 AM",
                            onClick = onNavigateToInvoices
                        )
                        RecentInvoiceCard(
                            invoiceNumber = "INV-2026-002",
                            customerName = "Global Logistics Ltd",
                            amount = "${state.currencySymbol}1,200.00",
                            status = "PENDING",
                            dateText = "Yesterday, 04:15 PM",
                            onClick = onNavigateToInvoices
                        )
                    } else {
                        state.recentInvoices.forEach { invoice ->
                            RecentInvoiceCard(
                                invoiceNumber = invoice.invoiceNumber,
                                customerName = invoice.customerName,
                                amount = "${state.currencySymbol}${String.format("%.2f", invoice.totalAmount)}",
                                status = invoice.status,
                                dateText = "Recent",
                                onClick = onNavigateToInvoices
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}
