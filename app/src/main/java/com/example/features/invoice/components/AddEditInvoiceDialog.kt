package com.example.features.invoice.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.database.entity.CustomerEntity
import com.example.core.database.entity.InventoryItemEntity
import com.example.core.database.entity.InvoiceEntity
import com.example.core.database.entity.InvoiceItemEntity
import com.example.core.database.entity.InvoiceWithItems
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing

@Composable
fun AddEditInvoiceDialog(
    initialInvoiceWithItems: InvoiceWithItems?,
    defaultInvoiceNumber: String,
    customers: List<CustomerEntity>,
    products: List<InventoryItemEntity>,
    currencySymbol: String,
    onSave: (InvoiceEntity, List<InvoiceItemEntity>, Boolean) -> Unit, // Boolean: isComplete
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val isEdit = initialInvoiceWithItems != null
    var invoiceNumber by remember { mutableStateOf(initialInvoiceWithItems?.invoice.let { it?.invoiceNumber } ?: defaultInvoiceNumber) }

    // Customer state
    var selectedCustomerId by remember { mutableStateOf(initialInvoiceWithItems?.invoice?.customerId ?: 0L) }
    var customerName by remember { mutableStateOf(initialInvoiceWithItems?.invoice?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(initialInvoiceWithItems?.invoice?.customerPhone ?: "") }
    var customerGst by remember { mutableStateOf(initialInvoiceWithItems?.invoice?.customerGst ?: "") }
    var billingAddress by remember { mutableStateOf(initialInvoiceWithItems?.invoice?.billingAddress ?: "") }

    var showCustomerPicker by remember { mutableStateOf(false) }
    var showProductPicker by remember { mutableStateOf(false) }

    // Items List
    val lineItems = remember {
        mutableStateListOf<InvoiceItemEntity>().apply {
            if (initialInvoiceWithItems != null) {
                addAll(initialInvoiceWithItems.items)
            }
        }
    }

    // Discount & Payment
    var discountType by remember { mutableStateOf(initialInvoiceWithItems?.invoice?.discountType ?: "FLAT") }
    var discountValueInput by remember { mutableStateOf(initialInvoiceWithItems?.invoice?.discountValue?.toString() ?: "0.0") }
    var notes by remember { mutableStateOf(initialInvoiceWithItems?.invoice?.notes ?: "") }
    var terms by remember { mutableStateOf(initialInvoiceWithItems?.invoice?.terms ?: "Goods once sold cannot be returned or exchanged.") }

    var paymentStatus by remember { mutableStateOf(initialInvoiceWithItems?.invoice?.paymentStatus ?: "UNPAID") }
    var paidAmountInput by remember { mutableStateOf(initialInvoiceWithItems?.invoice?.paidAmount?.toString() ?: "0.0") }

    // Computations
    val subtotal by remember {
        derivedStateOf {
            lineItems.sumOf { it.sellingPrice * it.quantity }
        }
    }

    val totalTaxAmount by remember {
        derivedStateOf {
            lineItems.sumOf { item ->
                val lineSub = item.sellingPrice * item.quantity
                lineSub * (item.gstPercentage / 100.0)
            }
        }
    }

    val discountAmount by remember {
        derivedStateOf {
            val valNum = discountValueInput.toDoubleOrNull() ?: 0.0
            if (discountType == "PERCENTAGE") {
                (subtotal * valNum / 100.0).coerceAtMost(subtotal)
            } else {
                valNum.coerceAtMost(subtotal)
            }
        }
    }

    val grandTotal by remember {
        derivedStateOf {
            (subtotal - discountAmount + totalTaxAmount).coerceAtLeast(0.0)
        }
    }

    val paidAmount by remember {
        derivedStateOf {
            paidAmountInput.toDoubleOrNull() ?: 0.0
        }
    }

    val balanceAmount by remember {
        derivedStateOf {
            (grandTotal - paidAmount).coerceAtLeast(0.0)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    Surface(
                        tonalElevation = 4.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.m, vertical = Spacing.s),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onDismiss) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                                Text(
                                    text = if (isEdit) "Edit Invoice #${invoiceNumber}" else "New Invoice #${invoiceNumber}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    Surface(
                        tonalElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.m),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (lineItems.isEmpty()) {
                                        Toast.makeText(context, "Please add at least one line item.", Toast.LENGTH_SHORT).show()
                                        return@OutlinedButton
                                    }
                                    val entity = buildInvoiceEntity(
                                        id = initialInvoiceWithItems?.invoice?.id ?: 0L,
                                        invoiceNumber = invoiceNumber,
                                        customerId = selectedCustomerId,
                                        customerName = customerName,
                                        customerPhone = customerPhone,
                                        customerGst = customerGst,
                                        billingAddress = billingAddress,
                                        status = "DRAFT",
                                        paymentStatus = paymentStatus,
                                        subtotal = subtotal,
                                        discountType = discountType,
                                        discountValue = discountValueInput.toDoubleOrNull() ?: 0.0,
                                        discountAmount = discountAmount,
                                        taxAmount = totalTaxAmount,
                                        totalAmount = grandTotal,
                                        paidAmount = paidAmount,
                                        balanceAmount = balanceAmount,
                                        itemsCount = lineItems.size,
                                        notes = notes,
                                        terms = terms
                                    )
                                    onSave(entity, lineItems, false)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_save_draft")
                            ) {
                                Text("Save Draft")
                            }

                            Button(
                                onClick = {
                                    if (lineItems.isEmpty()) {
                                        Toast.makeText(context, "Please add at least one line item.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val entity = buildInvoiceEntity(
                                        id = initialInvoiceWithItems?.invoice?.id ?: 0L,
                                        invoiceNumber = invoiceNumber,
                                        customerId = selectedCustomerId,
                                        customerName = customerName,
                                        customerPhone = customerPhone,
                                        customerGst = customerGst,
                                        billingAddress = billingAddress,
                                        status = "COMPLETED",
                                        paymentStatus = if (balanceAmount <= 0) "PAID" else if (paidAmount > 0) "PARTIAL" else "UNPAID",
                                        subtotal = subtotal,
                                        discountType = discountType,
                                        discountValue = discountValueInput.toDoubleOrNull() ?: 0.0,
                                        discountAmount = discountAmount,
                                        taxAmount = totalTaxAmount,
                                        totalAmount = grandTotal,
                                        paidAmount = paidAmount,
                                        balanceAmount = balanceAmount,
                                        itemsCount = lineItems.size,
                                        notes = notes,
                                        terms = terms
                                    )
                                    onSave(entity, lineItems, true)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_complete_invoice")
                            ) {
                                Text("Complete Invoice")
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.m)
                ) {
                    // 1. Customer Section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.m)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(Spacing.s))
                                    Text(
                                        text = if (customerName.isEmpty()) "Walk-in Customer" else customerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                TextButton(
                                    onClick = { showCustomerPicker = true },
                                    modifier = Modifier.testTag("btn_change_customer")
                                ) {
                                    Text("Select / Change")
                                }
                            }

                            if (selectedCustomerId != 0L || customerName.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = customerPhone,
                                        onValueChange = { customerPhone = it },
                                        label = { Text("Phone Number") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.s))
                                    OutlinedTextField(
                                        value = customerGst,
                                        onValueChange = { customerGst = it },
                                        label = { Text("GSTIN / Tax ID") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.m))

                    // 2. Line Items Section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Line Items (${lineItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { showProductPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.testTag("btn_add_product_item")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text("Add Product")
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.s))

                    if (lineItems.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.l),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.ShoppingBag,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.height(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(Spacing.xs))
                                    Text(
                                        text = "No items added yet. Click 'Add Product' to build invoice.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    } else {
                        lineItems.forEachIndexed { index, item ->
                            LineItemRow(
                                item = item,
                                currencySymbol = currencySymbol,
                                onQuantityChange = { newQty ->
                                    val calcTax = (item.sellingPrice * newQty) * (item.gstPercentage / 100.0)
                                    val calcTotal = (item.sellingPrice * newQty) + calcTax - item.discount
                                    lineItems[index] = item.copy(
                                        quantity = newQty,
                                        taxAmount = calcTax,
                                        lineTotal = calcTotal
                                    )
                                },
                                onPriceChange = { newPrice ->
                                    val calcTax = (newPrice * item.quantity) * (item.gstPercentage / 100.0)
                                    val calcTotal = (newPrice * item.quantity) + calcTax - item.discount
                                    lineItems[index] = item.copy(
                                        sellingPrice = newPrice,
                                        taxAmount = calcTax,
                                        lineTotal = calcTotal
                                    )
                                },
                                onRemove = {
                                    lineItems.removeAt(index)
                                }
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.l))

                    // 3. Billing Totals Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.m)) {
                            Text(
                                text = "Summary & Payment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(Spacing.s))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal:")
                                Text("$currencySymbol${String.format("%.2f", subtotal)}", fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(Spacing.xs))

                            // Discount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Discount:", modifier = Modifier.weight(1f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FilterChip(
                                        selected = discountType == "FLAT",
                                        onClick = { discountType = "FLAT" },
                                        label = { Text("Flat") }
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    FilterChip(
                                        selected = discountType == "PERCENTAGE",
                                        onClick = { discountType = "PERCENTAGE" },
                                        label = { Text("%") }
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.s))
                                    OutlinedTextField(
                                        value = discountValueInput,
                                        onValueChange = { discountValueInput = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.width(80.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Spacing.xs))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tax / GST:")
                                Text("$currencySymbol${String.format("%.2f", totalTaxAmount)}", fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(Spacing.s))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(Spacing.s))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Grand Total:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$currencySymbol${String.format("%.2f", grandTotal)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(Spacing.m))

                            // Payment Details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = paidAmountInput,
                                    onValueChange = { paidAmountInput = it },
                                    label = { Text("Paid Amount") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(Spacing.m))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Balance Due", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "$currencySymbol${String.format("%.2f", balanceAmount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (balanceAmount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.m))

                    // Notes & Terms
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Invoice Notes (Internal / Customer)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Spacing.s))

                    OutlinedTextField(
                        value = terms,
                        onValueChange = { terms = it },
                        label = { Text("Terms & Conditions") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showCustomerPicker) {
        CustomerSelectorDialog(
            customers = customers,
            currencySymbol = currencySymbol,
            onSelectCustomer = { cust ->
                if (cust != null) {
                    selectedCustomerId = cust.id
                    customerName = cust.name
                    customerPhone = cust.phone
                    customerGst = cust.gstNumber
                    billingAddress = cust.billingAddress
                } else {
                    selectedCustomerId = 0L
                    customerName = "Walk-in Customer"
                    customerPhone = ""
                    customerGst = ""
                    billingAddress = ""
                }
                showCustomerPicker = false
            },
            onDismiss = { showCustomerPicker = false }
        )
    }

    if (showProductPicker) {
        ProductSelectorDialog(
            products = products,
            currencySymbol = currencySymbol,
            onSelectProduct = { prod ->
                val lineSub = prod.unitPrice * 1.0
                val lineTax = lineSub * (prod.gstPercentage / 100.0)
                lineItems.add(
                    InvoiceItemEntity(
                        productId = prod.id,
                        productName = prod.name,
                        sku = prod.sku,
                        quantity = 1.0,
                        unit = prod.unit,
                        sellingPrice = prod.unitPrice,
                        discount = 0.0,
                        gstPercentage = prod.gstPercentage,
                        taxAmount = lineTax,
                        lineTotal = lineSub + lineTax
                    )
                )
                showProductPicker = false
            },
            onDismiss = { showProductPicker = false }
        )
    }
}

@Composable
private fun LineItemRow(
    item: InvoiceItemEntity,
    currencySymbol: String,
    onQuantityChange: (Double) -> Unit,
    onPriceChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(Spacing.m)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "GST: ${item.gstPercentage.toInt()}% • Price: $currencySymbol${String.format("%.2f", item.sellingPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString(),
                    onValueChange = {
                        val num = it.toDoubleOrNull() ?: 1.0
                        onQuantityChange(num.coerceAtLeast(0.1))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    label = { Text("Qty") },
                    modifier = Modifier.width(64.dp)
                )

                Spacer(modifier = Modifier.width(Spacing.s))

                Text(
                    text = "$currencySymbol${String.format("%.2f", item.lineTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun buildInvoiceEntity(
    id: Long,
    invoiceNumber: String,
    customerId: Long,
    customerName: String,
    customerPhone: String,
    customerGst: String,
    billingAddress: String,
    status: String,
    paymentStatus: String,
    subtotal: Double,
    discountType: String,
    discountValue: Double,
    discountAmount: Double,
    taxAmount: Double,
    totalAmount: Double,
    paidAmount: Double,
    balanceAmount: Double,
    itemsCount: Int,
    notes: String,
    terms: String
): InvoiceEntity {
    return InvoiceEntity(
        id = id,
        invoiceNumber = invoiceNumber,
        date = System.currentTimeMillis(),
        dueDate = System.currentTimeMillis() + 15 * 86400000L,
        customerId = customerId,
        customerName = customerName.ifEmpty { "Walk-in Customer" },
        customerPhone = customerPhone,
        customerGst = customerGst,
        billingAddress = billingAddress,
        status = status,
        paymentStatus = paymentStatus,
        subtotal = subtotal,
        discountType = discountType,
        discountValue = discountValue,
        discountAmount = discountAmount,
        taxAmount = taxAmount,
        roundOff = 0.0,
        totalAmount = totalAmount,
        paidAmount = paidAmount,
        balanceAmount = balanceAmount,
        itemsCount = itemsCount,
        notes = notes,
        terms = terms,
        createdDate = System.currentTimeMillis(),
        updatedDate = System.currentTimeMillis()
    )
}
