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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun RuleEditorScreen(
    viewModel: WorkflowViewModel
) {
    val rules by viewModel.rules.collectAsState()

    var ruleName by remember { mutableStateOf("") }
    var ruleField by remember { mutableStateOf("AMOUNT") }
    var ruleOperator by remember { mutableStateOf("GREATER_THAN") }
    var ruleValue by remember { mutableStateOf("5000") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Reusable Rule Engine Set Builder",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Create New Business Rule", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = ruleName,
                    onValueChange = { ruleName = it },
                    label = { Text("Rule Name") },
                    modifier = Modifier.fillMaxWidth().testTag("rule_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ruleField,
                        onValueChange = { ruleField = it },
                        label = { Text("Field (e.g. AMOUNT)") },
                        modifier = Modifier.weight(1f).testTag("rule_field_input")
                    )
                    OutlinedTextField(
                        value = ruleOperator,
                        onValueChange = { ruleOperator = it },
                        label = { Text("Operator (AND/OR/GT)") },
                        modifier = Modifier.weight(1f).testTag("rule_operator_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = ruleValue,
                    onValueChange = { ruleValue = it },
                    label = { Text("Target Value") },
                    modifier = Modifier.fillMaxWidth().testTag("rule_value_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (ruleName.isNotBlank()) {
                            viewModel.saveRule(ruleName, ruleField, ruleOperator, ruleValue)
                            ruleName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_rule_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Save Rule")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Configured Business Rules (${rules.size})",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rules, key = { it.id }) { rule ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("rule_card_${rule.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(rule.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Condition: IF ${rule.field} ${rule.operator} '${rule.value}'",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.deleteRule(rule.id) },
                            modifier = Modifier.testTag("delete_rule_${rule.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Rule")
                        }
                    }
                }
            }
        }
    }
}
