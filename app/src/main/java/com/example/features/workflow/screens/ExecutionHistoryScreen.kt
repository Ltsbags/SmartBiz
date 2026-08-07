package com.example.features.workflow.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.features.workflow.WorkflowViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExecutionHistoryScreen(
    viewModel: WorkflowViewModel
) {
    val executions by viewModel.executions.collectAsState()
    val automationHistory by viewModel.automationHistory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Realtime Execution Monitor & Audit Logs",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Filter Execution Logs") },
            modifier = Modifier.fillMaxWidth().testTag("execution_log_search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Workflow Executions Timeline (${executions.size})",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (executions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "No workflow execution history available yet.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(executions, key = { it.id }) { execution ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("execution_card_${execution.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = execution.workflowName,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = execution.status,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = when (execution.status) {
                                        "COMPLETED" -> MaterialTheme.colorScheme.primary
                                        "FAILED" -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.secondary
                                    }
                                )
                            }
                            Text(
                                text = "Trigger: ${execution.triggerEvent} | Started: ${dateFormat.format(Date(execution.startedAt))}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!execution.errorMessage.isNullOrBlank()) {
                                Text(
                                    text = "Error: ${execution.errorMessage}",
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
}
