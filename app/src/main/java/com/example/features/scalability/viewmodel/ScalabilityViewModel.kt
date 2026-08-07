package com.example.features.scalability.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.DatabaseHelper
import com.example.features.scalability.models.BackgroundJob
import com.example.features.scalability.models.BenchmarkMetric
import com.example.features.scalability.models.CacheRegionInfo
import com.example.features.scalability.models.CapacityMetric
import com.example.features.scalability.models.PerformanceBudget
import com.example.features.scalability.models.QueueName
import com.example.features.scalability.models.ServiceHealthInfo
import com.example.features.scalability.repositories.ScalabilityRepository
import com.example.features.scalability.services.CacheService
import com.example.features.scalability.services.HealthMonitoringService
import com.example.features.scalability.services.PerformanceService
import com.example.features.scalability.services.QueueManagerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScalabilityUiState(
    val cacheRegions: List<CacheRegionInfo> = emptyList(),
    val queueJobs: List<BackgroundJob> = emptyList(),
    val systemHealthMetrics: List<ServiceHealthInfo> = emptyList(),
    val performanceBudgets: List<PerformanceBudget> = emptyList(),
    val capacityMetrics: List<CapacityMetric> = emptyList(),
    val overallSystemStatus: String = "HEALTHY",
    val activeTransactionsPerSec: Int = 1850,
    val peakMemoryUsageMb: Int = 245,
    val dbConnectionPoolActive: Int = 42,
    val dbConnectionPoolMax: Int = 100,
    val isLoading: Boolean = false
)

class ScalabilityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScalabilityRepository(DatabaseHelper.getInstance(application).scalabilityDao)
    private val performanceService = PerformanceService()
    private val cacheService = CacheService()
    private val queueManagerService = QueueManagerService()
    private val healthMonitoringService = HealthMonitoringService()

    private val _uiState = MutableStateFlow(ScalabilityUiState())
    val uiState: StateFlow<ScalabilityUiState> = _uiState.asStateFlow()

    init {
        loadScalabilityData()
    }

    private fun loadScalabilityData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.seedInitialScalabilityData()

            // Observe Cache
            viewModelScope.launch {
                repository.getCacheMetrics().collect { cacheList ->
                    _uiState.value = _uiState.value.copy(cacheRegions = cacheList)
                }
            }

            // Observe Queue
            viewModelScope.launch {
                repository.getQueueJobs().collect { jobList ->
                    _uiState.value = _uiState.value.copy(queueJobs = jobList)
                }
            }

            // Observe Health
            viewModelScope.launch {
                repository.getSystemHealthMetrics().collect { healthList ->
                    val overall = if (healthList.any { it.status.name == "UNHEALTHY" }) "DEGRADED" else "HEALTHY"
                    _uiState.value = _uiState.value.copy(
                        systemHealthMetrics = healthList,
                        overallSystemStatus = overall
                    )
                }
            }

            // Observe Benchmarks
            viewModelScope.launch {
                repository.getPerformanceBenchmarks().collect { benchmarkList ->
                    _uiState.value = _uiState.value.copy(
                        performanceBudgets = benchmarkList,
                        capacityMetrics = generateCapacityMetrics(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun triggerQueueJob(queueName: QueueName, jobType: String) {
        viewModelScope.launch {
            repository.enqueueJob(
                queueName = queueName,
                jobType = jobType,
                payloadJson = "{\"scheduledAt\":${System.currentTimeMillis()},\"priority\":\"HIGH\"}"
            )
        }
    }

    fun purgeCompletedJobs() {
        viewModelScope.launch {
            repository.purgeCompletedJobs()
        }
    }

    fun clearCacheRegion(regionName: String) {
        cacheService.clearRegion(regionName)
        viewModelScope.launch {
            // Refresh
            repository.seedInitialScalabilityData()
        }
    }

    fun runPerformanceBenchmark(metric: BenchmarkMetric) {
        viewModelScope.launch {
            val budgetMs = when (metric) {
                BenchmarkMetric.APP_STARTUP -> 1200L
                BenchmarkMetric.API_RESPONSE -> 200L
                BenchmarkMetric.DASHBOARD_LOAD -> 500L
                BenchmarkMetric.SYNC_TIME -> 2000L
                BenchmarkMetric.SEARCH_LATENCY -> 150L
                BenchmarkMetric.REPORT_GEN -> 3000L
            }

            performanceService.measureAndRecord(
                metric = metric,
                budgetMs = budgetMs,
                onRecorded = { m, b, duration ->
                    viewModelScope.launch {
                        repository.recordBenchmark(m.name, b, duration)
                    }
                },
                block = {
                    kotlinx.coroutines.delay((50..300).random().toLong())
                }
            )
        }
    }

    private fun generateCapacityMetrics(): List<CapacityMetric> {
        return listOf(
            CapacityMetric("App Server Cluster CPU", 34.5, 100.0, "%", 34.5),
            CapacityMetric("PostgreSQL Connection Pool", 42.0, 100.0, "conns", 42.0),
            CapacityMetric("Redis Memory Usage", 2.1, 8.0, "GB", 26.25),
            CapacityMetric("Object Storage IOPS", 1240.0, 5000.0, "IOPS", 24.8),
            CapacityMetric("Queue Worker Throughput", 450.0, 2000.0, "jobs/sec", 22.5)
        )
    }
}
