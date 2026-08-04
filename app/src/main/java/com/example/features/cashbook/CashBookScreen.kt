package com.example.features.cashbook

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
import androidx.compose.material.icons.filled.AccountBalance
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
import com.example.features.cashbook.components.AddManualCashEntryDialog
import com.example.features.cashbook.components.CashBookEntryCard
import com.example.features.cashbook.components.CashBookSummaryCard
import com.example.shared.forms.SmartBizSearchField
import com.example.shared.widgets.EmptyStateWidget
import com.example.shared.widgets.PageHeader
import com.example.shared.widgets.ShimmerLoadingWidget

@Composable
fun CashBookScreen(
    viewModel: CashBookViewModel,
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

    val typeFilters = listOf(
        Pair("ALL", "All Entries"),
        Pair("CASH_IN", "Cash In"),
        Pair("CASH_OUT", "Cash Out")
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.l)
        ) {
            Spacer(modifier = Modifier.height(Spacing.m))

            PageHeader(
                title = "Cash Book",
                subtitle = "Real-time ledger of store cash flow & payment transactions"
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            SmartBizSearchField(
                query = state.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Search ref no, entity name, or source...",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            // Type Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                typeFilters.forEach { filter ->
                    FilterChip(
                        selected = state.typeFilter == filter.first,
                        onClick = { viewModel.onTypeFilterChanged(filter.first) },
                        label = { Text(filter.second) }
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
                        CashBookSummaryCard(
                            totalCashIn = state.totalCashIn,
                            totalCashOut = state.totalCashOut,
                            netBalance = state.netCashBalance
                        )
                    }

                    if (state.filteredEntries.isEmpty()) {
                        item {
                            EmptyStateWidget(
                                title = "No Cash Entries Found",
                                description = "Click the '+' button to record a manual cash adjustment or drawer deposit.",
                                icon = Icons.Default.AccountBalance
                            )
                        }
                    } else {
                        items(state.filteredEntries) { entry ->
                            CashBookEntryCard(entry = entry)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.toggleAddDialog(true) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.l)
                .testTag("fab_add_cash_entry")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Cash Entry")
        }
    }

    if (state.showAddDialog) {
        AddManualCashEntryDialog(
            onDismiss = { viewModel.toggleAddDialog(false) },
            onSave = { type, amount, entity, desc, mode ->
                viewModel.addManualEntry(type, amount, entity, desc, mode)
            }
        )
    }
}
