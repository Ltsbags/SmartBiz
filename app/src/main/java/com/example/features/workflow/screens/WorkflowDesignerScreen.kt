package com.example.features.workflow.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.features.workflow.WorkflowViewModel
import com.example.services.workflow.models.WorkflowNode

@Composable
fun WorkflowDesignerScreen(
    viewModel: WorkflowViewModel,
    onWorkflowSaved: () -> Unit
) {
    var workflowName by remember { mutableStateOf("New Automation Workflow") }
    var workflowDescription by remember { mutableStateOf("Automated enterprise trigger rule") }
    var selectedTrigger by remember { mutableStateOf("INVOICE_CREATED") }

    val nodes = remember {
        mutableStateListOf(
            WorkflowNode("n1", "TRIGGER", "Trigger Event: $selectedTrigger"),
            WorkflowNode("n2", "CONDITION", "Check Amount > $1000"),
            WorkflowNode("n3", "ACTION", "Send WhatsApp Notification")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Visual No-Code Workflow Designer",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = workflowName,
            onValueChange = { workflowName = it },
            label = { Text("Workflow Name") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("designer_name_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    nodes.add(
                        WorkflowNode(
                            id = "n_${System.currentTimeMillis()}",
                            nodeType = "CONDITION",
                            name = "Condition Rule"
                        )
                    )
                },
                modifier = Modifier.testTag("add_condition_node_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add Condition")
            }

            Button(
                onClick = {
                    nodes.add(
                        WorkflowNode(
                            id = "n_${System.currentTimeMillis()}",
                            nodeType = "ACTION",
                            name = "Action Step"
                        )
                    )
                },
                modifier = Modifier.testTag("add_action_node_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add Action")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Workflow Execution Canvas Foundation",
            style = MaterialTheme.typography.labelLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(nodes) { index, node ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("designer_node_card_$index"),
                    colors = CardDefaults.cardColors(
                        containerColor = when (node.nodeType) {
                            "TRIGGER" -> MaterialTheme.colorScheme.primaryContainer
                            "CONDITION" -> MaterialTheme.colorScheme.secondaryContainer
                            "APPROVAL" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Step ${index + 1}: [${node.nodeType}]",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = node.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (index > 0) {
                            IconButton(
                                onClick = { nodes.removeAt(index) },
                                modifier = Modifier.testTag("remove_node_button_$index")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Node")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.saveWorkflow(
                    name = workflowName,
                    description = workflowDescription,
                    triggerType = selectedTrigger
                )
                onWorkflowSaved()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_workflow_button")
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save & Deploy Workflow")
        }
    }
}
