package com.example.features.communication.screens

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
import androidx.compose.material3.RadioButton
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
import com.example.core.database.entity.CommunicationTemplateEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunicationComposeDialog(
    templates: List<CommunicationTemplateEntity>,
    onDismiss: () -> Unit,
    onSendDirect: (channel: String, recipient: String, name: String, subject: String, body: String) -> Unit,
    onSendTemplated: (templateId: String, channel: String?, recipient: String, name: String, variables: Map<String, String>) -> Unit
) {
    var mode by remember { mutableStateOf("DIRECT") } // "DIRECT" or "TEMPLATE"
    var channel by remember { mutableStateOf("WHATSAPP") }
    var recipient by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    // Template state
    var selectedTemplate by remember { mutableStateOf(templates.firstOrNull()) }
    var templateExpanded by remember { mutableStateOf(false) }
    var varCustomerName by remember { mutableStateOf("John Doe") }
    var varInvoiceNumber by remember { mutableStateOf("INV-2026-001") }
    var varAmount by remember { mutableStateOf("₹5,400.00") }
    var varDueDate by remember { mutableStateOf("15-Aug-2026") }
    var varCompanyName by remember { mutableStateOf("SmartBiz Enterprise") }

    val channels = listOf("WHATSAPP", "EMAIL", "SMS", "PUSH", "TELEGRAM", "SLACK")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Compose Outbound Message", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                // Mode Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = mode == "DIRECT",
                        onClick = { mode = "DIRECT" },
                        modifier = Modifier.testTag("mode_direct_radio")
                    )
                    Text("Direct Message", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = mode == "TEMPLATE",
                        onClick = { mode = "TEMPLATE" },
                        modifier = Modifier.testTag("mode_template_radio")
                    )
                    Text("Use Template", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Recipient & Phone / Email
                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    label = { Text("Recipient Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("compose_recipient_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("Phone Number / Email / Target") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("compose_recipient_contact_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Channel Choice
                Text("Channel:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    channels.take(4).forEach { ch ->
                        TextButton(
                            onClick = { channel = ch },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = ch,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (channel == ch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (mode == "DIRECT") {
                    if (channel == "EMAIL") {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("compose_subject_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Message Body") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("compose_body_input")
                    )
                } else {
                    // Template Dropdown
                    ExposedDropdownMenuBox(
                        expanded = templateExpanded,
                        onExpandedChange = { templateExpanded = !templateExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTemplate?.name ?: "Select Template",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Template") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("template_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = templateExpanded,
                            onDismissRequest = { templateExpanded = false }
                        ) {
                            templates.forEach { tmpl ->
                                DropdownMenuItem(
                                    text = { Text("${tmpl.name} (${tmpl.channel})") },
                                    onClick = {
                                        selectedTemplate = tmpl
                                        templateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Template Variable Bindings:", style = MaterialTheme.typography.labelLarge)

                    OutlinedTextField(
                        value = varCustomerName,
                        onValueChange = { varCustomerName = it },
                        label = { Text("{{customer_name}}") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = varInvoiceNumber,
                        onValueChange = { varInvoiceNumber = it },
                        label = { Text("{{invoice_number}}") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = varAmount,
                        onValueChange = { varAmount = it },
                        label = { Text("{{amount}}") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = varDueDate,
                        onValueChange = { varDueDate = it },
                        label = { Text("{{due_date}}") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (mode == "DIRECT") {
                        onSendDirect(channel, recipient, recipientName, subject, body)
                    } else {
                        val tmpl = selectedTemplate
                        if (tmpl != null) {
                            val vars = mapOf(
                                "customer_name" to varCustomerName,
                                "invoice_number" to varInvoiceNumber,
                                "amount" to varAmount,
                                "due_date" to varDueDate,
                                "company_name" to varCompanyName
                            )
                            onSendTemplated(tmpl.templateId, channel, recipient, recipientName, vars)
                        }
                    }
                    onDismiss()
                },
                modifier = Modifier.testTag("send_communication_button")
            ) {
                Text("Dispatch")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_communication_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
