package com.example.features.scalability.repositories

import com.example.core.database.dao.ScalabilityDao
import com.example.core.database.entity.CacheMetricsEntity
import com.example.core.database.entity.PerformanceBenchmarkEntity
import com.example.core.database.entity.QueueJobEntity
import com.example.core.database.entity.SystemHealthMetricEntity
import com.example.features.scalability.models.BackgroundJob
import com.example.features.scalability.models.BenchmarkMetric
import com.example.features.scalability.models.CacheRegionInfo
import com.example.features.scalability.models.CircuitState
import com.example.features.scalability.models.HealthStatus
import com.example.features.scalability.models.JobStatus
import com.example.features.scalability.models.PerformanceBudget
import com.example.features.scalability.models.QueueName
import com.example.features.scalability.models.ServiceHealthInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ScalabilityRepository(
    private val dao: ScalabilityDao
) {
    fun getCacheMetrics(): Flow<List<CacheRegionInfo>> {
        return dao.getAllCacheMetrics().map { list ->
            if (list.isEmpty()) getDefaultCacheMetrics()
            else list.map { it.toModel() }
        }
    }

    fun getQueueJobs(): Flow<List<BackgroundJob>> {
        return dao.getAllQueueJobs().map { list ->
            if (list.isEmpty()) getDefaultJobs()
            else list.map { it.toModel() }
        }
    }

    fun getSystemHealthMetrics(): Flow<List<ServiceHealthInfo>> {
        return dao.getSystemHealthMetrics().map { list ->
            if (list.isEmpty()) getDefaultHealthMetrics()
            else list.map { it.toModel() }
        }
    }

    fun getPerformanceBenchmarks(): Flow<List<PerformanceBudget>> {
        return dao.getPerformanceBenchmarks().map { list ->
            if (list.isEmpty()) getDefaultBenchmarks()
            else list.map { it.toModel() }
        }
    }

    suspend fun seedInitialScalabilityData() {
        val cacheSeeds = listOf(
            CacheMetricsEntity("REPOSITORY", hitCount = 14200, missCount = 850, totalMemoryBytes = 12500000, evictedKeysCount = 120, avgLatencyMs = 2.4),
            CacheMetricsEntity("API_RESPONSE", hitCount = 9800, missCount = 1200, totalMemoryBytes = 8400000, evictedKeysCount = 45, avgLatencyMs = 4.1),
            CacheMetricsEntity("DASHBOARD", hitCount = 6500, missCount = 310, totalMemoryBytes = 4200000, evictedKeysCount = 12, avgLatencyMs = 1.8),
            CacheMetricsEntity("LOCALIZATION", hitCount = 31000, missCount = 15, totalMemoryBytes = 1100000, evictedKeysCount = 0, avgLatencyMs = 0.5)
        )
        cacheSeeds.forEach { dao.insertOrUpdateCacheMetrics(it) }

        val now = System.currentTimeMillis()
        val jobSeeds = listOf(
            QueueJobEntity(UUID.randomUUID().toString(), "SYNC", "POS_TRANSACTION_SYNC", "{\"batchSize\":50}", "COMPLETED", 0, 3, null, now - 3600000, now - 3500000),
            QueueJobEntity(UUID.randomUUID().toString(), "NOTIFICATION", "INVOICE_EMAIL_DISPATCH", "{\"invoiceId\":\"INV-1002\"}", "COMPLETED", 0, 3, null, now - 1800000, now - 1790000),
            QueueJobEntity(UUID.randomUUID().toString(), "COMMUNICATION", "SMS_WHATSAPP_BROADCAST", "{\"recipients\":120}", "PROCESSING", 1, 3, null, now - 300000, now - 100000),
            QueueJobEntity(UUID.randomUUID().toString(), "WORKFLOW", "AUTO_APPROVAL_CHECK", "{\"entity\":\"PurchaseOrder\"}", "PENDING", 0, 3, null, now - 60000, now - 60000),
            QueueJobEntity(UUID.randomUUID().toString(), "DEAD_LETTER", "PAYMENT_GATEWAY_WEBHOOK", "{\"transactionId\":\"TXN_9981\"}", "DEAD_LETTER", 3, 3, "Gateway connection timeout (504)", now - 7200000, now - 7100000)
        )
        jobSeeds.forEach { dao.insertJob(it) }

        val healthSeeds = listOf(
            SystemHealthMetricEntity("API Gateway", "HEALTHY", 12, 99.98, 1420, "CLOSED"),
            SystemHealthMetricEntity("Redis Cache Cluster", "HEALTHY", 2, 100.0, 380, "CLOSED"),
            SystemHealthMetricEntity("PostgreSQL Database", "HEALTHY", 8, 99.95, 45, "CLOSED"),
            SystemHealthMetricEntity("Background Queue Worker", "HEALTHY", 15, 99.80, 12, "CLOSED"),
            SystemHealthMetricEntity("MinIO Object Storage", "DEGRADED", 85, 98.50, 8, "HALF_OPEN")
        )
        dao.insertAllHealthMetrics(healthSeeds)

        val benchmarkSeeds = listOf(
            PerformanceBenchmarkEntity("APP_STARTUP", budgetMs = 1200, actualMs = 680, status = "PASSED"),
            PerformanceBenchmarkEntity("API_RESPONSE", budgetMs = 200, actualMs = 45, status = "PASSED"),
            PerformanceBenchmarkEntity("DASHBOARD_LOAD", budgetMs = 500, actualMs = 210, status = "PASSED"),
            PerformanceBenchmarkEntity("SYNC_TIME", budgetMs = 2000, actualMs = 850, status = "PASSED"),
            PerformanceBenchmarkEntity("SEARCH_LATENCY", budgetMs = 150, actualMs = 38, status = "PASSED"),
            PerformanceBenchmarkEntity("REPORT_GEN", budgetMs = 3000, actualMs = 1120, status = "PASSED")
        )
        dao.insertAllBenchmarks(benchmarkSeeds)
    }

    suspend fun enqueueJob(queueName: QueueName, jobType: String, payloadJson: String) {
        val job = QueueJobEntity(
            jobId = UUID.randomUUID().toString(),
            queueName = queueName.name,
            jobType = jobType,
            payloadJson = payloadJson,
            status = "PENDING",
            retryCount = 0,
            maxRetries = 3
        )
        dao.insertJob(job)
    }

    suspend fun recordBenchmark(metricName: String, budgetMs: Long, actualMs: Long) {
        val status = if (actualMs <= budgetMs) "PASSED" else if (actualMs <= budgetMs * 1.5) "WARNING" else "VIOLATED"
        val benchmark = PerformanceBenchmarkEntity(
            metricName = metricName,
            budgetMs = budgetMs,
            actualMs = actualMs,
            status = status,
            lastUpdated = System.currentTimeMillis()
        )
        dao.insertOrUpdateBenchmark(benchmark)
    }

    suspend fun purgeCompletedJobs() {
        dao.purgeCompletedJobs()
    }

    private fun CacheMetricsEntity.toModel(): CacheRegionInfo {
        val totalReqs = hitCount + missCount
        val hitRatio = if (totalReqs > 0) (hitCount.toDouble() / totalReqs.toDouble()) * 100.0 else 0.0
        return CacheRegionInfo(
            regionName = cacheRegion,
            hitCount = hitCount,
            missCount = missCount,
            memoryUsageKb = totalMemoryBytes / 1024,
            evictedKeys = evictedKeysCount,
            avgLatencyMs = avgLatencyMs,
            hitRatioPercent = Math.round(hitRatio * 100.0) / 100.0
        )
    }

    private fun QueueJobEntity.toModel(): BackgroundJob {
        return BackgroundJob(
            jobId = jobId,
            queueName = try { QueueName.valueOf(queueName) } catch (_: Exception) { QueueName.SYNC },
            jobType = jobType,
            payloadJson = payloadJson,
            status = try { JobStatus.valueOf(status) } catch (_: Exception) { JobStatus.PENDING },
            retryCount = retryCount,
            maxRetries = maxRetries,
            errorMessage = errorMessage,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun SystemHealthMetricEntity.toModel(): ServiceHealthInfo {
        return ServiceHealthInfo(
            serviceName = serviceName,
            status = try { HealthStatus.valueOf(status) } catch (_: Exception) { HealthStatus.HEALTHY },
            latencyMs = latencyMs,
            successRatePercent = successRatePercent,
            activeConnections = activeConnections,
            circuitState = try { CircuitState.valueOf(circuitBreakerState) } catch (_: Exception) { CircuitState.CLOSED },
            lastCheckTimestamp = lastCheckTimestamp
        )
    }

    private fun PerformanceBenchmarkEntity.toModel(): PerformanceBudget {
        val enumMetric = try { BenchmarkMetric.valueOf(metricName) } catch (_: Exception) { BenchmarkMetric.APP_STARTUP }
        val displayName = when (enumMetric) {
            BenchmarkMetric.APP_STARTUP -> "App Startup Time"
            BenchmarkMetric.API_RESPONSE -> "API Latency (p99)"
            BenchmarkMetric.DASHBOARD_LOAD -> "Dashboard Render Time"
            BenchmarkMetric.SYNC_TIME -> "Offline Sync Duration"
            BenchmarkMetric.SEARCH_LATENCY -> "Global Search Speed"
            BenchmarkMetric.REPORT_GEN -> "BI Report Generation"
        }
        return PerformanceBudget(
            metric = enumMetric,
            displayName = displayName,
            budgetMs = budgetMs,
            actualMs = actualMs,
            isWithinBudget = actualMs <= budgetMs
        )
    }

    private fun getDefaultCacheMetrics(): List<CacheRegionInfo> {
        return listOf(
            CacheRegionInfo("REPOSITORY", 14200, 850, 12207, 120, 2.4, 94.35),
            CacheRegionInfo("API_RESPONSE", 9800, 1200, 8203, 45, 4.1, 89.09),
            CacheRegionInfo("DASHBOARD", 6500, 310, 4101, 12, 1.8, 95.45),
            CacheRegionInfo("LOCALIZATION", 31000, 15, 1074, 0, 0.5, 99.95)
        )
    }

    private fun getDefaultJobs(): List<BackgroundJob> {
        val now = System.currentTimeMillis()
        return listOf(
            BackgroundJob("JOB-101", QueueName.SYNC, "POS_TRANSACTION_SYNC", "{\"batch\":50}", JobStatus.COMPLETED, 0, 3, null, now - 3600000, now - 3500000),
            BackgroundJob("JOB-102", QueueName.NOTIFICATION, "INVOICE_EMAIL_DISPATCH", "{\"inv\":\"102\"}", JobStatus.COMPLETED, 0, 3, null, now - 1800000, now - 1790000),
            BackgroundJob("JOB-103", QueueName.COMMUNICATION, "SMS_WHATSAPP_BROADCAST", "{\"recipients\":120}", JobStatus.PROCESSING, 1, 3, null, now - 300000, now - 100000)
        )
    }

    private fun getDefaultHealthMetrics(): List<ServiceHealthInfo> {
        return listOf(
            ServiceHealthInfo("API Gateway", HealthStatus.HEALTHY, 12, 99.98, 1420, CircuitState.CLOSED, System.currentTimeMillis()),
            ServiceHealthInfo("Redis Cache Cluster", HealthStatus.HEALTHY, 2, 100.0, 380, CircuitState.CLOSED, System.currentTimeMillis()),
            ServiceHealthInfo("PostgreSQL Database", HealthStatus.HEALTHY, 8, 99.95, 45, CircuitState.CLOSED, System.currentTimeMillis()),
            ServiceHealthInfo("Background Queue Worker", HealthStatus.HEALTHY, 15, 99.80, 12, CircuitState.CLOSED, System.currentTimeMillis()),
            ServiceHealthInfo("MinIO Object Storage", HealthStatus.DEGRADED, 85, 98.50, 8, CircuitState.HALF_OPEN, System.currentTimeMillis())
        )
    }

    private fun getDefaultBenchmarks(): List<PerformanceBudget> {
        return listOf(
            PerformanceBudget(BenchmarkMetric.APP_STARTUP, "App Startup Time", 1200, 680, true),
            PerformanceBudget(BenchmarkMetric.API_RESPONSE, "API Latency (p99)", 200, 45, true),
            PerformanceBudget(BenchmarkMetric.DASHBOARD_LOAD, "Dashboard Render Time", 500, 210, true),
            PerformanceBudget(BenchmarkMetric.SYNC_TIME, "Offline Sync Duration", 2000, 850, true),
            PerformanceBudget(BenchmarkMetric.SEARCH_LATENCY, "Global Search Speed", 150, 38, true),
            PerformanceBudget(BenchmarkMetric.REPORT_GEN, "BI Report Generation", 3000, 1120, true)
        )
    }
}
