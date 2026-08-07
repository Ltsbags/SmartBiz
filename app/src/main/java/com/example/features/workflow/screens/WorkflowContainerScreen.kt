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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.features.workflow.WorkflowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowContainerScreen(
    viewModel: WorkflowViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Workflows", "Designer", "Rule Engine", "Approvals", "History", "AI Suggestions")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enterprise Workflow Automation") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("workflow_container_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth().testTag("workflow_tab_row")
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        modifier = Modifier.testTag("workflow_tab_$index")
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> WorkflowListScreen(viewModel = viewModel, onOpenDesigner = { selectedTabIndex = 1 })
                1 -> WorkflowDesignerScreen(viewModel = viewModel, onWorkflowSaved = { selectedTabIndex = 0 })
                2 -> RuleEditorScreen(viewModel = viewModel)
                3 -> ApprovalDashboardScreen(viewModel = viewModel)
                4 -> ExecutionHistoryScreen(viewModel = viewModel)
                5 -> AiSuggestionsTab(viewModel = viewModel, onApplySuggestion = { suggestion ->
                    viewModel.saveWorkflow(suggestion.title, suggestion.description, suggestion.triggerType)
                    selectedTabIndex = 0
                })
            }
        }
    }
}

@Composable
private fun AiSuggestionsTab(
    viewModel: WorkflowViewModel,
    onApplySuggestion: (com.example.services.workflow.models.AiWorkflowSuggestion) -> Unit
) {
    val suggestions = viewModel.aiSuggestions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = "AI Workflow Recommendations",
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = "AI suggestions require user approval before deployment. AI will never execute workflows directly.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(suggestions, key = { it.id }) { suggestion ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ai_suggestion_card_${suggestion.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(suggestion.title, style = MaterialTheme.typography.titleSmall)
                            Text(suggestion.category, style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(suggestion.description, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onApplySuggestion(suggestion) },
                            modifier = Modifier.fillMaxWidth().testTag("apply_ai_suggestion_${suggestion.id}")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Apply & Configure Workflow")
                        }
                    }
                }
            }
        }
    }
}
