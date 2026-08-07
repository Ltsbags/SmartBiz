package com.example.features.payment.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.database.entity.PaymentRequestEntity
import com.example.services.payment.models.PaymentEngineRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentLinkDialog(
    initialInvoiceId: Long? = null,
    initialInvoiceNumber: String = "",
    initialAmount: Double = 0.0,
    initialCustomerName: String = "",
    onDismiss: () -> Unit,
    onGenerateLink: (request: PaymentEngineRequest, expiryHours: Int) -> Unit,
    getQrBitmap: (payload: String) -> Bitmap
) {
    var amountStr by remember { mutableStateOf(if (initialAmount > 0) initialAmount.toString() else "") }
    var customerName by remember { mutableStateOf(initialCustomerName) }
    var invoiceNumber by remember { mutableStateOf(initialInvoiceNumber) }
    var description by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("UPI") }
    var expiryHoursStr by remember { mutableStateOf("72") }

    var generatedRequest by remember { mutableStateOf<PaymentRequestEntity?>(null) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var providerDropdownExpanded by remember { mutableStateOf(false) }

    val providers = listOf("UPI", "RAZORPAY", "STRIPE", "CASH")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (generatedRequest == null) "Create Payment Link / QR" else "Payment Link Generated",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                if (generatedRequest == null) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Payment Amount (INR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("link_amount_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = invoiceNumber,
                        onValueChange = { invoiceNumber = it },
                        label = { Text("Invoice # (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Note / Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = providerDropdownExpanded,
                        onExpandedChange = { providerDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = provider,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Preferred Provider / Gateway") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = providerDropdownExpanded,
                            onDismissRequest = { providerDropdownExpanded = false }
                        ) {
                            providers.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = {
                                        provider = p
                                        providerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = expiryHoursStr,
                        onValueChange = { expiryHoursStr = it },
                        label = { Text("Expiry (Hours)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    val req = generatedRequest!!
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Request #: ${req.requestNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Amount: ₹${req.amount}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            qrBitmap?.let { bmp ->
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "Dynamic Payment QR Code",
                                        modifier = Modifier.size(180.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Payment Link URL:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = req.paymentLinkUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (generatedRequest == null) {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            val req = PaymentEngineRequest(
                                amount = amt,
                                customerName = customerName,
                                invoiceId = initialInvoiceId,
                                invoiceNumber = invoiceNumber,
                                description = description,
                                preferredProvider = provider,
                                paymentMethod = provider
                            )
                            val exp = expiryHoursStr.toIntOrNull() ?: 72
                            onGenerateLink(req, exp)
                        }
                    },
                    modifier = Modifier.testTag("submit_generate_link_btn")
                ) {
                    Text("Generate Link & QR")
                }
            } else {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("done_link_btn")
                ) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (generatedRequest == null) "Cancel" else "Close")
            }
        }
    )
}
