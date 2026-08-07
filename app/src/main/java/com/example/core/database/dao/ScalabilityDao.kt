package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.CacheMetricsEntity
import com.example.core.database.entity.PerformanceBenchmarkEntity
import com.example.core.database.entity.QueueJobEntity
import com.example.core.database.entity.SystemHealthMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScalabilityDao {

    // Cache Metrics
    @Query("SELECT * FROM cache_metrics")
    fun getAllCacheMetrics(): Flow<List<CacheMetricsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCacheMetrics(metrics: CacheMetricsEntity)

    // Queue Jobs
    @Query("SELECT * FROM queue_jobs ORDER BY createdAt DESC")
    fun getAllQueueJobs(): Flow<List<QueueJobEntity>>

    @Query("SELECT * FROM queue_jobs WHERE queueName = :queueName ORDER BY createdAt DESC")
    fun getJobsByQueue(queueName: String): Flow<List<QueueJobEntity>>

    @Query("SELECT COUNT(*) FROM queue_jobs WHERE status = :status")
    fun getJobCountByStatus(status: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: QueueJobEntity)

    @Update
    suspend fun updateJob(job: QueueJobEntity)

    @Query("DELETE FROM queue_jobs WHERE status = 'COMPLETED'")
    suspend fun purgeCompletedJobs()

    // System Health Metrics
    @Query("SELECT * FROM system_health_metrics")
    fun getSystemHealthMetrics(): Flow<List<SystemHealthMetricEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHealthMetric(metric: SystemHealthMetricEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHealthMetrics(metrics: List<SystemHealthMetricEntity>)

    // Performance Benchmarks
    @Query("SELECT * FROM performance_benchmarks")
    fun getPerformanceBenchmarks(): Flow<List<PerformanceBenchmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBenchmark(benchmark: PerformanceBenchmarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBenchmarks(benchmarks: List<PerformanceBenchmarkEntity>)
}
