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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@Composable
fun ApprovalDashboardScreen(
    viewModel: WorkflowViewModel
) {
    val pendingApprovals by viewModel.pendingApprovals.collectAsState()
    val allApprovals by viewModel.allApprovals.collectAsState()

    var notesText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Enterprise Approval Governance Dashboard",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Pending Approvals (${pendingApprovals.size})",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (pendingApprovals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "No pending approval requests.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.height(260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pendingApprovals, key = { it.id }) { approval ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("approval_card_${approval.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Workflow: ${approval.workflowName}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Requested by: ${approval.requesterName} | Required Role: ${approval.requiredRole}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = notesText,
                                onValueChange = { notesText = it },
                                label = { Text("Approver Notes") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("approval_notes_input_${approval.id}")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.approveRequest(approval.id, "Manager User", notesText) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("approve_button_${approval.id}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Text("Approve")
                                }

                                OutlinedButton(
                                    onClick = { viewModel.rejectRequest(approval.id, "Manager User", notesText) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("reject_button_${approval.id}")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                    Text("Reject")
                                }

                                OutlinedButton(
                                    onClick = { viewModel.escalateRequest(approval.id, "DIRECTOR") },
                                    modifier = Modifier.testTag("escalate_button_${approval.id}")
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null)
                                    Text("Escalate")
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Approval Audit History",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allApprovals, key = { it.id }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.workflowName, style = MaterialTheme.typography.bodyMedium)
                            Text("Status: ${item.status} by ${item.approvedBy ?: "System"}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(item.status, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
