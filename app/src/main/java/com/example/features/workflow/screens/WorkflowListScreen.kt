package com.example.features.workflow.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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

@Composable
fun WorkflowListScreen(
    viewModel: WorkflowViewModel,
    onOpenDesigner: () -> Unit
) {
    val workflows by viewModel.workflows.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredWorkflows = remember(workflows, searchQuery) {
        if (searchQuery.isBlank()) workflows
        else workflows.filter { it.name.contains(searchQuery, ignoreCase = true) || it.triggerType.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Workflows") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("workflow_search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Workflows (${filteredWorkflows.size})",
                style = MaterialTheme.typography.titleMedium
            )
            Button(
                onClick = { viewModel.triggerTestEvent("INVOICE_CREATED") },
                modifier = Modifier.testTag("trigger_test_event_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Simulate Event")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredWorkflows.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No workflows configured yet.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onOpenDesigner, modifier = Modifier.testTag("create_first_workflow_button")) {
                        Text("Create Workflow in Designer")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredWorkflows, key = { it.id }) { workflow ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workflow_card_${workflow.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (workflow.isActive) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = workflow.name,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Trigger: ${workflow.triggerType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = workflow.description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = workflow.isActive,
                                    onCheckedChange = { viewModel.toggleWorkflowStatus(workflow.id, it) },
                                    modifier = Modifier.testTag("workflow_switch_${workflow.id}")
                                )
                                IconButton(
                                    onClick = { viewModel.deleteWorkflow(workflow.id) },
                                    modifier = Modifier.testTag("workflow_delete_${workflow.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Workflow")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
