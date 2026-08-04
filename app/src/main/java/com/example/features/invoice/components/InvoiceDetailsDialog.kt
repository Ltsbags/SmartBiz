package com.example.features.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.database.entity.InvoiceWithItems
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceDetailsDialog(
    invoiceWithItems: InvoiceWithItems,
    currencySymbol: String,
    onPrint: () -> Unit,
    onShare: () -> Unit,
    onDuplicate: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val invoice = invoiceWithItems.invoice
    val items = invoiceWithItems.items
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

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
                                    text = "Invoice Details #${invoice.invoiceNumber}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = onPrint,
                                    modifier = Modifier.testTag("btn_detail_print")
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = "Print")
                                }
                                IconButton(
                                    onClick = onShare,
                                    modifier = Modifier.testTag("btn_detail_share")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share")
                                }
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
                            if (invoice.status == "DRAFT") {
                                Button(
                                    onClick = onEdit,
                                    modifier = Modifier.weight(1f).testTag("btn_detail_edit")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Text("Edit Draft")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onDuplicate,
                                    modifier = Modifier.weight(1f).testTag("btn_detail_duplicate")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Text("Duplicate")
                                }
                            }

                            if (invoice.status != "CANCELLED") {
                                OutlinedButton(
                                    onClick = onCancel,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f).testTag("btn_detail_cancel")
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = null)
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Text("Cancel")
                                }
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
                    // Header Status
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.m)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = invoice.invoiceNumber,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row {
                                    StatusBadge(status = invoice.status)
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    PaymentStatusBadge(paymentStatus = invoice.paymentStatus)
                                }
                            }

                            Spacer(modifier = Modifier.height(Spacing.xs))

                            Text(
                                text = "Date: ${dateFormat.format(Date(invoice.date))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.m))

                    // Billed To Customer Info
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.m)) {
                            Text(
                                text = "Billed To",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = invoice.customerName.ifEmpty { "Walk-in Customer" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (invoice.customerPhone.isNotEmpty()) {
                                Text(text = "Phone: ${invoice.customerPhone}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (invoice.customerGst.isNotEmpty()) {
                                Text(text = "GSTIN: ${invoice.customerGst}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (invoice.billingAddress.isNotEmpty()) {
                                Text(text = invoice.billingAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.m))

                    // Itemized Table
                    Text(
                        text = "Line Items (${items.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(Spacing.s))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.m)) {
                            items.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${index + 1}. ${item.productName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()} ${item.unit} @ $currencySymbol${String.format("%.2f", item.sellingPrice)} (${item.gstPercentage.toInt()}% GST)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "$currencySymbol${String.format("%.2f", item.lineTotal)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (index < items.size - 1) {
                                    Spacer(modifier = Modifier.height(Spacing.s))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(Spacing.s))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.m))

                    // Calculation Summary Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.m)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:")
                                Text("$currencySymbol${String.format("%.2f", invoice.subtotal)}")
                            }
                            if (invoice.discountAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount:")
                                    Text("-$currencySymbol${String.format("%.2f", invoice.discountAmount)}")
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tax / GST:")
                                Text("$currencySymbol${String.format("%.2f", invoice.taxAmount)}")
                            }
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("$currencySymbol${String.format("%.2f", invoice.totalAmount)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Amount Paid:")
                                Text("$currencySymbol${String.format("%.2f", invoice.paidAmount)}")
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Balance Due:", fontWeight = FontWeight.Bold)
                                Text("$currencySymbol${String.format("%.2f", invoice.balanceAmount)}", fontWeight = FontWeight.Bold, color = if (invoice.balanceAmount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    if (invoice.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Spacing.m))
                        Text(text = "Notes: ${invoice.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (invoice.terms.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(text = "Terms & Conditions: ${invoice.terms}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
