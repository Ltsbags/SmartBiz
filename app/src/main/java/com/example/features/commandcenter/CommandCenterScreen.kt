package com.example.features.commandcenter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shared.ui.components.dashboard.ActivityTimelineCard
import com.example.shared.ui.components.dashboard.DashboardSection
import com.example.shared.ui.components.dashboard.DashboardWidgetCard
import com.example.shared.ui.components.dashboard.HealthScoreCard
import com.example.shared.ui.components.dashboard.InsightCard
import com.example.shared.ui.components.dashboard.QuickActionGrid
import com.example.shared.ui.components.dashboard.TaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandCenterScreen(
    viewModel: CommandCenterViewModel,
    onNavigateToNotifications: () -> Unit,
    onNavigateToActivityCenter: () -> Unit,
    onNavigateToTaskCenter: () -> Unit,
    onNavigateToBusinessHealth: () -> Unit,
    onNavigateToCustomizer: () -> Unit,
    onQuickActionNavigate: (String) -> Unit
) {
    val widgets by viewModel.activeWidgets.collectAsState()
    val metrics by viewModel.metricsState.collectAsState()
    val health by viewModel.latestHealth.collectAsState()
    val tasks by viewModel.pendingTasks.collectAsState()
    val activities by viewModel.recentActivities.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = metrics.businessName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enterprise Command Center",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onNavigateToCustomizer) {
                        Icon(Icons.Default.Edit, contentDescription = "Customize Widgets")
                    }
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        if (metrics.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .testTag("command_center_lazy_column")
            ) {
                widgets.forEach { widget ->
                    item(key = widget.widgetKey) {
                        when (widget.widgetKey) {
                            "QUICK_ACTIONS" -> {
                                QuickActionGrid(
                                    onActionClick = onQuickActionNavigate,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            "BUSINESS_HEALTH" -> {
                                HealthScoreCard(
                                    health = health,
                                    onClick = onNavigateToBusinessHealth,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            "TODAYS_SALES" -> {
                                DashboardWidgetCard(
                                    title = widget.title,
                                    value = "${metrics.currencySymbol}${String.format("%.2f", metrics.todaySales)}",
                                    subtitle = "Real-time Sales Today",
                                    icon = Icons.Default.BarChart,
                                    isPinned = widget.isPinned,
                                    onPinToggle = { viewModel.togglePin(widget.widgetKey, !widget.isPinned) }
                                )
                            }
                            "TODAYS_PURCHASES" -> {
                                DashboardWidgetCard(
                                    title = widget.title,
                                    value = "${metrics.currencySymbol}${String.format("%.2f", metrics.todayPurchases)}",
                                    subtitle = "Procurement & Inward Expenses",
                                    icon = Icons.Default.BarChart,
                                    isPinned = widget.isPinned,
                                    onPinToggle = { viewModel.togglePin(widget.widgetKey, !widget.isPinned) }
                                )
                            }
                            "TODAYS_EXPENSES" -> {
                                DashboardWidgetCard(
                                    title = widget.title,
                                    value = "${metrics.currencySymbol}${String.format("%.2f", metrics.todayExpenses)}",
                                    subtitle = "Operational & Indirect Expenses",
                                    icon = Icons.Default.BarChart,
                                    isPinned = widget.isPinned,
                                    onPinToggle = { viewModel.togglePin(widget.widgetKey, !widget.isPinned) }
                                )
                            }
                            "TODAYS_INCOME" -> {
                                DashboardWidgetCard(
                                    title = widget.title,
                                    value = "${metrics.currencySymbol}${String.format("%.2f", metrics.todayIncome)}",
                                    subtitle = "Other Inward Business Revenue",
                                    icon = Icons.Default.BarChart,
                                    isPinned = widget.isPinned,
                                    onPinToggle = { viewModel.togglePin(widget.widgetKey, !widget.isPinned) }
                                )
                            }
                            "OUTSTANDING_RECEIVABLES" -> {
                                DashboardWidgetCard(
                                    title = widget.title,
                                    value = "${metrics.currencySymbol}${String.format("%.2f", metrics.outstandingReceivables)}",
                                    subtitle = "Pending Customer Credit Collections",
                                    icon = Icons.Default.BarChart,
                                    isPinned = widget.isPinned,
                                    onPinToggle = { viewModel.togglePin(widget.widgetKey, !widget.isPinned) }
                                )
                            }
                            "OUTSTANDING_PAYABLES" -> {
                                DashboardWidgetCard(
                                    title = widget.title,
                                    value = "${metrics.currencySymbol}${String.format("%.2f", metrics.outstandingPayables)}",
                                    subtitle = "Pending Supplier Dues",
                                    icon = Icons.Default.BarChart,
                                    isPinned = widget.isPinned,
                                    onPinToggle = { viewModel.togglePin(widget.widgetKey, !widget.isPinned) }
                                )
                            }
                            "INVENTORY_VALUE" -> {
                                DashboardWidgetCard(
                                    title = widget.title,
                                    value = "${metrics.currencySymbol}${String.format("%.2f", metrics.inventoryValue)}",
                                    subtitle = "${metrics.totalProducts} total catalog items",
                                    icon = Icons.Default.BarChart,
                                    isPinned = widget.isPinned,
                                    onPinToggle = { viewModel.togglePin(widget.widgetKey, !widget.isPinned) }
                                )
                            }
                            "LOW_STOCK" -> {
                                DashboardWidgetCard(
                                    title = widget.title,
                                    value = "${metrics.lowStockCount} Items",
                                    subtitle = "Items at or below min reorder point",
                                    icon = Icons.Default.BarChart,
                                    isPinned = widget.isPinned,
                                    onPinToggle = { viewModel.togglePin(widget.widgetKey, !widget.isPinned) }
                                )
                            }
                            "OUT_OF_STOCK" -> {
                                DashboardWidgetCard(
                                    title = widget.title,
                                    value = "${metrics.outOfStockCount} Items",
                                    subtitle = "Replenish immediately to avoid stockout",
                                    icon = Icons.Default.BarChart,
                                    isPinned = widget.isPinned,
                                    onPinToggle = { viewModel.togglePin(widget.widgetKey, !widget.isPinned) }
                                )
                            }
                            "PENDING_TASKS" -> {
                                DashboardSection(
                                    title = "Pending System Tasks (${tasks.size})",
                                    actionText = "View All",
                                    onActionClick = onNavigateToTaskCenter
                                ) {
                                    if (tasks.isEmpty()) {
                                        Text(
                                            text = "No pending tasks. All clear!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        tasks.take(3).forEach { task ->
                                            TaskCard(
                                                task = task,
                                                onComplete = { /* Handled in Task Center */ },
                                                onDelete = { /* Handled in Task Center */ }
                                            )
                                        }
                                    }
                                }
                            }
                            "RECENT_ACTIVITIES" -> {
                                DashboardSection(
                                    title = "Recent System Activities",
                                    actionText = "Timeline",
                                    onActionClick = onNavigateToActivityCenter
                                ) {
                                    if (activities.isEmpty()) {
                                        Text(
                                            text = "No recent activity recorded.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        activities.take(3).forEach { activity ->
                                            ActivityTimelineCard(item = activity)
                                        }
                                    }
                                }
                            }
                            "TOP_PRODUCTS" -> {
                                InsightCard(
                                    title = "Top Inventory Asset",
                                    insightText = "Highest inventory value product currently holds top asset share in inventory."
                                )
                            }
                            "TOP_CUSTOMERS" -> {
                                InsightCard(
                                    title = "Customer Intelligence",
                                    insightText = "Top active customers contribute over 60% of total revenue this month."
                                )
                            }
                            "TOP_SUPPLIERS" -> {
                                InsightCard(
                                    title = "Supplier Fulfillment",
                                    insightText = "Primary suppliers maintain 100% fulfill rate across recent procurement orders."
                                )
                            }
                            else -> {}
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
