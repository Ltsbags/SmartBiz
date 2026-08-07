package com.example.features.payment.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.entity.PaymentEntity
import com.example.core.database.entity.PaymentGatewayLogEntity
import com.example.core.database.entity.PaymentRequestEntity
import com.example.core.database.entity.RefundEntity
import com.example.features.payment.PaymentEngineViewModel
import com.example.services.payment.models.PaymentEngineRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDashboardScreen(
    viewModel: PaymentEngineViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val payments by viewModel.payments.collectAsState()
    val paymentRequests by viewModel.paymentRequests.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val refunds by viewModel.refunds.collectAsState()
    val totalCollected by viewModel.totalCollected.collectAsState()
    val reconciliationReport by viewModel.reconciliationReport.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showDirectPaymentDialog by remember { mutableStateOf(false) }
    var paymentToRefund by remember { mutableStateOf<PaymentEntity?>(null) }
    var selectedFilterProvider by remember { mutableStateOf("ALL") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Payment Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "UPI • Razorpay • Stripe • Offline Cash",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showConfigDialog = true },
                        modifier = Modifier.testTag("gateway_settings_btn")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Gateway Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Metrics Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Total Collected",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "₹${String.format("%.2f", totalCollected)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Active Requests",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${paymentRequests.count { it.status == "ACTIVE" }} Active",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Total Payments",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "${payments.size} Txns",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Quick Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showLinkDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_payment_link_btn")
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Link / QR", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { showDirectPaymentDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("record_direct_payment_btn")
                ) {
                    Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Record Pay", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { viewModel.reconcileGateway("UPI") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reconcile_gateway_btn")
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reconcile", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Tabs
            val tabs = listOf("Transactions", "Requests", "Reconciliation", "Refunds", "Gateway Logs")
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("tab_$index")
                    )
                }
            }

            // Provider Filter Bar for Transactions Tab
            if (selectedTab == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL", "UPI", "RAZORPAY", "STRIPE", "CASH").forEach { p ->
                        FilterChip(
                            selected = selectedFilterProvider == p,
                            onClick = { selectedFilterProvider = p },
                            label = { Text(p, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // Content Area
            Box(modifier = Modifier.fillMaxSize()) {
                val filteredPayments = remember(payments, selectedFilterProvider) {
                    if (selectedFilterProvider == "ALL") payments
                    else payments.filter { it.paymentMethod.equals(selectedFilterProvider, ignoreCase = true) || it.gatewayProvider.equals(selectedFilterProvider, ignoreCase = true) }
                }

                when (selectedTab) {
                    0 -> TransactionsList(
                        payments = filteredPayments,
                        onInitiateRefund = { paymentToRefund = it }
                    )
                    1 -> PaymentRequestsList(paymentRequests = paymentRequests)
                    2 -> ReconciliationView(
                        report = reconciliationReport,
                        onRunReconcile = { viewModel.reconcileGateway("UPI") }
                    )
                    3 -> RefundsList(refunds = refunds)
                    4 -> GatewayLogsList(logs = logs)
                }
            }
        }
    }

    // Dialog Modals
    if (showConfigDialog) {
        PaymentGatewayConfigDialog(
            onDismiss = { showConfigDialog = false },
            onSave = { config ->
                viewModel.configureProvider(config)
                showConfigDialog = false
            }
        )
    }

    if (showLinkDialog) {
        PaymentLinkDialog(
            onDismiss = { showLinkDialog = false },
            onGenerateLink = { req, exp ->
                viewModel.generatePaymentLink(req, exp)
                showLinkDialog = false
            },
            getQrBitmap = { payload -> viewModel.generateQrBitmap(payload) }
        )
    }

    if (showDirectPaymentDialog) {
        DirectPaymentRecordingDialog(
            onDismiss = { showDirectPaymentDialog = false },
            onProcess = { req ->
                viewModel.processPayment(req) { success ->
                    if (success) showDirectPaymentDialog = false
                }
            }
        )
    }

    paymentToRefund?.let { payment ->
        RefundDialog(
            payment = payment,
            onDismiss = { paymentToRefund = null },
            onConfirmRefund = { pId, amt, reason ->
                viewModel.processRefund(pId, amt, reason) { success ->
                    if (success) paymentToRefund = null
                }
            }
        )
    }
}

@Composable
fun TransactionsList(
    payments: List<PaymentEntity>,
    onInitiateRefund: (PaymentEntity) -> Unit
) {
    if (payments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No payments recorded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
        ) {
            items(payments, key = { it.id }) { payment ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when (payment.status) {
                                        "SUCCESS" -> MaterialTheme.colorScheme.primaryContainer
                                        "REFUNDED" -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = when (payment.status) {
                                    "SUCCESS" -> MaterialTheme.colorScheme.primary
                                    "REFUNDED" -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = payment.paymentNumber,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(
                                    containerColor = if (payment.isOfflineProcessed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                                ) {
                                    Text(if (payment.isOfflineProcessed) "OFFLINE" else payment.gatewayProvider)
                                }
                            }
                            Text(
                                text = "Customer: ${payment.customerName.ifBlank { "Walk-in Customer" }}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (payment.invoiceNumber.isNotBlank()) {
                                Text(
                                    text = "Invoice #${payment.invoiceNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(payment.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${payment.amount}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (payment.status == "REFUNDED") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = payment.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (payment.status == "SUCCESS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )

                            if (payment.status == "SUCCESS") {
                                TextButton(
                                    onClick = { onInitiateRefund(payment) },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Refund", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentRequestsList(paymentRequests: List<PaymentRequestEntity>) {
    if (paymentRequests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active payment requests", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
        ) {
            items(paymentRequests, key = { it.id }) { req ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = req.requestNumber,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Badge(
                                containerColor = when (req.status) {
                                    "ACTIVE" -> MaterialTheme.colorScheme.primary
                                    "COMPLETED" -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.error
                                }
                            ) {
                                Text(req.status)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Customer: ${req.customerName.ifBlank { "Direct Link" }} | Amount: ₹${req.amount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (req.paymentLinkUrl.isNotBlank()) {
                            Text(
                                text = req.paymentLinkUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReconciliationView(
    report: com.example.services.payment.models.ReconciliationReport?,
    onRunReconcile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = onRunReconcile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Run Settlement Reconciliation")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (report == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tap 'Run Settlement Reconciliation' to compare gateway logs with database ledger.")
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Reconciliation Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Gateway Settlement: ₹${report.totalGatewayAmount}")
                    Text("Total Local Database: ₹${report.totalLocalAmount}")
                    Text("Matched Transactions: ${report.matchedCount}", color = MaterialTheme.colorScheme.primary)
                    Text("Unmatched / Discrepancies: ${report.unmatchedCount}", color = if (report.unmatchedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (report.discrepancies.isNotEmpty()) {
                Text(
                    text = "Discrepancies Detected:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(report.discrepancies) { disc ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Type: ${disc.discrepancyType}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = disc.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("🎉 100% Reconciled! All gateway records match local ledger perfectly.")
                }
            }
        }
    }
}

@Composable
fun RefundsList(refunds: List<RefundEntity>) {
    if (refunds.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No refunds issued", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
        ) {
            items(refunds, key = { it.id }) { rfd ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = rfd.refundNumber,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ref Payment: ${rfd.paymentNumber} | Customer: ${rfd.customerName.ifBlank { "N/A" }}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Reason: ${rfd.reason}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "-₹${rfd.amount}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GatewayLogsList(logs: List<PaymentGatewayLogEntity>) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No gateway execution logs", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
        ) {
            items(logs, key = { it.id }) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${log.provider} [${log.eventType}]",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Status: ${log.statusCode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (log.statusCode == 200) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                        if (log.requestPayload.isNotBlank()) {
                            Text(
                                text = "Req: ${log.requestPayload}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (log.responsePayload.isNotBlank()) {
                            Text(
                                text = "Res: ${log.responsePayload}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (log.errorMessage.isNotBlank()) {
                            Text(
                                text = "Err: ${log.errorMessage}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectPaymentRecordingDialog(
    onDismiss: () -> Unit,
    onProcess: (PaymentEngineRequest) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("UPI") }
    var notes by remember { mutableStateOf("") }
    var methodDropdownExpanded by remember { mutableStateOf(false) }

    val methods = listOf("UPI", "RAZORPAY", "STRIPE", "CASH", "BANK_TRANSFER")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Received Payment", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (INR)") },
                    modifier = Modifier.fillMaxWidth().testTag("direct_amount_input")
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
                ExposedDropdownMenuBox(
                    expanded = methodDropdownExpanded,
                    onExpandedChange = { methodDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = method,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = methodDropdownExpanded,
                        onDismissRequest = { methodDropdownExpanded = false }
                    ) {
                        methods.forEach { m ->
                            DropdownMenuItem(text = { Text(m) }, onClick = { method = m; methodDropdownExpanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Transaction Ref") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onProcess(
                            PaymentEngineRequest(
                                amount = amt,
                                customerName = customerName,
                                invoiceNumber = invoiceNumber,
                                paymentMethod = method,
                                notes = notes,
                                isOfflineProcessed = method == "CASH" || method == "BANK_TRANSFER"
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("submit_direct_payment_btn")
            ) {
                Text("Save Payment & Update Accounting")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
