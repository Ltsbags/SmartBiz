package com.example.features.cashbook.components

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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.example.core.theme.Spacing
import com.example.shared.buttons.PrimaryButton
import com.example.shared.buttons.SecondaryButton
import com.example.shared.forms.SmartBizTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddManualCashEntryDialog(
    onDismiss: () -> Unit,
    onSave: (entryType: String, amount: Double, entityName: String, description: String, paymentMode: String) -> Unit,
    currencySymbol: String = "$"
) {
    var entryType by remember { mutableStateOf("CASH_IN") } // CASH_IN or CASH_OUT
    var amountText by remember { mutableStateOf("") }
    var entityName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("CASH") }

    var modeExpanded by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val paymentModes = listOf("CASH", "BANK", "UPI", "CARD", "CHEQUE")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Cash Entry",
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
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                // Cash In / Cash Out Toggle
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = entryType == "CASH_IN",
                        onClick = { entryType = "CASH_IN" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Cash In (+)")
                    }
                    SegmentedButton(
                        selected = entryType == "CASH_OUT",
                        onClick = { entryType = "CASH_OUT" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Cash Out (-)")
                    }
                }

                SmartBizTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Amount ($currencySymbol)",
                    placeholder = "0.00",
                    testTag = "input_cash_amount"
                )

                SmartBizTextField(
                    value = entityName,
                    onValueChange = { entityName = it },
                    label = "Person / Account / Ref",
                    placeholder = "e.g. Owner Capital / Petty Cash Drawer",
                    testTag = "input_cash_entity"
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
                    value = description,
                    onValueChange = { description = it },
                    label = "Description / Reason",
                    placeholder = "e.g. Opening cash injection into drawer",
                    singleLine = false,
                    maxLines = 3,
                    testTag = "input_cash_desc"
                )
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Save Entry",
                onClick = {
                    onSave(
                        entryType,
                        amount,
                        entityName.ifBlank { if (entryType == "CASH_IN") "Cash Inflow" else "Cash Outflow" },
                        description,
                        paymentMode
                    )
                },
                enabled = amount > 0,
                modifier = Modifier.testTag("btn_save_cash_entry")
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
