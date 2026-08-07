package com.example.features.bi.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.entity.KpiDefinitionEntity
import com.example.core.database.entity.ReportDefinitionEntity
import com.example.repositories.AppRepositoryProvider
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReportBuilderScreen(
    onNavigateBack: () -> Unit = {}
) {
    val repositoryProvider = AppRepositoryProvider.getInstance()
    val reportingRepo = repositoryProvider.reportingRepository
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Export Builder, 1: KPI Designer, 2: Saved Snapshots

    val savedSnapshots by reportingRepo.savedSnapshots.collectAsState(initial = emptyList())
    val activeKpis by reportingRepo.activeKpis.collectAsState(initial = emptyList())

    // Export Builder State
    var reportTitle by remember { mutableStateOf("Custom Financial Overview") }
    var selectedCategory by remember { mutableStateOf("EXECUTIVE") }
    var selectedFormat by remember { mutableStateOf("JSON") }
    var isGenerating by remember { mutableStateOf(false) }

    // KPI Designer State
    var showAddKpiDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report & KPI Designer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_bi_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Export Builder") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("KPI Designer") }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Saved Snapshots (${savedSnapshots.size})") }
                )
            }

            when (selectedTabIndex) {
                0 -> {
                    // Export Builder Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Generate Enterprise Report Snapshot",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = reportTitle,
                            onValueChange = { reportTitle = it },
                            label = { Text("Report Title") },
                            modifier = Modifier.fillMaxWidth().testTag("report_title_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { selectedFormat = "JSON" },
                                modifier = Modifier.weight(1f).testTag("format_json_btn"),
                                colors = if (selectedFormat == "JSON") androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text("JSON Export")
                            }

                            OutlinedButton(
                                onClick = { selectedFormat = "CSV" },
                                modifier = Modifier.weight(1f).testTag("format_csv_btn"),
                                colors = if (selectedFormat == "CSV") androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text("CSV Export")
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    isGenerating = true
                                    val end = System.currentTimeMillis()
                                    val start = end - (30L * 86400000L)
                                    val snapshot = reportingRepo.generateCustomReport(
                                        title = reportTitle,
                                        category = selectedCategory,
                                        startDate = start,
                                        endDate = end,
                                        format = selectedFormat
                                    )
                                    isGenerating = false
                                    snackbarHostState.showSnackbar("Report snapshot #${snapshot.id} generated!")
                                    selectedTabIndex = 2
                                }
                            },
                            enabled = !isGenerating && reportTitle.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("generate_report_btn")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isGenerating) "Generating..." else "Generate & Save Snapshot")
                        }
                    }
                }

                1 -> {
                    // KPI Designer Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Configured KPI Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { showAddKpiDialog = true },
                                modifier = Modifier.testTag("add_kpi_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text("New KPI")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(activeKpis) { kpi ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(kpi.name, fontWeight = FontWeight.Bold)
                                        Text("Category: ${kpi.category} | Code: ${kpi.code}", style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Target: ${kpi.targetValue}", style = MaterialTheme.typography.bodyMedium)
                                            Text("Warning: ${kpi.warningThreshold}", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Saved Snapshots Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(savedSnapshots) { snap ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(snap.reportTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            snap.format,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(snap.generatedAt))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        snap.snapshotDataJson.take(200) + if (snap.snapshotDataJson.length > 200) "..." else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddKpiDialog) {
        CreateKpiDialog(
            onDismiss = { showAddKpiDialog = false },
            onSave = { newKpi ->
                scope.launch {
                    reportingRepo.saveKpi(newKpi)
                    showAddKpiDialog = false
                    snackbarHostState.showSnackbar("New KPI Created!")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateKpiDialog(
    onDismiss: () -> Unit,
    onSave: (KpiDefinitionEntity) -> Unit
) {
    var code by remember { mutableStateOf("KPI_CUSTOM_${System.currentTimeMillis() % 1000}") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("SALES") }
    var targetStr by remember { mutableStateOf("10000.0") }
    var warningStr by remember { mutableStateOf("5000.0") }
    var formatType by remember { mutableStateOf("CURRENCY") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Define New Enterprise KPI") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("KPI Name") },
                    modifier = Modifier.fillMaxWidth().testTag("kpi_name_input")
                )
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Target Value") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = warningStr,
                    onValueChange = { warningStr = it },
                    label = { Text("Warning Threshold") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val kpi = KpiDefinitionEntity(
                            code = code,
                            name = name,
                            category = category,
                            targetValue = targetStr.toDoubleOrNull() ?: 10000.0,
                            warningThreshold = warningStr.toDoubleOrNull() ?: 5000.0,
                            formatType = formatType
                        )
                        onSave(kpi)
                    }
                },
                modifier = Modifier.testTag("save_kpi_btn")
            ) {
                Text("Save KPI")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
