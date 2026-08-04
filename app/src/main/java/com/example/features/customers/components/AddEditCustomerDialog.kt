package com.example.features.customers.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.core.database.entity.CustomerEntity
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerDialog(
    customerToEdit: CustomerEntity? = null,
    onSave: (CustomerEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(customerToEdit?.name ?: "") }
    var company by remember { mutableStateOf(customerToEdit?.company ?: "") }
    var phone by remember { mutableStateOf(customerToEdit?.phone ?: "") }
    var alternateNumber by remember { mutableStateOf(customerToEdit?.alternateNumber ?: "") }
    var email by remember { mutableStateOf(customerToEdit?.email ?: "") }
    var gstNumber by remember { mutableStateOf(customerToEdit?.gstNumber ?: "") }
    var panNumber by remember { mutableStateOf(customerToEdit?.panNumber ?: "") }
    var customerType by remember { mutableStateOf(customerToEdit?.customerType ?: "Retail") }
    var billingAddress by remember { mutableStateOf(customerToEdit?.billingAddress ?: "") }
    var shippingAddress by remember { mutableStateOf(customerToEdit?.shippingAddress ?: "") }
    var city by remember { mutableStateOf(customerToEdit?.city ?: "") }
    var state by remember { mutableStateOf(customerToEdit?.state ?: "") }
    var pincode by remember { mutableStateOf(customerToEdit?.pincode ?: "") }
    var openingBalance by remember { mutableStateOf(customerToEdit?.openingBalance?.toString() ?: "0") }
    var creditLimit by remember { mutableStateOf(customerToEdit?.creditLimit?.toString() ?: "0") }
    var paymentTermsDays by remember { mutableStateOf(customerToEdit?.paymentTermsDays?.toString() ?: "30") }
    var notes by remember { mutableStateOf(customerToEdit?.notes ?: "") }
    var tags by remember { mutableStateOf(customerToEdit?.tags ?: "") }

    // Validation Errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var gstError by remember { mutableStateOf<String?>(null) }

    val customerTypes = listOf("Retail", "Wholesale", "Distributor", "Corporate", "Other")
    var isTypeExpanded by remember { mutableStateOf(false) }

    fun validateInputs(): Boolean {
        var isValid = true

        if (name.trim().isEmpty()) {
            nameError = "Customer name is required"
            isValid = false
        } else {
            nameError = null
        }

        if (phone.trim().isEmpty()) {
            phoneError = "Phone number is required"
            isValid = false
        } else if (phone.trim().length < 8) {
            phoneError = "Enter a valid phone number"
            isValid = false
        } else {
            phoneError = null
        }

        if (email.isNotBlank() && (!email.contains("@") || !email.contains("."))) {
            emailError = "Enter a valid email address"
            isValid = false
        } else {
            emailError = null
        }

        if (gstNumber.isNotBlank() && gstNumber.trim().length != 15) {
            gstError = "GST number must be 15 characters"
            isValid = false
        } else {
            gstError = null
        }

        return isValid
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 16.dp)
            .testTag("add_edit_customer_dialog"),
        confirmButton = {
            TextButton(
                onClick = {
                    if (validateInputs()) {
                        val parsedOpeningBalance = openingBalance.toDoubleOrNull() ?: 0.0
                        val parsedCreditLimit = creditLimit.toDoubleOrNull() ?: 0.0
                        val parsedTerms = paymentTermsDays.toIntOrNull() ?: 30

                        val updatedCustomer = (customerToEdit ?: CustomerEntity(
                            name = name.trim(),
                            phone = phone.trim(),
                            createdDate = System.currentTimeMillis()
                        )).copy(
                            name = name.trim(),
                            company = company.trim(),
                            phone = phone.trim(),
                            alternateNumber = alternateNumber.trim(),
                            email = email.trim(),
                            gstNumber = gstNumber.trim().uppercase(),
                            panNumber = panNumber.trim().uppercase(),
                            customerType = customerType,
                            billingAddress = billingAddress.trim(),
                            shippingAddress = shippingAddress.trim().ifEmpty { billingAddress.trim() },
                            city = city.trim(),
                            state = state.trim(),
                            pincode = pincode.trim(),
                            openingBalance = parsedOpeningBalance,
                            outstandingBalance = if (customerToEdit == null) parsedOpeningBalance else (customerToEdit.outstandingBalance),
                            creditLimit = parsedCreditLimit,
                            paymentTermsDays = parsedTerms,
                            notes = notes.trim(),
                            tags = tags.trim()
                        )
                        onSave(updatedCustomer)
                    }
                },
                modifier = Modifier.testTag("save_customer_btn")
            ) {
                Text(if (customerToEdit == null) "Create Customer" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text(
                text = if (customerToEdit == null) "Add New Customer" else "Edit Customer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                // Name & Business
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text("Customer Name *") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_name")
                )

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Business / Company Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_business_name")
                )

                // Phone & Alternate Phone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            if (phoneError != null) phoneError = null
                        },
                        label = { Text("Mobile Number *") },
                        isError = phoneError != null,
                        supportingText = phoneError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_customer_phone")
                    )

                    OutlinedTextField(
                        value = alternateNumber,
                        onValueChange = { alternateNumber = it },
                        label = { Text("Alt Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text("Email Address") },
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_email")
                )

                // Customer Type Selection
                Text(
                    text = "Customer Type",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    customerTypes.forEach { type ->
                        FilterChip(
                            selected = customerType == type,
                            onClick = { customerType = type },
                            label = { Text(type) }
                        )
                    }
                }

                // Tax Info: GST & PAN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    OutlinedTextField(
                        value = gstNumber,
                        onValueChange = {
                            gstNumber = it
                            if (gstError != null) gstError = null
                        },
                        label = { Text("GSTIN Number") },
                        isError = gstError != null,
                        supportingText = gstError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_customer_gst")
                    )

                    OutlinedTextField(
                        value = panNumber,
                        onValueChange = { panNumber = it },
                        label = { Text("PAN Number") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Addresses
                OutlinedTextField(
                    value = billingAddress,
                    onValueChange = { billingAddress = it },
                    label = { Text("Billing Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = pincode,
                    onValueChange = { pincode = it },
                    label = { Text("Pincode") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Financial terms
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    OutlinedTextField(
                        value = openingBalance,
                        onValueChange = { openingBalance = it },
                        label = { Text("Opening Balance (₹)") },
                        enabled = customerToEdit == null, // opening balance set on creation
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = creditLimit,
                        onValueChange = { creditLimit = it },
                        label = { Text("Credit Limit (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = paymentTermsDays,
                    onValueChange = { paymentTermsDays = it },
                    label = { Text("Payment Terms (Days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated e.g. VIP, Wholesale)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Customer Notes & Remarks") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
