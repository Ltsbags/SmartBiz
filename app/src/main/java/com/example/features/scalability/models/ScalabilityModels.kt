package com.example.features.scalability.models

data class CacheRegionInfo(
    val regionName: String, // e.g. "REPOSITORY", "API_RESPONSE", "DASHBOARD", "LOCALIZATION"
    val hitCount: Long,
    val missCount: Long,
    val memoryUsageKb: Long,
    val evictedKeys: Long,
    val avgLatencyMs: Double,
    val hitRatioPercent: Double
)

enum class QueueName {
    SYNC, NOTIFICATION, COMMUNICATION, WORKFLOW, RETRY, DEAD_LETTER
}

enum class JobStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, DEAD_LETTER
}

data class BackgroundJob(
    val jobId: String,
    val queueName: QueueName,
    val jobType: String,
    val payloadJson: String,
    val status: JobStatus,
    val retryCount: Int,
    val maxRetries: Int,
    val errorMessage: String?,
    val createdAt: Long,
    val updatedAt: Long
)

enum class HealthStatus {
    HEALTHY, DEGRADED, UNHEALTHY
}

enum class CircuitState {
    CLOSED, HALF_OPEN, OPEN
}

data class ServiceHealthInfo(
    val serviceName: String, // "API Gateway", "Redis Cache", "PostgreSQL DB", "Queue Worker", "Object Storage"
    val status: HealthStatus,
    val latencyMs: Long,
    val successRatePercent: Double,
    val activeConnections: Int,
    val circuitState: CircuitState,
    val lastCheckTimestamp: Long
)

enum class BenchmarkMetric {
    APP_STARTUP, API_RESPONSE, DASHBOARD_LOAD, SYNC_TIME, SEARCH_LATENCY, REPORT_GEN
}

data class PerformanceBudget(
    val metric: BenchmarkMetric,
    val displayName: String,
    val budgetMs: Long,
    val actualMs: Long,
    val isWithinBudget: Boolean,
    val unit: String = "ms"
)

data class CapacityMetric(
    val resourceName: String,
    val currentUsage: Double,
    val maxCapacity: Double,
    val unit: String,
    val utilizationPercent: Double
)
