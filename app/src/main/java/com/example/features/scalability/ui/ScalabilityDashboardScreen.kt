package com.example.features.scalability.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.scalability.models.BenchmarkMetric
import com.example.features.scalability.models.CacheRegionInfo
import com.example.features.scalability.models.CapacityMetric
import com.example.features.scalability.models.CircuitState
import com.example.features.scalability.models.HealthStatus
import com.example.features.scalability.models.PerformanceBudget
import com.example.features.scalability.models.QueueName
import com.example.features.scalability.models.ServiceHealthInfo
import com.example.features.scalability.viewmodel.ScalabilityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScalabilityDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScalabilityViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedSection by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Enterprise Scalability & Performance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cluster Health • Cache Manager • Queue Worker • Latency Budget",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("scalability_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        modifier = Modifier.testTag("scalability_dashboard_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Segmented Section Navigation
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4),
                    icon = { Icon(Icons.Default.Speed, contentDescription = null) }
                ) {
                    Text("Health")
                }
                SegmentedButton(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4),
                    icon = { Icon(Icons.Default.Storage, contentDescription = null) }
                ) {
                    Text("Cache")
                }
                SegmentedButton(
                    selected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4),
                    icon = { Icon(Icons.Default.Queue, contentDescription = null) }
                ) {
                    Text("Queues")
                }
                SegmentedButton(
                    selected = selectedSection == 3,
                    onClick = { selectedSection = 3 },
                    shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4),
                    icon = { Icon(Icons.Default.Memory, contentDescription = null) }
                ) {
                    Text("Budgets")
                }
            }

            // Cluster Overview Header
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Cluster System Status: ${state.overallSystemStatus}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "High Availability • Multi-Instance Stateless Backend",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (state.overallSystemStatus == "HEALTHY") Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        ) {}
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricBadge("Txn/sec", "${state.activeTransactionsPerSec}")
                        MetricBadge("Memory", "${state.peakMemoryUsageMb} MB")
                        MetricBadge("DB Pool", "${state.dbConnectionPoolActive}/${state.dbConnectionPoolMax}")
                    }
                }
            }

            when (selectedSection) {
                0 -> SystemHealthSection(healthList = state.systemHealthMetrics)
                1 -> CacheStatisticsSection(
                    cacheRegions = state.cacheRegions,
                    onClearCache = { viewModel.clearCacheRegion(it) }
                )
                2 -> QueueMonitorSection(
                    jobs = state.queueJobs,
                    onEnqueueJob = { queue, type -> viewModel.triggerQueueJob(queue, type) },
                    onPurgeCompleted = { viewModel.purgeCompletedJobs() }
                )
                3 -> PerformanceBudgetsSection(
                    budgets = state.performanceBudgets,
                    capacityMetrics = state.capacityMetrics,
                    onRunBenchmark = { viewModel.runPerformanceBenchmark(it) }
                )
            }
        }
    }
}

@Composable
private fun MetricBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SystemHealthSection(healthList: List<ServiceHealthInfo>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Services & Fault Tolerance Circuit Breakers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Real-time service health, response latency, and automatic circuit breaker tripping thresholds.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            healthList.forEach { service ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(service.serviceName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Latency: ${service.latencyMs}ms • Success: ${service.successRatePercent}% • Conns: ${service.activeConnections}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (service.status) {
                                    HealthStatus.HEALTHY -> Color(0xFFE8F5E9)
                                    HealthStatus.DEGRADED -> Color(0xFFFFF3E0)
                                    HealthStatus.UNHEALTHY -> Color(0xFFFFEBEE)
                                }
                            ) {
                                Text(
                                    text = service.status.name,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (service.status) {
                                        HealthStatus.HEALTHY -> Color(0xFF2E7D32)
                                        HealthStatus.DEGRADED -> Color(0xFFE65100)
                                        HealthStatus.UNHEALTHY -> Color(0xFFC62828)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Circuit: ${service.circuitState}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CacheStatisticsSection(
    cacheRegions: List<CacheRegionInfo>,
    onClearCache: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Redis & L1 Memory Cache Strategy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Hit ratios, memory footprint, evicted keys, and regional invalidation controls.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            cacheRegions.forEach { region ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(region.regionName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onClearCache(region.regionName) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Purge Cache", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Hit Ratio: ${region.hitRatioPercent}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("Memory: ${region.memoryUsageKb} KB", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (region.hitRatioPercent / 100.0).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Hits: ${region.hitCount} • Misses: ${region.missCount} • Evicted: ${region.evictedKeys} • Latency: ${region.avgLatencyMs}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueMonitorSection(
    jobs: List<com.example.features.scalability.models.BackgroundJob>,
    onEnqueueJob: (QueueName, String) -> Unit,
    onPurgeCompleted: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Background Queue & DLQ Worker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Asynchronous job queues, retry handling, and Dead-Letter Queue (DLQ).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = onPurgeCompleted) {
                    Text("Purge Completed")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onEnqueueJob(QueueName.SYNC, "OFFLINE_BATCH_SYNC") }, modifier = Modifier.weight(1f)) {
                    Text("+ Sync Job", fontSize = 12.sp)
                }
                Button(onClick = { onEnqueueJob(QueueName.NOTIFICATION, "EMAIL_DISPATCH") }, modifier = Modifier.weight(1f)) {
                    Text("+ Notif Job", fontSize = 12.sp)
                }
            }

            jobs.forEach { job ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${job.queueName} • ${job.jobType}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (job.status.name) {
                                    "COMPLETED" -> Color(0xFFE8F5E9)
                                    "PROCESSING" -> Color(0xFFE3F2FD)
                                    "DEAD_LETTER" -> Color(0xFFFFEBEE)
                                    else -> Color(0xFFFFF3E0)
                                }
                            ) {
                                Text(
                                    text = job.status.name,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (job.status.name) {
                                        "COMPLETED" -> Color(0xFF2E7D32)
                                        "PROCESSING" -> Color(0xFF1565C0)
                                        "DEAD_LETTER" -> Color(0xFFC62828)
                                        else -> Color(0xFFE65100)
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Payload: ${job.payloadJson} • Retries: ${job.retryCount}/${job.maxRetries}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (job.errorMessage != null) {
                            Text(
                                text = "Error: ${job.errorMessage}",
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

@Composable
private fun PerformanceBudgetsSection(
    budgets: List<PerformanceBudget>,
    capacityMetrics: List<CapacityMetric>,
    onRunBenchmark: (BenchmarkMetric) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Performance Latency Budgets & Benchmarks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Strict latency targets and resource utilization constraints for 10M+ transaction scale.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            budgets.forEach { budget ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(budget.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Target Budget: <= ${budget.budgetMs}${budget.unit} • Actual: ${budget.actualMs}${budget.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (budget.isWithinBudget) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(onClick = { onRunBenchmark(budget.metric) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Benchmark", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            HorizontalDivider()

            Text("Capacity Planning & Resource Usage", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            capacityMetrics.forEach { capacity ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(capacity.resourceName, style = MaterialTheme.typography.bodySmall)
                        Text("${capacity.currentUsage} / ${capacity.maxCapacity} ${capacity.unit}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { (capacity.utilizationPercent / 100.0).toFloat() },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
