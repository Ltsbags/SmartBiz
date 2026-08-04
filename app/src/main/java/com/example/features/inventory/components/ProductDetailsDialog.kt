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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.database.entity.InventoryItemEntity
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProductDetailsDialog(
    item: InventoryItemEntity,
    currencySymbol: String = "$",
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    testTag: String = "product_details_dialog"
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault())

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
                // Large Product Image Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductImageWidget(
                        imagePath = item.imagePath,
                        productName = item.name,
                        size = 72.dp
                    )
                    Spacer(modifier = Modifier.width(Spacing.m))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${item.category}${if (item.brand.isNotBlank()) " • ${item.brand}" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        StockBadge(
                            stockQuantity = item.stockQuantity,
                            minStockThreshold = item.minStockThreshold,
                            unit = item.unit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.l))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.l))

                // Pricing Info Section
                Text(
                    text = "Pricing & Taxes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Spacing.s))
                DetailRow("Selling Price:", "$currencySymbol${String.format("%.2f", item.unitPrice)}")
                DetailRow("Cost Price:", "$currencySymbol${String.format("%.2f", item.costPrice.takeIf { it > 0 } ?: item.purchasePrice)}")
                DetailRow("GST Tax Rate:", "${item.gstPercentage}%")

                Spacer(modifier = Modifier.height(Spacing.m))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.m))

                // Inventory & Location Section
                Text(
                    text = "Inventory & Storage",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Spacing.s))
                DetailRow("SKU:", item.sku.ifEmpty { "N/A" })
                DetailRow("Barcode:", item.barcode.ifEmpty { "N/A" })
                DetailRow("Current Stock:", "${item.stockQuantity} ${item.unit}")
                DetailRow("Minimum Alert Level:", "${item.minStockThreshold} ${item.unit}")
                DetailRow("Store Location:", item.location.ifEmpty { "Unassigned" })

                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.m))
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.m))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.m))

                // Timestamps
                DetailRow("Created Date:", dateFormat.format(Date(item.createdDate)))
                DetailRow("Last Updated:", dateFormat.format(Date(item.updatedDate)))

                Spacer(modifier = Modifier.height(Spacing.l))

                // Quick Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onDismiss(); onEdit() }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    OutlinedButton(
                        onClick = { onDismiss(); onDuplicate() }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Duplicate")
                    }
                    OutlinedButton(
                        onClick = { onDismiss(); onArchive() }
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (item.isArchived) "Restore" else "Archive")
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.m))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onDismiss(); onDelete() }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
