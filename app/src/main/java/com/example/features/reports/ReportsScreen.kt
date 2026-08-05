package com.example.features.reports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.features.reports.components.CustomerReportView
import com.example.features.reports.components.FinancialReportView
import com.example.features.reports.components.GstSummaryView
import com.example.features.reports.components.InventoryReportView
import com.example.features.reports.components.PurchaseReportView
import com.example.features.reports.components.SalesReportView
import com.example.features.reports.components.SupplierReportView
import com.example.features.reports.models.ReportExportType
import com.example.features.reports.widgets.DateRangeSelector
import com.example.shared.widgets.PageHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            PageHeader(
                title = "Analytics & Reports",
                subtitle = "Comprehensive business analytics, sales trends & tax summaries",
                actionIcon = Icons.Default.Refresh,
                onActionClick = { viewModel.loadReportData() }
            )

            DateRangeSelector(
                selectedOption = state.selectedDateOption,
                onOptionSelected = { option, start, end ->
                    viewModel.onDateFilterSelected(option, start, end)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Search and Export controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search records...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = { viewModel.triggerExport(ReportExportType.PDF) }
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF")
                }

                OutlinedButton(
                    onClick = { viewModel.triggerExport(ReportExportType.EXCEL) }
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CSV")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Tab Row
            val tabs = listOf(
                "Sales",
                "Purchases",
                "Inventory",
                "Customers",
                "Suppliers",
                "Financials",
                "GST"
            )

            PrimaryTabRow(
                selectedTabIndex = state.selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    when (state.selectedTab) {
                        0 -> SalesReportView(data = state.salesData, searchQuery = state.searchQuery)
                        1 -> PurchaseReportView(data = state.purchaseData, searchQuery = state.searchQuery)
                        2 -> InventoryReportView(data = state.inventoryData, searchQuery = state.searchQuery)
                        3 -> CustomerReportView(data = state.customerData, searchQuery = state.searchQuery)
                        4 -> SupplierReportView(data = state.supplierData, searchQuery = state.searchQuery)
                        5 -> FinancialReportView(data = state.financialData)
                        6 -> GstSummaryView(data = state.gstData)
                    }
                }
            }
        }
    }
}
