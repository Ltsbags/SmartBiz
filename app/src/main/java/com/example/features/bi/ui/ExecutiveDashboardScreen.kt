package com.example.features.bi.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repositories.AppRepositoryProvider
import com.example.services.bi.ExecutiveDashboardSummary
import com.example.services.bi.KpiEvaluation
import com.example.services.bi.KpiStatus
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutiveDashboardScreen(
    onNavigateToCustomReports: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToForecasting: () -> Unit = {}
) {
    val repositoryProvider = AppRepositoryProvider.getInstance()
    val reportingRepo = repositoryProvider.reportingRepository
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedRangeFilter by remember { mutableStateOf("THIS_MONTH") }
    var summary by remember { mutableStateOf<ExecutiveDashboardSummary?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            isLoading = true
            val cal = Calendar.getInstance()
            val endDate = cal.timeInMillis

            val startDate = when (selectedRangeFilter) {
                "TODAY" -> {
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.timeInMillis
                }
                "THIS_WEEK" -> {
                    cal.add(Calendar.DAY_OF_YEAR, -7)
                    cal.timeInMillis
                }
                "THIS_MONTH" -> {
                    cal.add(Calendar.DAY_OF_YEAR, -30)
                    cal.timeInMillis
                }
                "THIS_QUARTER" -> {
                    cal.add(Calendar.DAY_OF_YEAR, -90)
                    cal.timeInMillis
                }
                else -> {
                    cal.add(Calendar.DAY_OF_YEAR, -30)
                    cal.timeInMillis
                }
            }

            summary = reportingRepo.getExecutiveSummary(startDate, endDate)
            isLoading = false
        }
    }

    LaunchedEffect(selectedRangeFilter) {
        loadData()
    }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Executive BI Dashboard", fontWeight = FontWeight.Bold)
                        Text(
                            "Enterprise Analytics & Real-Time KPIs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { loadData() },
                        modifier = Modifier.testTag("refresh_bi_dashboard_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Navigation Bar / Quick Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToCustomReports,
                    modifier = Modifier.weight(1f).testTag("nav_custom_reports_btn")
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Report Designer")
                }

                OutlinedButton(
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.weight(1f).testTag("nav_analytics_btn")
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Analytics")
                }

                Button(
                    onClick = onNavigateToForecasting,
                    modifier = Modifier.weight(1f).testTag("nav_forecasting_btn")
                ) {
                    Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Forecast")
                }
            }

            // Date Range Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ranges = listOf(
                    "TODAY" to "Today",
                    "THIS_WEEK" to "7 Days",
                    "THIS_MONTH" to "30 Days",
                    "THIS_QUARTER" to "90 Days"
                )
                ranges.forEach { (code, label) ->
                    FilterChip(
                        selected = selectedRangeFilter == code,
                        onClick = { selectedRangeFilter = code },
                        label = { Text(label) },
                        modifier = Modifier.testTag("filter_chip_$code")
                    )
                }
            }

            if (isLoading && summary == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.6f))
                }
            } else summary?.let { sum ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        // Key Metrics Cards Grid
                        Text(
                            text = "Executive Financial Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricSummaryCard(
                                title = "Revenue",
                                value = currencyFormat.format(sum.totalRevenue),
                                subtitle = "${sum.salesCount} Invoices",
                                color = MaterialTheme.colorScheme.primaryContainer,
                                icon = Icons.Default.AttachMoney,
                                modifier = Modifier.weight(1f)
                            )
                            MetricSummaryCard(
                                title = "Net Profit",
                                value = currencyFormat.format(sum.netProfit),
                                subtitle = "Margin: ${String.format("%.1f", sum.netProfitMarginPct)}%",
                                color = if (sum.netProfit >= 0) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer,
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricSummaryCard(
                                title = "Receivables",
                                value = currencyFormat.format(sum.outstandingReceivables),
                                subtitle = "Uncollected Invoices",
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                icon = Icons.Default.AccountBalance,
                                modifier = Modifier.weight(1f)
                            )
                            MetricSummaryCard(
                                title = "Payables",
                                value = currencyFormat.format(sum.outstandingPayables),
                                subtitle = "Supplier Dues",
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                icon = Icons.Default.MoneyOff,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        // Inventory & GST Banner
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Inventory Valuation", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        currencyFormat.format(sum.inventoryValuation),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Divider(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .width(1.dp)
                                )
                                Column {
                                    Text("Net GST Collected", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        currencyFormat.format(sum.gstNetCollected),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // KPI Evaluation Section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Enterprise KPI Targets & Thresholds",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    items(sum.kpiEvaluations) { eval ->
                        KpiCardItem(eval = eval)
                    }

                    // Branch Consolidation Section
                    if (sum.branchConsolidation.isNotEmpty()) {
                        item {
                            Text(
                                text = "Consolidated Multi-Branch Overview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(sum.branchConsolidation) { branch ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(branch.branchName, fontWeight = FontWeight.SemiBold)
                                            Text("Branch Code: ${branch.branchCode}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            currencyFormat.format(branch.salesAmount),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Net: ${currencyFormat.format(branch.netProfitAmount)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (branch.netProfitAmount >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
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
}

@Composable
fun MetricSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun KpiCardItem(eval: KpiEvaluation) {
    val statusColor = when (eval.status) {
        KpiStatus.TARGET_MET -> MaterialTheme.colorScheme.primary
        KpiStatus.NORMAL -> MaterialTheme.colorScheme.secondary
        KpiStatus.WARNING -> Color(0xFFFF9800)
        KpiStatus.CRITICAL -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(eval.kpi.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Badge(containerColor = statusColor) {
                    Text(
                        text = eval.status.name,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current: ${eval.currentValue}",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Target: ${eval.kpi.targetValue}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = (eval.progressPercentage / 100.0).toFloat().coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = eval.statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor
            )
        }
    }
}
