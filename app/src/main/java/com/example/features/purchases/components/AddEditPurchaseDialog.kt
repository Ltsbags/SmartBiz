package com.example.features.purchases.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.core.database.entity.InventoryItemEntity
import com.example.core.database.entity.PurchaseEntity
import com.example.core.database.entity.PurchaseItemEntity
import com.example.core.database.entity.PurchaseWithItems
import com.example.core.database.entity.SupplierEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPurchaseDialog(
    purchaseWithItems: PurchaseWithItems?,
    suppliers: List<SupplierEntity>,
    availableProducts: List<InventoryItemEntity>,
    onDismiss: () -> Unit,
    onSave: (PurchaseEntity, List<PurchaseItemEntity>) -> Unit
) {
    val isEdit = purchaseWithItems != null
    val existingPurchase = purchaseWithItems?.purchase

    var selectedSupplier by remember {
        mutableStateOf(suppliers.find { it.id == existingPurchase?.supplierId })
    }
    var showSupplierSelector by remember { mutableStateOf(false) }

    var status by remember { mutableStateOf(existingPurchase?.status ?: "DRAFT") }
    var notes by remember { mutableStateOf(existingPurchase?.notes ?: "") }
    var paidAmountText by remember { mutableStateOf(existingPurchase?.paidAmount?.toString() ?: "0.0") }

    val itemsList = remember {
        mutableStateListOf<PurchaseItemEntity>().apply {
            if (purchaseWithItems != null) {
                addAll(purchaseWithItems.items)
            }
        }
    }

    var selectedProductForAdd by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    // Calculation helper
    val subtotal = itemsList.sumOf { (it.quantity * it.purchasePrice) }
    val totalTax = itemsList.sumOf { (it.quantity * it.purchasePrice * (it.taxPercentage / 100.0)) }
    val totalAmount = subtotal + totalTax
    val paidAmount = paidAmountText.toDoubleOrNull() ?: 0.0
    val balanceAmount = if (totalAmount - paidAmount < 0) 0.0 else totalAmount - paidAmount

    val paymentStatus = when {
        paidAmount >= totalAmount && totalAmount > 0 -> "PAID"
        paidAmount > 0 -> "PARTIAL"
        else -> "UNPAID"
    }

    var supplierError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isEdit) "Edit Purchase Order" else "New Purchase Order / Stock Inward",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Supplier Selector Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSupplierSelector = true }
                        .testTag("btn_select_supplier_po"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (supplierError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedSupplier?.supplierName ?: "Select Supplier *",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (selectedSupplier != null) {
                                Text(
                                    text = "${selectedSupplier!!.supplierCode} • ${selectedSupplier!!.phone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Items Section
                Text(
                    text = "Items / Products",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Product Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = productDropdownExpanded,
                    onExpandedChange = { productDropdownExpanded = !productDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedProductForAdd?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Product to Add") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = productDropdownExpanded,
                        onDismissRequest = { productDropdownExpanded = false }
                    ) {
                        availableProducts.forEach { product ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(product.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            "SKU: ${product.sku} | Cost: $${product.purchasePrice}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                onClick = {
                                    selectedProductForAdd = product
                                    productDropdownExpanded = false
                                    // Add to list
                                    itemsList.add(
                                        PurchaseItemEntity(
                                            productId = product.id,
                                            productName = product.name,
                                            sku = product.sku,
                                            quantity = 1.0,
                                            unit = product.unit,
                                            purchasePrice = product.purchasePrice,
                                            taxPercentage = product.gstPercentage,
                                            lineTotal = product.purchasePrice * (1 + product.gstPercentage / 100.0)
                                        )
                                    )
                                    selectedProductForAdd = null
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Added Items Table
                itemsList.forEachIndexed { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.productName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                IconButton(onClick = { itemsList.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = item.quantity.toString(),
                                    onValueChange = {
                                        val qty = it.toDoubleOrNull() ?: 1.0
                                        itemsList[index] = item.copy(
                                            quantity = qty,
                                            lineTotal = qty * item.purchasePrice * (1 + item.taxPercentage / 100.0)
                                        )
                                    },
                                    label = { Text("Qty (${item.unit})") },
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedTextField(
                                    value = item.purchasePrice.toString(),
                                    onValueChange = {
                                        val price = it.toDoubleOrNull() ?: 0.0
                                        itemsList[index] = item.copy(
                                            purchasePrice = price,
                                            lineTotal = item.quantity * price * (1 + item.taxPercentage / 100.0)
                                        )
                                    },
                                    label = { Text("Price ($)") },
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedTextField(
                                    value = item.taxPercentage.toString(),
                                    onValueChange = {
                                        val tax = it.toDoubleOrNull() ?: 0.0
                                        itemsList[index] = item.copy(
                                            taxPercentage = tax,
                                            lineTotal = item.quantity * item.purchasePrice * (1 + tax / 100.0)
                                        )
                                    },
                                    label = { Text("Tax %") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Summary Calculation Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal:", style = MaterialTheme.typography.bodyMedium)
                    Text("$${String.format("%.2f", subtotal)}", style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tax Amount:", style = MaterialTheme.typography.bodyMedium)
                    Text("$${String.format("%.2f", totalTax)}", style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Amount:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        "$${String.format("%.2f", totalAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = paidAmountText,
                        onValueChange = { paidAmountText = it },
                        label = { Text("Amount Paid ($)") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp)
                    ) {
                        Text("Balance: $${String.format("%.2f", balanceAmount)}", fontWeight = FontWeight.Bold)
                        Text("Payment: $paymentStatus", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Selector
                Text("Order Status", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("DRAFT", "ORDERED", "RECEIVED").forEach { s ->
                        OutlinedButton(
                            onClick = { status = s },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp),
                            border = if (status == s) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Text(if (s == "RECEIVED") "RECEIVED (Inward)" else s, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

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
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (selectedSupplier == null) {
                                supplierError = true
                                return@Button
                            }
                            if (itemsList.isEmpty()) {
                                return@Button
                            }

                            val entity = existingPurchase?.copy(
                                supplierId = selectedSupplier!!.id,
                                supplierName = selectedSupplier!!.supplierName,
                                supplierPhone = selectedSupplier!!.phone,
                                status = status,
                                paymentStatus = paymentStatus,
                                subtotal = subtotal,
                                taxAmount = totalTax,
                                totalAmount = totalAmount,
                                paidAmount = paidAmount,
                                balanceAmount = balanceAmount,
                                notes = notes
                            ) ?: PurchaseEntity(
                                purchaseNumber = "",
                                supplierId = selectedSupplier!!.id,
                                supplierName = selectedSupplier!!.supplierName,
                                supplierPhone = selectedSupplier!!.phone,
                                status = status,
                                paymentStatus = paymentStatus,
                                subtotal = subtotal,
                                taxAmount = totalTax,
                                totalAmount = totalAmount,
                                paidAmount = paidAmount,
                                balanceAmount = balanceAmount,
                                notes = notes
                            )

                            onSave(entity, itemsList.toList())
                        },
                        modifier = Modifier.testTag("btn_save_po")
                    ) {
                        Text(if (isEdit) "Update Purchase" else "Save Purchase Order")
                    }
                }
            }
        }
    }

    if (showSupplierSelector) {
        SupplierSelectorDialog(
            suppliers = suppliers,
            onDismiss = { showSupplierSelector = false },
            onSupplierSelected = {
                selectedSupplier = it
                supplierError = false
                showSupplierSelector = false
            }
        )
    }
}
