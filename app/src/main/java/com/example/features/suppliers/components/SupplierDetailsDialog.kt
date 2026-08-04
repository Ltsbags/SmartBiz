package com.example.features.suppliers.components

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
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.core.database.entity.SupplierEntity

@Composable
fun SupplierDetailsDialog(
    supplier: SupplierEntity,
    onDismiss: () -> Unit,
    onEdit: (SupplierEntity) -> Unit,
    onDelete: (SupplierEntity) -> Unit,
    onArchiveToggle: (SupplierEntity) -> Unit
) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = supplier.supplierName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = supplier.supplierCode,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { onEdit(supplier) },
                            modifier = Modifier.testTag("btn_edit_supplier_details")
                        ) {
                            Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit")
                        }
                        IconButton(
                            onClick = { onArchiveToggle(supplier) },
                            modifier = Modifier.testTag("btn_archive_supplier_details")
                        ) {
                            Icon(
                                imageVector = if (supplier.isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                                contentDescription = if (supplier.isArchived) "Restore" else "Archive"
                            )
                        }
                        IconButton(
                            onClick = { onDelete(supplier) },
                            modifier = Modifier.testTag("btn_delete_supplier_details")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                DetailItem(label = "Business / Company", value = supplier.businessName.ifBlank { "N/A" })
                DetailItem(label = "Phone Number", value = supplier.phone.ifBlank { "N/A" })
                DetailItem(label = "Email Address", value = supplier.email.ifBlank { "N/A" })
                DetailItem(label = "GST Number", value = supplier.gstNumber.ifBlank { "N/A" })
                DetailItem(label = "PAN Number", value = supplier.panNumber.ifBlank { "N/A" })

                val fullAddress = listOf(supplier.billingAddress, supplier.city, supplier.state, supplier.pincode)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                DetailItem(label = "Address", value = fullAddress.ifBlank { "N/A" })

                DetailItem(label = "Payment Terms", value = supplier.paymentTerms)
                DetailItem(
                    label = "Opening Balance",
                    value = "$${String.format("%.2f", supplier.openingBalance)}"
                )
                DetailItem(
                    label = "Outstanding Balance",
                    value = "$${String.format("%.2f", supplier.outstandingBalance)}",
                    valueColor = if (supplier.outstandingBalance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )

                if (supplier.notes.isNotBlank()) {
                    DetailItem(label = "Notes", value = supplier.notes)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = valueColor
        )
    }
}
