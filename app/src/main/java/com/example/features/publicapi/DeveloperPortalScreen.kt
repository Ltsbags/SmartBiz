package com.example.features.publicapi

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Webhook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.publicapi.auth.ApiKey
import com.example.publicapi.auth.RateLimitTier
import com.example.publicapi.webhooks.WebhookSubscription
import com.example.repositories.ApiRequestAuditLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperPortalScreen(
    viewModel: DeveloperPortalViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateKeyDialog by remember { mutableStateOf(false) }
    var showCreateWebhookDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Enterprise Public API Platform", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Developer Portal & Integration Hub", style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PrimaryTabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("API Keys") },
                    icon = { Icon(Icons.Default.Key, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Webhooks") },
                    icon = { Icon(Icons.Default.Webhook, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    text = { Text("Sandbox") },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    text = { Text("SDK & Docs") },
                    icon = { Icon(Icons.Default.Code, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    text = { Text("Audit Logs") },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = null) }
                )
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (uiState.selectedTab) {
                    0 -> ApiKeysTab(
                        keys = uiState.apiKeys,
                        onCreateKeyClick = { showCreateKeyDialog = true },
                        onRevokeKey = { viewModel.revokeApiKey(it) }
                    )
                    1 -> WebhooksTab(
                        webhooks = uiState.webhooks,
                        onCreateWebhookClick = { showCreateWebhookDialog = true }
                    )
                    2 -> ApiSandboxTab(
                        keys = uiState.apiKeys,
                        outputJson = uiState.sandboxOutputJson,
                        isLoading = uiState.isLoading,
                        onExecuteQuery = { endpoint, key -> viewModel.runSandboxQuery(endpoint, key) }
                    )
                    3 -> SdkAndDocsTab(
                        specJson = uiState.openApiSpecJson,
                        jsSnippet = uiState.sdkSnippetJs,
                        flutterSnippet = uiState.sdkSnippetFlutter,
                        kotlinSnippet = uiState.sdkSnippetKotlin
                    )
                    4 -> AuditLogsTab(logs = uiState.auditLogs)
                }
            }
        }
    }

    // Secret Display Dialog
    if (uiState.newlyCreatedKeySecret != null || uiState.newlyCreatedWebhookSecret != null) {
        val secret = uiState.newlyCreatedKeySecret ?: uiState.newlyCreatedWebhookSecret ?: ""
        val isKey = uiState.newlyCreatedKeySecret != null
        val clipboard = LocalClipboardManager.current

        AlertDialog(
            onDismissRequest = { viewModel.dismissSecretDialog() },
            title = { Text(if (isKey) "API Key Created" else "Webhook Secret Generated") },
            text = {
                Column {
                    Text("Please copy your secret key now. You will not be able to see it again!")
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = secret,
                            color = Color.Green,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(secret))
                        viewModel.dismissSecretDialog()
                    },
                    modifier = Modifier.testTag("copy_secret_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy & Close")
                }
            }
        )
    }

    // Create Key Dialog
    if (showCreateKeyDialog) {
        CreateApiKeyDialog(
            onDismiss = { showCreateKeyDialog = false },
            onCreate = { name, tier, scopes ->
                viewModel.createApiKey(name, tier, scopes)
                showCreateKeyDialog = false
            }
        )
    }

    // Create Webhook Dialog
    if (showCreateWebhookDialog) {
        CreateWebhookDialog(
            onDismiss = { showCreateWebhookDialog = false },
            onCreate = { url, events ->
                viewModel.registerWebhook(url, events)
                showCreateWebhookDialog = false
            }
        )
    }
}

@Composable
private fun ApiKeysTab(
    keys: List<ApiKey>,
    onCreateKeyClick: () -> Unit,
    onRevokeKey: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("API Credentials", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Manage API Keys for external apps and partners", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onCreateKeyClick, modifier = Modifier.testTag("create_key_button")) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Generate Key")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (keys.isEmpty()) {
            Text("No API keys found. Create one to get started.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(keys) { key ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(key.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = key.status.name,
                                        color = if (key.status == com.example.publicapi.auth.ApiKeyStatus.ACTIVE) Color(0xFF2E7D32) else Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .background(
                                                if (key.status == com.example.publicapi.auth.ApiKeyStatus.ACTIVE) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Prefix: ${key.prefix}", fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                Text("Tier: ${key.rateLimitTier.name} (${key.rateLimitTier.requestsPerMinute} req/min)", fontSize = 12.sp)
                                Text("Scopes: ${key.scopes.joinToString(", ")}", fontSize = 12.sp, color = Color.Gray)
                            }

                            if (key.status == com.example.publicapi.auth.ApiKeyStatus.ACTIVE) {
                                OutlinedButton(
                                    onClick = { onRevokeKey(key.id) },
                                    modifier = Modifier.testTag("revoke_key_${key.id}")
                                ) {
                                    Text("Revoke", color = Color.Red)
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
private fun WebhooksTab(
    webhooks: List<WebhookSubscription>,
    onCreateWebhookClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Webhook Subscriptions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Receive real-time HTTP POST notifications on business events", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onCreateWebhookClick, modifier = Modifier.testTag("create_webhook_button")) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Webhook")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (webhooks.isEmpty()) {
            Text("No active webhooks. Register an endpoint to receive live events.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(webhooks) { sub ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(sub.targetUrl, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Events: ${sub.events.joinToString(", ")}", fontSize = 13.sp, color = Color.DarkGray)
                            Text("Secret: ${sub.secretKey.take(12)}...", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApiSandboxTab(
    keys: List<ApiKey>,
    outputJson: String,
    isLoading: Boolean,
    onExecuteQuery: (String, String) -> Unit
) {
    var selectedEndpoint by remember { mutableStateOf("GET /v1/invoices") }
    var selectedRawKey by remember { mutableStateOf("") }

    val endpoints = listOf(
        "GET /v1/invoices",
        "POST /v1/invoices",
        "GET /v1/customers",
        "GET /v1/inventory",
        "GET /v1/analytics/summary"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Interactive API Sandbox Explorer", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Test public API endpoints directly inside BillNova", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = selectedRawKey,
            onValueChange = { selectedRawKey = it },
            label = { Text("API Key (or select from existing)") },
            modifier = Modifier.fillMaxWidth().testTag("sandbox_key_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Select Endpoint:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            endpoints.take(3).forEach { ep ->
                OutlinedButton(
                    onClick = { selectedEndpoint = ep },
                    modifier = Modifier.testTag("ep_${ep.replace(" ", "_")}")
                ) {
                    Text(ep, fontSize = 11.sp, fontWeight = if (selectedEndpoint == ep) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onExecuteQuery(selectedEndpoint, selectedRawKey) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().testTag("execute_sandbox_button")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isLoading) "Executing..." else "Send API Request")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Response Inspector:", fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = if (outputJson.isBlank()) "// Response JSON output will appear here..." else outputJson,
                color = Color(0xFF00FF66),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SdkAndDocsTab(
    specJson: String,
    jsSnippet: String,
    flutterSnippet: String,
    kotlinSnippet: String
) {
    var docTab by remember { mutableStateOf(0) } // 0 = Node.js, 1 = Flutter, 2 = Kotlin, 3 = OpenAPI Spec

    Column(modifier = Modifier.fillMaxSize()) {
        Row {
            OutlinedButton(onClick = { docTab = 0 }) { Text("Node.js / JS") }
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedButton(onClick = { docTab = 1 }) { Text("Flutter") }
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedButton(onClick = { docTab = 2 }) { Text("Kotlin") }
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedButton(onClick = { docTab = 3 }) { Text("OpenAPI JSON") }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val content = when (docTab) {
            0 -> jsSnippet
            1 -> flutterSnippet
            2 -> kotlinSnippet
            else -> specJson
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = content,
                color = Color(0xFFE0E0E0),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AuditLogsTab(logs: List<ApiRequestAuditLog>) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Public API Request Audit Log", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Real-time telemetry of external client calls", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Text("No API requests logged yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logs.reversed()) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = log.httpMethod,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (log.httpMethod == "GET") Color.Blue else Color(0xFFE65100)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(log.endpoint, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                }
                                Text("Client: ${log.clientName} | IP: ${log.clientIp}", fontSize = 11.sp, color = Color.Gray)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${log.statusCode}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (log.statusCode < 300) Color(0xFF2E7D32) else Color.Red
                                )
                                Text("${log.responseTimeMs}ms | ${dateFormat.format(Date(log.timestamp))}", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateApiKeyDialog(
    onDismiss: () -> Unit,
    onCreate: (String, RateLimitTier, List<String>) -> Unit
) {
    var keyName by remember { mutableStateOf("") }
    var selectedTier by remember { mutableStateOf(RateLimitTier.FREE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate New API Key") },
        text = {
            Column {
                OutlinedTextField(
                    value = keyName,
                    onValueChange = { keyName = it },
                    label = { Text("App or Partner Name") },
                    modifier = Modifier.fillMaxWidth().testTag("key_name_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Rate Limit Tier:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RateLimitTier.values().forEach { tier ->
                        OutlinedButton(
                            onClick = { selectedTier = tier },
                            modifier = Modifier.testTag("tier_${tier.name}")
                        ) {
                            Text(tier.name, fontSize = 11.sp, fontWeight = if (selectedTier == tier) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(keyName.ifBlank { "Client App" }, selectedTier, listOf("read:all", "write:all")) },
                modifier = Modifier.testTag("confirm_create_key")
            ) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CreateWebhookDialog(
    onDismiss: () -> Unit,
    onCreate: (String, List<String>) -> Unit
) {
    var targetUrl by remember { mutableStateOf("https://") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Webhook") },
        text = {
            Column {
                OutlinedTextField(
                    value = targetUrl,
                    onValueChange = { targetUrl = it },
                    label = { Text("Target Endpoint URL") },
                    modifier = Modifier.fillMaxWidth().testTag("webhook_url_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(targetUrl, listOf("invoice.created", "customer.created", "inventory.low_stock")) },
                modifier = Modifier.testTag("confirm_create_webhook")
            ) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
