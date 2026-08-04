package com.example.features.invoice

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.database.entity.InvoiceWithItems
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing
import com.example.core.utils.InvoicePdfGenerator
import com.example.features.invoice.components.AddEditInvoiceDialog
import com.example.features.invoice.components.InvoiceCard
import com.example.features.invoice.components.InvoiceDetailsDialog
import com.example.features.invoice.components.InvoiceFilterBottomSheet
import com.example.features.invoice.components.InvoiceSortBottomSheet
import com.example.features.invoice.components.InvoiceSummaryCard
import com.example.shared.components.SmartBizTopAppBar
import com.example.shared.dialogs.ConfirmationDialog
import com.example.shared.widgets.EmptyStateView

@Composable
fun InvoiceScreen(
    viewModel: InvoiceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val nextNumber by viewModel.nextInvoiceNumber.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    var invoiceToDelete by remember { mutableStateOf<InvoiceWithItems?>(null) }
    var invoiceToCancel by remember { mutableStateOf<InvoiceWithItems?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            SmartBizTopAppBar(title = "Invoices & Billing")
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateInvoice() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_invoice")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Invoice"
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.m)
        ) {
            Spacer(modifier = Modifier.height(Spacing.xs))

            // Summary Metric Header
            InvoiceSummaryCard(
                totalRevenue = state.totalRevenue,
                pendingAmount = state.pendingAmount,
                totalCount = state.totalInvoicesCount,
                currencySymbol = state.currencySymbol
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            // Search Bar + Filter + Sort Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search invoice #, customer name...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(Dimensions.buttonCornerRadius),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("invoice_search_input")
                )

                Spacer(modifier = Modifier.width(Spacing.xs))

                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier.testTag("btn_invoice_filter")
                ) {
                    Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter Invoices")
                }

                IconButton(
                    onClick = { showSortSheet = true },
                    modifier = Modifier.testTag("btn_invoice_sort")
                ) {
                    Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort Invoices")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            if (state.invoices.isEmpty()) {
                EmptyStateView(
                    title = "No Invoices Found",
                    description = if (state.searchQuery.isNotEmpty()) "No results matching '${state.searchQuery}'."
                    else "Create your first commercial billing invoice using the '+' button.",
                    icon = Icons.Default.Receipt
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.m),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.invoices, key = { it.invoice.id }) { invoiceWithItems ->
                        InvoiceCard(
                            invoiceWithItems = invoiceWithItems,
                            currencySymbol = state.currencySymbol,
                            onClick = { viewModel.selectInvoiceForDetails(invoiceWithItems) },
                            onPrint = {
                                val pdfFile = InvoicePdfGenerator.generatePdf(
                                    context,
                                    invoiceWithItems,
                                    currencySymbol = state.currencySymbol
                                )
                                if (pdfFile != null) {
                                    InvoicePdfGenerator.printPdf(context, pdfFile)
                                } else {
                                    Toast.makeText(context, "Error generating PDF for print", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onShare = {
                                val pdfFile = InvoicePdfGenerator.generatePdf(
                                    context,
                                    invoiceWithItems,
                                    currencySymbol = state.currencySymbol
                                )
                                if (pdfFile != null) {
                                    InvoicePdfGenerator.sharePdf(context, pdfFile)
                                } else {
                                    Toast.makeText(context, "Error generating PDF for sharing", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDuplicate = { viewModel.duplicateInvoice(invoiceWithItems.invoice.id) },
                            onEdit = { viewModel.selectInvoiceForEdit(invoiceWithItems) },
                            onCancel = { invoiceToCancel = invoiceWithItems },
                            onDelete = { invoiceToDelete = invoiceWithItems }
                        )
                    }
                }
            }
        }
    }

    // Dialogs & Sheets

    if (showFilterSheet) {
        InvoiceFilterBottomSheet(
            currentFilterState = state.filterState,
            onApplyFilter = { newFilter ->
                viewModel.updateFilterState(newFilter)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showSortSheet) {
        InvoiceSortBottomSheet(
            currentSortOption = state.sortOption,
            onSelectSortOption = { newSort ->
                viewModel.updateSortOption(newSort)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false }
        )
    }

    // Create / Edit Invoice Dialog
    if (state.isCreateInvoiceOpen || state.selectedInvoiceForEdit != null) {
        AddEditInvoiceDialog(
            initialInvoiceWithItems = state.selectedInvoiceForEdit,
            defaultInvoiceNumber = nextNumber,
            customers = state.customers,
            products = state.products,
            currencySymbol = state.currencySymbol,
            onSave = { entity, items, isComplete ->
                viewModel.saveInvoice(entity, items, isComplete)
            },
            onDismiss = {
                viewModel.closeCreateInvoice()
                viewModel.selectInvoiceForEdit(null)
            }
        )
    }

    // Invoice Details Dialog
    state.selectedInvoiceForDetails?.let { detailInv ->
        InvoiceDetailsDialog(
            invoiceWithItems = detailInv,
            currencySymbol = state.currencySymbol,
            onPrint = {
                val pdfFile = InvoicePdfGenerator.generatePdf(context, detailInv, currencySymbol = state.currencySymbol)
                if (pdfFile != null) InvoicePdfGenerator.printPdf(context, pdfFile)
            },
            onShare = {
                val pdfFile = InvoicePdfGenerator.generatePdf(context, detailInv, currencySymbol = state.currencySymbol)
                if (pdfFile != null) InvoicePdfGenerator.sharePdf(context, pdfFile)
            },
            onDuplicate = {
                viewModel.duplicateInvoice(detailInv.invoice.id)
                viewModel.selectInvoiceForDetails(null)
            },
            onEdit = {
                viewModel.selectInvoiceForEdit(detailInv)
                viewModel.selectInvoiceForDetails(null)
            },
            onCancel = {
                invoiceToCancel = detailInv
            },
            onDelete = {
                invoiceToDelete = detailInv
            },
            onDismiss = { viewModel.selectInvoiceForDetails(null) }
        )
    }

    // Delete Confirmation
    invoiceToDelete?.let { inv ->
        ConfirmationDialog(
            title = "Delete Invoice",
            message = "Are you sure you want to delete invoice #${inv.invoice.invoiceNumber}? This action cannot be undone.",
            confirmText = "Delete",
            dismissText = "Cancel",
            onConfirm = {
                viewModel.deleteInvoice(inv.invoice.id)
                invoiceToDelete = null
            },
            onDismiss = { invoiceToDelete = null }
        )
    }

    // Cancel Confirmation
    invoiceToCancel?.let { inv ->
        ConfirmationDialog(
            title = "Cancel Invoice",
            message = "Are you sure you want to cancel invoice #${inv.invoice.invoiceNumber}? If completed, this will restore stock quantities and adjust customer ledger balance.",
            confirmText = "Cancel Invoice",
            dismissText = "Dismiss",
            onConfirm = {
                viewModel.cancelInvoice(inv.invoice.id)
                invoiceToCancel = null
            },
            onDismiss = { invoiceToCancel = null }
        )
    }
}
