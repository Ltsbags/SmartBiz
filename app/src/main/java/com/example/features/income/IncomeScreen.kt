package com.example.features.income

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.theme.Spacing
import com.example.features.income.components.AddEditIncomeDialog
import com.example.features.income.components.IncomeCard
import com.example.features.income.components.IncomeDetailsDialog
import com.example.features.income.components.IncomeSummaryCard
import com.example.shared.forms.SmartBizSearchField
import com.example.shared.widgets.EmptyStateWidget
import com.example.shared.widgets.PageHeader
import com.example.shared.widgets.ShimmerLoadingWidget

@Composable
fun IncomeScreen(
    viewModel: IncomeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    val categories = listOf("ALL", "Sales Revenue", "Consulting Services", "Interest Income", "Rental Income", "Other Income")

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.l)
        ) {
            Spacer(modifier = Modifier.height(Spacing.m))

            PageHeader(
                title = "Income Tracker",
                subtitle = "Log sales revenue, consulting & other income streams"
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            SmartBizSearchField(
                query = state.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Search income no, payer or notes...",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = state.categoryFilter == cat,
                        onClick = { viewModel.onCategoryFilterSelected(cat) },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            if (state.isLoading) {
                ShimmerLoadingWidget()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.m),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        IncomeSummaryCard(
                            totalAmount = state.totalIncomeAmount,
                            incomeCount = state.totalIncomeCount
                        )
                    }

                    if (state.filteredIncome.isEmpty()) {
                        item {
                            EmptyStateWidget(
                                title = "No Income Entries",
                                description = "Click the '+' button to log a new revenue or income entry.",
                                icon = Icons.Default.TrendingUp
                            )
                        }
                    } else {
                        items(state.filteredIncome) { income ->
                            IncomeCard(
                                income = income,
                                onClick = { viewModel.onIncomeSelected(income) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.onAddIncomeClicked() },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.l)
                .testTag("fab_add_income")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Record Income")
        }
    }

    if (state.showAddEditDialog) {
        AddEditIncomeDialog(
            incomeToEdit = state.incomeToEdit,
            customers = state.customers,
            onDismiss = { viewModel.dismissAddEditDialog() },
            onSave = { viewModel.saveIncome(it) }
        )
    }

    if (state.showDetailsDialog && state.selectedIncome != null) {
        IncomeDetailsDialog(
            income = state.selectedIncome!!,
            onDismiss = { viewModel.dismissDetailsDialog() },
            onEdit = {
                val inc = state.selectedIncome!!
                viewModel.dismissDetailsDialog()
                viewModel.onEditIncomeClicked(inc)
            },
            onDelete = { viewModel.deleteIncome(state.selectedIncome!!) }
        )
    }
}
