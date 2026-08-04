package com.example.shared.chips

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun InvoiceStatusChip(
    status: String,
    testTag: String = "invoice_status_chip",
    modifier: Modifier = Modifier
) {
    PaymentStatusChip(
        status = status,
        testTag = testTag,
        modifier = modifier
    )
}
