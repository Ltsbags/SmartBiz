package com.example.features.inventory.components

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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.database.entity.CategoryEntity
import com.example.core.database.entity.InventoryItemEntity
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing
import com.example.shared.forms.SmartBizTextField
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    productToEdit: InventoryItemEntity? = null,
    categories: List<CategoryEntity>,
    onSave: (InventoryItemEntity) -> Unit,
    onDismiss: () -> Unit,
    testTag: String = "add_edit_product_dialog"
) {
    val isEditMode = productToEdit != null

    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var sku by remember { mutableStateOf(productToEdit?.sku ?: "SKU-${(1000..9999).random()}") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }
    var selectedCategory by remember { mutableStateOf(productToEdit?.category ?: (categories.firstOrNull()?.name ?: "General")) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var brand by remember { mutableStateOf(productToEdit?.brand ?: "") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }
    var unit by remember { mutableStateOf(productToEdit?.unit ?: "pcs") }

    var purchasePriceText by remember { mutableStateOf(productToEdit?.purchasePrice?.toString() ?: "0.0") }
    var sellingPriceText by remember { mutableStateOf(productToEdit?.unitPrice?.toString() ?: "0.0") }
    var gstPercentageText by remember { mutableStateOf(productToEdit?.gstPercentage?.toString() ?: "18.0") }

    var openingStockText by remember { mutableStateOf(productToEdit?.openingStock?.toString() ?: "10") }
    var currentStockText by remember { mutableStateOf(productToEdit?.stockQuantity?.toString() ?: "10") }
    var minStockText by remember { mutableStateOf(productToEdit?.minStockThreshold?.toString() ?: "5") }
    var maxStockText by remember { mutableStateOf(productToEdit?.maxStock?.toString() ?: "100") }

    var location by remember { mutableStateOf(productToEdit?.location ?: "Shelf A-1") }
    var imagePath by remember { mutableStateOf(productToEdit?.imagePath ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(Dimensions.radius16),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(Spacing.m)
                .testTag(testTag)
        ) {
            Column(
                modifier = Modifier
                    .padding(Spacing.l)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isEditMode) "Edit Product" else "Add New Product",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.m))

                // Product Name
                SmartBizTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = if (it.isBlank()) "Product Name is required" else null
                    },
                    label = "Product Name *",
                    placeholder = "e.g. Wireless Barcode Reader",
                    isError = nameError != null,
                    errorMessage = nameError,
                    testTag = "input_product_name"
                )
                Spacer(modifier = Modifier.height(Spacing.m))

                // Category Dropdown & Unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCategory = cat.name
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                            if (categories.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("General") },
                                    onClick = {
                                        selectedCategory = "General"
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    SmartBizTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = "Unit",
                        placeholder = "pcs, box, kg",
                        modifier = Modifier.weight(0.8f),
                        testTag = "input_product_unit"
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                // SKU & Auto Generate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmartBizTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = "SKU Code",
                        placeholder = "SKU-901",
                        modifier = Modifier.weight(1f),
                        testTag = "input_product_sku"
                    )
                    OutlinedButton(
                        onClick = { sku = "SKU-${(10000..99999).random()}" },
                        modifier = Modifier.padding(top = 18.dp)
                    ) {
                        Text("Auto")
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                // Barcode & Brand
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    SmartBizTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = "Barcode",
                        placeholder = "8901234567890",
                        modifier = Modifier.weight(1f),
                        testTag = "input_product_barcode"
                    )
                    SmartBizTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = "Brand",
                        placeholder = "SmartTech",
                        modifier = Modifier.weight(1f),
                        testTag = "input_product_brand"
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                // Pricing Row: Purchase Price, Selling Price, GST %
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    SmartBizTextField(
                        value = purchasePriceText,
                        onValueChange = { purchasePriceText = it },
                        label = "Cost Price ($)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        testTag = "input_cost_price"
                    )
                    SmartBizTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it },
                        label = "Selling Price ($) *",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        testTag = "input_selling_price"
                    )
                    SmartBizTextField(
                        value = gstPercentageText,
                        onValueChange = { gstPercentageText = it },
                        label = "GST %",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f),
                        testTag = "input_gst"
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                // Stock Row: Opening, Current, Min Alert, Max
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    SmartBizTextField(
                        value = currentStockText,
                        onValueChange = { currentStockText = it },
                        label = "Current Stock *",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        testTag = "input_current_stock"
                    )
                    SmartBizTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = "Min Alert",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        testTag = "input_min_stock"
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                // Location & Description
                SmartBizTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Store Location",
                    placeholder = "Shelf A-1, Rack 3",
                    testTag = "input_location"
                )

                Spacer(modifier = Modifier.height(Spacing.m))

                SmartBizTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description / Notes",
                    placeholder = "Product specs & details...",
                    singleLine = false,
                    maxLines = 3,
                    testTag = "input_description"
                )

                Spacer(modifier = Modifier.height(Spacing.l))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(Spacing.m))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = "Product Name is required"
                                return@Button
                            }
                            val purchasePrice = purchasePriceText.toDoubleOrNull() ?: 0.0
                            val sellingPrice = sellingPriceText.toDoubleOrNull() ?: 0.0
                            val gst = gstPercentageText.toDoubleOrNull() ?: 0.0
                            val currentStock = currentStockText.toIntOrNull() ?: 0
                            val openingStock = openingStockText.toIntOrNull() ?: currentStock
                            val minStock = minStockText.toIntOrNull() ?: 5
                            val maxStock = maxStockText.toIntOrNull() ?: 100

                            val product = InventoryItemEntity(
                                id = productToEdit?.id ?: 0,
                                name = name.trim(),
                                sku = sku.trim(),
                                barcode = barcode.trim(),
                                category = selectedCategory,
                                brand = brand.trim(),
                                description = description.trim(),
                                unit = unit.trim().ifEmpty { "pcs" },
                                purchasePrice = purchasePrice,
                                costPrice = purchasePrice,
                                unitPrice = sellingPrice,
                                gstPercentage = gst,
                                openingStock = openingStock,
                                stockQuantity = currentStock,
                                minStockThreshold = minStock,
                                maxStock = maxStock,
                                location = location.trim(),
                                imagePath = imagePath.trim(),
                                createdDate = productToEdit?.createdDate ?: System.currentTimeMillis(),
                                updatedDate = System.currentTimeMillis(),
                                isActive = productToEdit?.isActive ?: true,
                                isArchived = productToEdit?.isArchived ?: false
                            )

                            onSave(product)
                        },
                        modifier = Modifier.testTag("save_product_btn")
                    ) {
                        Text(if (isEditMode) "Update Product" else "Save Product")
                    }
                }
            }
        }
    }
}
