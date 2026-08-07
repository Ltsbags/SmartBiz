package com.example.features.communication.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.entity.CommunicationTemplateEntity
import com.example.features.communication.widgets.TemplateManagementWidget

@Composable
fun TemplateManagerScreen(
    templates: List<CommunicationTemplateEntity>,
    onSaveTemplate: (CommunicationTemplateEntity) -> Unit,
    onDeleteTemplate: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reusable Message Templates",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Supports {{customer_name}}, {{invoice_number}}, {{invoice_amount}}, {{due_date}}, {{outstanding_amount}}, etc.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_template_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Template")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (templates.isEmpty()) {
                Text(
                    text = "No communication templates configured.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(templates, key = { it.id }) { template ->
                        TemplateManagementWidget(
                            template = template,
                            onDelete = { onDeleteTemplate(template.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTemplateDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newTemplate ->
                onSaveTemplate(newTemplate)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddTemplateDialog(
    onDismiss: () -> Unit,
    onConfirm: (CommunicationTemplateEntity) -> Unit
) {
    var templateId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var channel by remember { mutableStateOf("WHATSAPP") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("BILLING") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Reusable Template") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_template_name_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = templateId,
                    onValueChange = { templateId = it.uppercase() },
                    label = { Text("Template ID (e.g. INV_DELIVERY)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_template_id_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = channel,
                    onValueChange = { channel = it.uppercase() },
                    label = { Text("Channel (WHATSAPP, EMAIL, SMS)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject Template (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Body Template (Use {{customer_name}}, {{invoice_amount}}, etc.)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("add_template_body_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && templateId.isNotBlank() && body.isNotBlank()) {
                        onConfirm(
                            CommunicationTemplateEntity(
                                templateId = templateId,
                                name = name,
                                channel = channel,
                                subjectTemplate = subject,
                                bodyTemplate = body,
                                category = category
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_add_template_button")
            ) {
                Text("Save Template")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
