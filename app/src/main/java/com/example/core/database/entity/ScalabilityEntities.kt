package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_metrics")
data class CacheMetricsEntity(
    @PrimaryKey val cacheRegion: String, // e.g. "REPOSITORY", "API_RESPONSE", "DASHBOARD", "LOCALIZATION"
    val hitCount: Long = 0,
    val missCount: Long = 0,
    val totalMemoryBytes: Long = 0,
    val evictedKeysCount: Long = 0,
    val avgLatencyMs: Double = 0.0,
    val lastClearedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "queue_jobs")
data class QueueJobEntity(
    @PrimaryKey val jobId: String,
    val queueName: String, // "SYNC", "NOTIFICATION", "COMMUNICATION", "WORKFLOW", "RETRY", "DEAD_LETTER"
    val jobType: String,
    val payloadJson: String,
    val status: String = "PENDING", // "PENDING", "PROCESSING", "COMPLETED", "FAILED", "DEAD_LETTER"
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_health_metrics")
data class SystemHealthMetricEntity(
    @PrimaryKey val serviceName: String, // e.g. "API_GATEWAY", "REDIS_CACHE", "POSTGRES_DB", "QUEUE_WORKER", "OBJECT_STORAGE"
    val status: String, // "HEALTHY", "DEGRADED", "UNHEALTHY"
    val latencyMs: Long,
    val successRatePercent: Double,
    val activeConnections: Int,
    val circuitBreakerState: String = "CLOSED", // "CLOSED", "HALF_OPEN", "OPEN"
    val lastCheckTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "performance_benchmarks")
data class PerformanceBenchmarkEntity(
    @PrimaryKey val metricName: String, // "APP_STARTUP", "API_RESPONSE", "DASHBOARD_LOAD", "SYNC_TIME", "SEARCH_LATENCY", "REPORT_GEN"
    val budgetMs: Long,
    val actualMs: Long,
    val status: String, // "PASSED", "WARNING", "VIOLATED"
    val sampleSize: Int = 1,
    val lastUpdated: Long = System.currentTimeMillis()
)
