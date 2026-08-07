package com.example.features.scalability.services

import com.example.features.scalability.models.BenchmarkMetric
import com.example.features.scalability.models.CacheRegionInfo
import com.example.features.scalability.models.CircuitState
import com.example.features.scalability.models.HealthStatus
import com.example.features.scalability.models.JobStatus
import com.example.features.scalability.models.QueueName
import com.example.features.scalability.models.ServiceHealthInfo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class PerformanceService {
    private val metricsMap = ConcurrentHashMap<BenchmarkMetric, Long>()

    suspend fun <T> measureAndRecord(
        metric: BenchmarkMetric,
        budgetMs: Long,
        onRecorded: (BenchmarkMetric, Long, Long) -> Unit,
        block: suspend () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        try {
            return block()
        } finally {
            val duration = System.currentTimeMillis() - startTime
            metricsMap[metric] = duration
            onRecorded(metric, budgetMs, duration)
        }
    }

    fun getRecordedLatency(metric: BenchmarkMetric): Long? {
        return metricsMap[metric]
    }
}

class CacheService {
    private val memoryCache = ConcurrentHashMap<String, Any>()
    private val regionStats = ConcurrentHashMap<String, Pair<Long, Long>>() // Region to (Hits, Misses)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrPut(
        region: String,
        key: String,
        ttlMs: Long = 300000L,
        fetcher: () -> T
    ): T {
        val compositeKey = "$region:$key"
        val existing = memoryCache[compositeKey]
        return if (existing != null) {
            recordHit(region)
            existing as T
        } else {
            recordMiss(region)
            val computed = fetcher()
            memoryCache[compositeKey] = computed
            computed
        }
    }

    fun clearRegion(region: String) {
        val keysToRemove = memoryCache.keys.filter { it.startsWith("$region:") }
        keysToRemove.forEach { memoryCache.remove(it) }
    }

    fun clearAll() {
        memoryCache.clear()
        regionStats.clear()
    }

    private fun recordHit(region: String) {
        val current = regionStats[region] ?: Pair(0L, 0L)
        regionStats[region] = Pair(current.first + 1, current.second)
    }

    private fun recordMiss(region: String) {
        val current = regionStats[region] ?: Pair(0L, 0L)
        regionStats[region] = Pair(current.first, current.second + 1)
    }

    fun getStatsForRegion(region: String): Pair<Long, Long> {
        return regionStats[region] ?: Pair(0L, 0L)
    }
}

class QueueManagerService {
    private val activeWorkers = ConcurrentHashMap<QueueName, Int>()

    suspend fun processNextJob(
        queueName: QueueName,
        handler: suspend (String) -> Boolean
    ): JobStatus {
        activeWorkers[queueName] = (activeWorkers[queueName] ?: 0) + 1
        return try {
            val success = handler("SamplePayload")
            if (success) JobStatus.COMPLETED else JobStatus.FAILED
        } catch (e: Exception) {
            JobStatus.FAILED
        } finally {
            activeWorkers[queueName] = Math.max(0, (activeWorkers[queueName] ?: 1) - 1)
        }
    }

    fun getActiveWorkerCount(queueName: QueueName): Int {
        return activeWorkers[queueName] ?: 0
    }
}

class CircuitBreaker(
    private val failureThreshold: Int = 3,
    private val resetTimeoutMs: Long = 10000L
) {
    private var state: CircuitState = CircuitState.CLOSED
    private var failureCount = 0
    private var lastStateChange: Long = System.currentTimeMillis()
    private val mutex = Mutex()

    suspend fun <T> execute(block: suspend () -> T): T {
        mutex.withLock {
            if (state == CircuitState.OPEN) {
                if (System.currentTimeMillis() - lastStateChange > resetTimeoutMs) {
                    state = CircuitState.HALF_OPEN
                    lastStateChange = System.currentTimeMillis()
                } else {
                    throw IllegalStateException("Circuit Breaker is OPEN. Execution blocked for fault tolerance.")
                }
            }
        }

        return try {
            val result = block()
            mutex.withLock {
                if (state == CircuitState.HALF_OPEN) {
                    state = CircuitState.CLOSED
                    failureCount = 0
                    lastStateChange = System.currentTimeMillis()
                }
            }
            result
        } catch (e: Exception) {
            mutex.withLock {
                failureCount++
                if (failureCount >= failureThreshold) {
                    state = CircuitState.OPEN
                    lastStateChange = System.currentTimeMillis()
                }
            }
            throw e
        }
    }

    fun getState(): CircuitState = state
}

class HealthMonitoringService {
    private val circuitBreakers = ConcurrentHashMap<String, CircuitBreaker>()

    fun getCircuitBreaker(serviceName: String): CircuitBreaker {
        return circuitBreakers.getOrPut(serviceName) { CircuitBreaker() }
    }

    fun evaluateServiceHealth(
        serviceName: String,
        latencyMs: Long,
        successRatePercent: Double
    ): HealthStatus {
        val cb = getCircuitBreaker(serviceName)
        val cbState = cb.getState()

        return when {
            cbState == CircuitState.OPEN || successRatePercent < 90.0 || latencyMs > 2000 -> HealthStatus.UNHEALTHY
            cbState == CircuitState.HALF_OPEN || successRatePercent < 98.0 || latencyMs > 500 -> HealthStatus.DEGRADED
            else -> HealthStatus.HEALTHY
        }
    }
}
