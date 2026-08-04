package com.example.features.suppliers.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.core.database.entity.SupplierEntity

@Composable
fun AddEditSupplierDialog(
    supplier: SupplierEntity?,
    onDismiss: () -> Unit,
    onSave: (SupplierEntity) -> Unit
) {
    val isEdit = supplier != null

    var supplierName by remember { mutableStateOf(supplier?.supplierName ?: "") }
    var businessName by remember { mutableStateOf(supplier?.businessName ?: "") }
    var phone by remember { mutableStateOf(supplier?.phone ?: "") }
    var email by remember { mutableStateOf(supplier?.email ?: "") }
    var gstNumber by remember { mutableStateOf(supplier?.gstNumber ?: "") }
    var panNumber by remember { mutableStateOf(supplier?.panNumber ?: "") }
    var billingAddress by remember { mutableStateOf(supplier?.billingAddress ?: "") }
    var city by remember { mutableStateOf(supplier?.city ?: "") }
    var state by remember { mutableStateOf(supplier?.state ?: "") }
    var pincode by remember { mutableStateOf(supplier?.pincode ?: "") }
    var openingBalance by remember { mutableStateOf(supplier?.openingBalance?.toString() ?: "0.0") }
    var paymentTerms by remember { mutableStateOf(supplier?.paymentTerms ?: "Net 30") }
    var notes by remember { mutableStateOf(supplier?.notes ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isEdit) "Edit Supplier" else "Add New Supplier",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = supplierName,
                    onValueChange = {
                        supplierName = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Supplier Name *") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_supplier_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business / Company Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_supplier_company")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = it.isBlank()
                        },
                        label = { Text("Phone Number *") },
                        isError = phoneError,
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_supplier_phone")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_supplier_email")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gstNumber,
                        onValueChange = { gstNumber = it },
                        label = { Text("GST Number") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_supplier_gst")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = panNumber,
                        onValueChange = { panNumber = it },
                        label = { Text("PAN Number") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_supplier_pan")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = billingAddress,
                    onValueChange = { billingAddress = it },
                    label = { Text("Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_supplier_address")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = pincode,
                        onValueChange = { pincode = it },
                        label = { Text("Pincode / ZIP") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = openingBalance,
                        onValueChange = { openingBalance = it },
                        label = { Text("Opening Balance ($)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = paymentTerms,
                    onValueChange = { paymentTerms = it },
                    label = { Text("Payment Terms (e.g. Net 30, COD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Internal Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_cancel_supplier")
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (supplierName.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            if (phone.isBlank()) {
                                phoneError = true
                                return@Button
                            }

                            val openBal = openingBalance.toDoubleOrNull() ?: 0.0

                            val entity = supplier?.copy(
                                supplierName = supplierName,
                                businessName = businessName,
                                phone = phone,
                                email = email,
                                gstNumber = gstNumber,
                                panNumber = panNumber,
                                billingAddress = billingAddress,
                                city = city,
                                state = state,
                                pincode = pincode,
                                openingBalance = openBal,
                                paymentTerms = paymentTerms,
                                notes = notes,
                                updatedDate = System.currentTimeMillis()
                            ) ?: SupplierEntity(
                                supplierCode = "",
                                supplierName = supplierName,
                                businessName = businessName,
                                phone = phone,
                                email = email,
                                gstNumber = gstNumber,
                                panNumber = panNumber,
                                billingAddress = billingAddress,
                                city = city,
                                state = state,
                                pincode = pincode,
                                openingBalance = openBal,
                                outstandingBalance = openBal,
                                paymentTerms = paymentTerms,
                                notes = notes
                            )
                            onSave(entity)
                        },
                        modifier = Modifier.testTag("btn_save_supplier")
                    ) {
                        Text(if (isEdit) "Update Supplier" else "Save Supplier")
                    }
                }
            }
        }
    }
}
