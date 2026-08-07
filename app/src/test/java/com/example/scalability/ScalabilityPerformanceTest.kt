package com.example.scalability

import com.example.features.scalability.models.BenchmarkMetric
import com.example.features.scalability.models.CircuitState
import com.example.features.scalability.models.QueueName
import com.example.features.scalability.services.CacheService
import com.example.features.scalability.services.CircuitBreaker
import com.example.features.scalability.services.HealthMonitoringService
import com.example.features.scalability.services.PerformanceService
import com.example.features.scalability.services.QueueManagerService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ScalabilityPerformanceTest {

    private lateinit var performanceService: PerformanceService
    private lateinit var cacheService: CacheService
    private lateinit var queueManagerService: QueueManagerService
    private lateinit var healthMonitoringService: HealthMonitoringService

    @Before
    fun setUp() {
        performanceService = PerformanceService()
        cacheService = CacheService()
        queueManagerService = QueueManagerService()
        healthMonitoringService = HealthMonitoringService()
    }

    @Test
    fun testPerformanceMeasurement() = runBlocking {
        var recordedMetric: BenchmarkMetric? = null
        var recordedDuration: Long? = null

        val result = performanceService.measureAndRecord(
            metric = BenchmarkMetric.API_RESPONSE,
            budgetMs = 200L,
            onRecorded = { m, _, duration ->
                recordedMetric = m
                recordedDuration = duration
            },
            block = {
                kotlinx.coroutines.delay(20)
                "API_SUCCESS"
            }
        )

        assertEquals("API_SUCCESS", result)
        assertEquals(BenchmarkMetric.API_RESPONSE, recordedMetric)
        assertNotNull(recordedDuration)
        assertTrue(recordedDuration!! >= 15)
    }

    @Test
    fun testCacheHitMissTracking() {
        val region = "REPOSITORY"
        val key = "item_1001"

        // First call - Miss
        val val1 = cacheService.getOrPut(region, key) { "DataFromDb" }
        assertEquals("DataFromDb", val1)

        // Second call - Hit
        val val2 = cacheService.getOrPut(region, key) { "FreshData" }
        assertEquals("DataFromDb", val2) // Should return cached value

        val (hits, misses) = cacheService.getStatsForRegion(region)
        assertEquals(1L, hits)
        assertEquals(1L, misses)
    }

    @Test
    fun testCircuitBreakerTripping() = runBlocking {
        val circuitBreaker = CircuitBreaker(failureThreshold = 2, resetTimeoutMs = 1000L)

        // First failure
        try {
            circuitBreaker.execute<Unit> { throw RuntimeException("DB Conn Error 1") }
        } catch (_: Exception) {}
        assertEquals(CircuitState.CLOSED, circuitBreaker.getState())

        // Second failure -> Trips to OPEN
        try {
            circuitBreaker.execute<Unit> { throw RuntimeException("DB Conn Error 2") }
        } catch (_: Exception) {}
        assertEquals(CircuitState.OPEN, circuitBreaker.getState())

        // Third execution attempt blocked immediately without calling block
        try {
            circuitBreaker.execute { "ShouldNotReachHere" }
            fail("Should have thrown IllegalStateException because Circuit Breaker is OPEN")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("Circuit Breaker is OPEN"))
        }
    }

    @Test
    fun testQueueWorkerProcessing() = runBlocking {
        val status = queueManagerService.processNextJob(QueueName.SYNC) { payload ->
            payload.isNotEmpty()
        }

        assertEquals(com.example.features.scalability.models.JobStatus.COMPLETED, status)
        assertEquals(0, queueManagerService.getActiveWorkerCount(QueueName.SYNC))
    }
}
