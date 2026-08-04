package com.example.features.expenses

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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.theme.Spacing
import com.example.features.expenses.components.AddEditExpenseDialog
import com.example.features.expenses.components.CategoryManagementDialog
import com.example.features.expenses.components.ExpenseCard
import com.example.features.expenses.components.ExpenseDetailsDialog
import com.example.features.expenses.components.ExpenseSummaryCard
import com.example.shared.forms.SmartBizSearchField
import com.example.shared.widgets.EmptyStateWidget
import com.example.shared.widgets.PageHeader
import com.example.shared.widgets.ShimmerLoadingWidget

@Composable
fun ExpenseScreen(
    viewModel: ExpenseViewModel,
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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.l)
        ) {
            Spacer(modifier = Modifier.height(Spacing.m))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageHeader(
                    title = "Expense Tracker",
                    subtitle = "Manage operational bills & vendor overheads"
                )
                IconButton(onClick = { viewModel.toggleCategoryDialog(true) }) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Categories",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            SmartBizSearchField(
                query = state.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Search expense no, payee, or notes...",
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
                FilterChip(
                    selected = state.filterState.categoryId == null,
                    onClick = { viewModel.onCategoryFilterSelected(null) },
                    label = { Text("All Categories") }
                )
                state.categories.forEach { cat ->
                    FilterChip(
                        selected = state.filterState.categoryId == cat.id,
                        onClick = { viewModel.onCategoryFilterSelected(cat.id) },
                        label = { Text(cat.name) }
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
                        ExpenseSummaryCard(
                            totalAmount = state.totalExpensesAmount,
                            paidAmount = state.totalPaidAmount,
                            pendingAmount = state.totalPendingAmount,
                            expenseCount = state.totalExpensesCount
                        )
                    }

                    if (state.filteredExpenses.isEmpty()) {
                        item {
                            EmptyStateWidget(
                                title = "No Expenses Found",
                                description = "Click the '+' button to log a new store expense or bill.",
                                icon = Icons.Default.Receipt
                            )
                        }
                    } else {
                        items(state.filteredExpenses) { expense ->
                            ExpenseCard(
                                expense = expense,
                                onClick = { viewModel.onExpenseSelected(expense) }
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
            onClick = { viewModel.onAddExpenseClicked() },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.l)
                .testTag("fab_add_expense")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense")
        }
    }

    if (state.showAddEditDialog) {
        AddEditExpenseDialog(
            expenseToEdit = state.expenseToEdit,
            categories = state.categories,
            onDismiss = { viewModel.dismissAddEditDialog() },
            onSave = { viewModel.saveExpense(it) }
        )
    }

    if (state.showDetailsDialog && state.selectedExpense != null) {
        ExpenseDetailsDialog(
            expense = state.selectedExpense!!,
            onDismiss = { viewModel.dismissDetailsDialog() },
            onEdit = {
                val exp = state.selectedExpense!!
                viewModel.dismissDetailsDialog()
                viewModel.onEditExpenseClicked(exp)
            },
            onDelete = { viewModel.deleteExpense(state.selectedExpense!!) }
        )
    }

    if (state.showCategoryDialog) {
        CategoryManagementDialog(
            categories = state.categories,
            onDismiss = { viewModel.toggleCategoryDialog(false) },
            onAddCategory = { viewModel.saveCategory(it) },
            onDeleteCategory = { viewModel.deleteCategory(it) }
        )
    }
}
