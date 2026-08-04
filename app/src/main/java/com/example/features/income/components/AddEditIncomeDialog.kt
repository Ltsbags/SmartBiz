package com.example.features.income.components

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
import com.example.core.database.entity.CustomerEntity
import com.example.core.database.entity.IncomeEntity
import com.example.core.theme.Spacing
import com.example.shared.buttons.PrimaryButton
import com.example.shared.buttons.SecondaryButton
import com.example.shared.forms.SmartBizDatePickerField
import com.example.shared.forms.SmartBizTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditIncomeDialog(
    incomeToEdit: IncomeEntity?,
    customers: List<CustomerEntity>,
    onDismiss: () -> Unit,
    onSave: (IncomeEntity) -> Unit,
    currencySymbol: String = "$"
) {
    var incomeNumber by remember { mutableStateOf(incomeToEdit?.incomeNumber ?: "") }
    var incomeDate by remember { mutableStateOf(incomeToEdit?.incomeDate ?: System.currentTimeMillis()) }
    var category by remember { mutableStateOf(incomeToEdit?.category ?: "Consulting Services") }
    var selectedCustomer by remember { mutableStateOf(customers.find { it.id == incomeToEdit?.customerId }) }
    var customerNameText by remember { mutableStateOf(incomeToEdit?.customerName ?: "") }
    var amountText by remember { mutableStateOf(incomeToEdit?.amount?.toString() ?: "") }
    var paymentMode by remember { mutableStateOf(incomeToEdit?.paymentMode ?: "BANK_TRANSFER") }
    var referenceNumber by remember { mutableStateOf(incomeToEdit?.referenceNumber ?: "") }
    var notes by remember { mutableStateOf(incomeToEdit?.notes ?: "") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var customerExpanded by remember { mutableStateOf(false) }
    var modeExpanded by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull() ?: 0.0

    val categories = listOf("Sales Revenue", "Consulting Services", "Interest Income", "Rental Income", "Other Income")
    val paymentModes = listOf("CASH", "BANK_TRANSFER", "UPI", "CHEQUE", "CARD")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (incomeToEdit == null) "Record Income" else "Edit Income",
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
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Income Category") },
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
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Optional Customer Dropdown
                ExposedDropdownMenuBox(
                    expanded = customerExpanded,
                    onExpandedChange = { customerExpanded = !customerExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCustomer?.name ?: customerNameText.ifBlank { "Select Customer (Optional)" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Customer / Payer") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = customerExpanded,
                        onDismissRequest = { customerExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (Direct Income)") },
                            onClick = {
                                selectedCustomer = null
                                customerNameText = ""
                                customerExpanded = false
                            }
                        )
                        customers.forEach { cust ->
                            DropdownMenuItem(
                                text = { Text(cust.name) },
                                onClick = {
                                    selectedCustomer = cust
                                    customerNameText = cust.name
                                    customerExpanded = false
                                }
                            )
                        }
                    }
                }

                SmartBizTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Income Amount ($currencySymbol)",
                    placeholder = "0.00",
                    testTag = "input_income_amount"
                )

                // Payment Mode Dropdown
                ExposedDropdownMenuBox(
                    expanded = modeExpanded,
                    onExpandedChange = { modeExpanded = !modeExpanded }
                ) {
                    OutlinedTextField(
                        value = paymentMode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Mode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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

                SmartBizTextField(
                    value = referenceNumber,
                    onValueChange = { referenceNumber = it },
                    label = "Reference / Transaction No.",
                    placeholder = "e.g. NEFT-99210",
                    testTag = "input_income_ref"
                )

                val dateText = remember(incomeDate) {
                    java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(incomeDate))
                }
                val context = androidx.compose.ui.platform.LocalContext.current
                SmartBizDatePickerField(
                    label = "Income Date",
                    selectedDateText = dateText,
                    onClick = {
                        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = incomeDate }
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val newCal = java.util.Calendar.getInstance().apply {
                                    set(java.util.Calendar.YEAR, year)
                                    set(java.util.Calendar.MONTH, month)
                                    set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                incomeDate = newCal.timeInMillis
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
                    placeholder = "Additional income description...",
                    singleLine = false,
                    maxLines = 3,
                    testTag = "input_income_notes"
                )
            }
        },
        confirmButton = {
            PrimaryButton(
                text = if (incomeToEdit == null) "Record Income" else "Update Income",
                onClick = {
                    val newIncome = IncomeEntity(
                        id = incomeToEdit?.id ?: 0L,
                        incomeNumber = incomeNumber,
                        incomeDate = incomeDate,
                        category = category,
                        customerId = selectedCustomer?.id,
                        customerName = selectedCustomer?.name ?: customerNameText,
                        amount = amount,
                        paymentMode = paymentMode,
                        referenceNumber = referenceNumber,
                        notes = notes,
                        createdDate = incomeToEdit?.createdDate ?: System.currentTimeMillis(),
                        updatedDate = System.currentTimeMillis()
                    )
                    onSave(newIncome)
                },
                enabled = amount > 0,
                modifier = Modifier.testTag("btn_save_income")
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
