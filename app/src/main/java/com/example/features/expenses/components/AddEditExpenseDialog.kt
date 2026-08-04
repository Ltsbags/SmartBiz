package com.example.features.expenses.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.entity.ExpenseCategoryEntity
import com.example.core.database.entity.ExpenseEntity
import com.example.core.theme.Spacing
import com.example.shared.buttons.PrimaryButton
import com.example.shared.buttons.SecondaryButton
import com.example.shared.forms.SmartBizDatePickerField
import com.example.shared.forms.SmartBizTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseDialog(
    expenseToEdit: ExpenseEntity?,
    categories: List<ExpenseCategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit,
    currencySymbol: String = "$"
) {
    var expenseNumber by remember { mutableStateOf(expenseToEdit?.expenseNumber ?: "") }
    var expenseDate by remember { mutableStateOf(expenseToEdit?.expenseDate ?: System.currentTimeMillis()) }
    var selectedCategory by remember { mutableStateOf(categories.find { it.id == expenseToEdit?.categoryId } ?: categories.firstOrNull()) }
    var amountText by remember { mutableStateOf(expenseToEdit?.amount?.toString() ?: "") }
    var taxText by remember { mutableStateOf(expenseToEdit?.taxAmount?.toString() ?: "0.0") }
    var paymentMode by remember { mutableStateOf(expenseToEdit?.paymentMode ?: "CASH") }
    var paymentStatus by remember { mutableStateOf(expenseToEdit?.paymentStatus ?: "PAID") }
    var paidAmountText by remember { mutableStateOf(expenseToEdit?.paidAmount?.toString() ?: "") }
    var referenceNumber by remember { mutableStateOf(expenseToEdit?.referenceNumber ?: "") }
    var payeeName by remember { mutableStateOf(expenseToEdit?.payeeName ?: "") }
    var notes by remember { mutableStateOf(expenseToEdit?.notes ?: "") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var modeExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val tax = taxText.toDoubleOrNull() ?: 0.0
    val totalAmount = amount + tax

    val paymentModes = listOf("CASH", "BANK_TRANSFER", "UPI", "CREDIT_CARD", "CHEQUE")
    val paymentStatuses = listOf("PAID", "UNPAID", "PARTIAL")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expenseToEdit == null) "Add New Expense" else "Edit Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                SmartBizTextField(
                    value = payeeName,
                    onValueChange = { payeeName = it },
                    label = "Payee / Vendor Name",
                    placeholder = "e.g. Commercial Properties Ltd",
                    testTag = "input_expense_payee"
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expense Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    SmartBizTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            if (paymentStatus == "PAID") {
                                paidAmountText = (it.toDoubleOrNull() ?: 0.0).toString()
                            }
                        },
                        label = "Amount ($currencySymbol)",
                        placeholder = "0.00",
                        modifier = Modifier.weight(1f),
                        testTag = "input_expense_amount"
                    )

                    SmartBizTextField(
                        value = taxText,
                        onValueChange = { taxText = it },
                        label = "Tax / GST ($currencySymbol)",
                        placeholder = "0.00",
                        modifier = Modifier.weight(1f),
                        testTag = "input_expense_tax"
                    )
                }

                Text(
                    text = "Total Expense: $currencySymbol${String.format("%.2f", totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    // Payment Mode
                    ExposedDropdownMenuBox(
                        expanded = modeExpanded,
                        onExpandedChange = { modeExpanded = !modeExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = paymentMode,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Payment Mode") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = modeExpanded,
                            onDismissRequest = { modeExpanded = false }
                        ) {
                            paymentModes.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode) },
                                    onClick = {
                                        paymentMode = mode
                                        modeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Payment Status
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = paymentStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            paymentStatuses.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = {
                                        paymentStatus = status
                                        if (status == "PAID") {
                                            paidAmountText = totalAmount.toString()
                                        } else if (status == "UNPAID") {
                                            paidAmountText = "0.0"
                                        }
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (paymentStatus == "PARTIAL") {
                    SmartBizTextField(
                        value = paidAmountText,
                        onValueChange = { paidAmountText = it },
                        label = "Amount Paid Now ($currencySymbol)",
                        placeholder = "0.00",
                        testTag = "input_expense_paid_amount"
                    )
                }

                SmartBizTextField(
                    value = referenceNumber,
                    onValueChange = { referenceNumber = it },
                    label = "Ref / Bill / Receipt No.",
                    placeholder = "e.g. REC-1029",
                    testTag = "input_expense_ref"
                )

                val dateText = remember(expenseDate) {
                    java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(expenseDate))
                }
                val context = androidx.compose.ui.platform.LocalContext.current
                SmartBizDatePickerField(
                    label = "Expense Date",
                    selectedDateText = dateText,
                    onClick = {
                        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = expenseDate }
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val newCal = java.util.Calendar.getInstance().apply {
                                    set(java.util.Calendar.YEAR, year)
                                    set(java.util.Calendar.MONTH, month)
                                    set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                expenseDate = newCal.timeInMillis
                            },
                            calendar.get(java.util.Calendar.YEAR),
                            calendar.get(java.util.Calendar.MONTH),
                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                )

                SmartBizTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes / Description",
                    placeholder = "Additional expense details...",
                    singleLine = false,
                    maxLines = 3,
                    testTag = "input_expense_notes"
                )
            }
        },
        confirmButton = {
            PrimaryButton(
                text = if (expenseToEdit == null) "Save Expense" else "Update Expense",
                onClick = {
                    val cat = selectedCategory ?: categories.firstOrNull() ?: return@PrimaryButton
                    val finalPaidAmount = when (paymentStatus) {
                        "PAID" -> totalAmount
                        "UNPAID" -> 0.0
                        else -> paidAmountText.toDoubleOrNull() ?: 0.0
                    }

                    val newExpense = ExpenseEntity(
                        id = expenseToEdit?.id ?: 0L,
                        expenseNumber = expenseNumber,
                        expenseDate = expenseDate,
                        categoryId = cat.id,
                        categoryName = cat.name,
                        amount = amount,
                        taxAmount = tax,
                        totalAmount = totalAmount,
                        paymentMode = paymentMode,
                        paymentStatus = paymentStatus,
                        paidAmount = finalPaidAmount,
                        referenceNumber = referenceNumber,
                        payeeName = payeeName,
                        notes = notes,
                        createdDate = expenseToEdit?.createdDate ?: System.currentTimeMillis(),
                        updatedDate = System.currentTimeMillis()
                    )
                    onSave(newExpense)
                },
                enabled = amount > 0,
                modifier = Modifier.testTag("btn_save_expense")
            )
        },
        dismissButton = {
            SecondaryButton(
                text = "Cancel",
                onClick = onDismiss
            )
        }
    )
}
