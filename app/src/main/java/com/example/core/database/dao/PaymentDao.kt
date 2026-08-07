package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): PaymentEntity?

    @Query("SELECT * FROM payments WHERE paymentNumber = :paymentNumber LIMIT 1")
    suspend fun getPaymentByNumber(paymentNumber: String): PaymentEntity?

    @Query("SELECT * FROM payments WHERE invoiceId = :invoiceId ORDER BY timestamp DESC")
    fun getPaymentsByInvoiceId(invoiceId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getPaymentsByCustomerId(customerId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE status = :status ORDER BY timestamp DESC")
    fun getPaymentsByStatus(status: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE gatewayProvider = :provider ORDER BY timestamp DESC")
    fun getPaymentsByProvider(provider: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE isOfflineProcessed = 1 ORDER BY timestamp DESC")
    fun getOfflineProcessedPayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("SELECT SUM(amount) FROM payments WHERE status = 'SUCCESS'")
    fun getTotalCollectedAmount(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM payments")
    fun getPaymentCount(): Flow<Int>
}
