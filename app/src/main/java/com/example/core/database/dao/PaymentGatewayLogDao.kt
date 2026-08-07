package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.PaymentGatewayLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentGatewayLogDao {
    @Query("SELECT * FROM payment_gateway_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<PaymentGatewayLogEntity>>

    @Query("SELECT * FROM payment_gateway_logs WHERE paymentId = :paymentId ORDER BY timestamp DESC")
    fun getLogsByPaymentId(paymentId: Long): Flow<List<PaymentGatewayLogEntity>>

    @Query("SELECT * FROM payment_gateway_logs WHERE provider = :provider ORDER BY timestamp DESC")
    fun getLogsByProvider(provider: String): Flow<List<PaymentGatewayLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PaymentGatewayLogEntity): Long
}
