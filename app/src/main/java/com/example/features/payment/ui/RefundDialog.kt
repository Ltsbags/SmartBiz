package com.example.features.payment.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.database.entity.PaymentEntity

@Composable
fun RefundDialog(
    payment: PaymentEntity,
    onDismiss: () -> Unit,
    onConfirmRefund: (paymentId: Long, amount: Double, reason: String) -> Unit
) {
    var refundAmountStr by remember { mutableStateOf(payment.amount.toString()) }
    var reason by remember { mutableStateOf("Customer Request") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Initiate Refund",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Ref Payment #: ${payment.paymentNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Customer: ${payment.customerName.ifBlank { "N/A" }} | Method: ${payment.paymentMethod}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Original Paid Amount: ₹${payment.amount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = refundAmountStr,
                    onValueChange = {
                        refundAmountStr = it
                        errorMessage = null
                    },
                    label = { Text("Refund Amount (INR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMessage != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("refund_amount_input")
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Refund") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("refund_reason_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "⚠️ Accounting Impact: Issuing a refund will adjust the customer's ledger, reduce store cash inflow, and restore invoice outstanding balance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = refundAmountStr.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        errorMessage = "Please enter a valid positive amount"
                    } else if (amt > payment.amount) {
                        errorMessage = "Refund cannot exceed original amount ₹${payment.amount}"
                    } else {
                        onConfirmRefund(payment.id, amt, reason)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_refund_btn")
            ) {
                Text("Confirm Refund")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
