package com.example.features.communication.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.features.communication.viewmodel.CommunicationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunicationContainerScreen(
    viewModel: CommunicationViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showComposeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userNotice) {
        uiState.userNotice?.let { notice ->
            snackbarHostState.showSnackbar(notice)
            viewModel.clearNotice()
        }
    }

    val tabs = listOf("Activity Log", "Templates", "Automation Rules")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Communication Engine") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("communication_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showComposeDialog = true },
                modifier = Modifier.testTag("compose_communication_fab")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Compose Message")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                modifier = Modifier.testTag("communication_tab_row")
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.setTab(index) },
                        text = { Text(title) },
                        modifier = Modifier.testTag("communication_tab_$index")
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState.selectedTab) {
                    0 -> CommunicationDashboardScreen(
                        messages = uiState.messages,
                        channelFilter = uiState.channelFilter,
                        statusFilter = uiState.statusFilter,
                        onSelectChannelFilter = { viewModel.setChannelFilter(it) },
                        onSelectStatusFilter = { viewModel.setStatusFilter(it) },
                        onRetryMessage = { viewModel.retryMessage(context, it) }
                    )
                    1 -> TemplateManagerScreen(
                        templates = uiState.templates,
                        onSaveTemplate = { viewModel.saveTemplate(it) },
                        onDeleteTemplate = { viewModel.deleteTemplate(it) }
                    )
                    2 -> AutomationRulesScreen(
                        rules = uiState.automationRules,
                        onToggleRule = { ruleId, isEnabled -> viewModel.toggleAutomationRule(ruleId, isEnabled) }
                    )
                }
            }
        }
    }

    if (showComposeDialog) {
        CommunicationComposeDialog(
            templates = uiState.templates,
            onDismiss = { showComposeDialog = false },
            onSendDirect = { channel, recipient, name, subject, body ->
                viewModel.sendQuickMessage(context, channel, recipient, name, subject, body)
            },
            onSendTemplated = { templateId, channel, recipient, name, variables ->
                viewModel.sendTemplatedMessage(context, templateId, channel, recipient, name, variables)
            }
        )
    }
}
