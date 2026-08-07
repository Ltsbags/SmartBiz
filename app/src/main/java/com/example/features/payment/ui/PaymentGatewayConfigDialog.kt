package com.example.features.payment.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.example.services.payment.models.PaymentGatewayConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGatewayConfigDialog(
    onDismiss: () -> Unit,
    onSave: (PaymentGatewayConfig) -> Unit
) {
    var selectedProvider by remember { mutableStateOf("UPI") }
    var apiKey by remember { mutableStateOf("") }
    var secretKey by remember { mutableStateOf("") }
    var merchantId by remember { mutableStateOf("") }
    var upiVpa by remember { mutableStateOf("store@upi") }
    var upiName by remember { mutableStateOf("SmartBiz Store") }
    var isTestMode by remember { mutableStateOf(true) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val providers = listOf("UPI", "RAZORPAY", "STRIPE", "OFFLINE_CASH", "BANK_API")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Payment Gateway Settings",
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
                Text(
                    text = "Configure Provider Credentials",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedProvider,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Provider") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("provider_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        providers.forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider) },
                                onClick = {
                                    selectedProvider = provider
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedProvider == "UPI") {
                    OutlinedTextField(
                        value = upiVpa,
                        onValueChange = { upiVpa = it },
                        label = { Text("Store UPI VPA / ID") },
                        placeholder = { Text("merchant@upi") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upi_vpa_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = upiName,
                        onValueChange = { upiName = it },
                        label = { Text("Merchant / Store Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (selectedProvider in listOf("RAZORPAY", "STRIPE")) {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text(if (selectedProvider == "RAZORPAY") "Key ID" else "Publishable Key") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = secretKey,
                        onValueChange = { secretKey = it },
                        label = { Text(if (selectedProvider == "RAZORPAY") "Key Secret" else "Secret Key") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = merchantId,
                        onValueChange = { merchantId = it },
                        label = { Text("Merchant / Account ID (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sandbox / Test Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isTestMode,
                        onCheckedChange = { isTestMode = it },
                        modifier = Modifier.testTag("test_mode_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val config = PaymentGatewayConfig(
                        provider = selectedProvider,
                        apiKey = apiKey,
                        secretKey = secretKey,
                        merchantId = merchantId,
                        upiVpa = upiVpa,
                        upiName = upiName,
                        isTestMode = isTestMode
                    )
                    onSave(config)
                },
                modifier = Modifier.testTag("save_gateway_config_btn")
            ) {
                Text("Save Credentials")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
