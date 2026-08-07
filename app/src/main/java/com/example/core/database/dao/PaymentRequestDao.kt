package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.PaymentRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentRequestDao {
    @Query("SELECT * FROM payment_requests ORDER BY createdAt DESC")
    fun getAllPaymentRequests(): Flow<List<PaymentRequestEntity>>

    @Query("SELECT * FROM payment_requests WHERE id = :id")
    suspend fun getPaymentRequestById(id: Long): PaymentRequestEntity?

    @Query("SELECT * FROM payment_requests WHERE requestNumber = :requestNumber LIMIT 1")
    suspend fun getPaymentRequestByNumber(requestNumber: String): PaymentRequestEntity?

    @Query("SELECT * FROM payment_requests WHERE invoiceId = :invoiceId ORDER BY createdAt DESC")
    fun getRequestsByInvoiceId(invoiceId: Long): Flow<List<PaymentRequestEntity>>

    @Query("SELECT * FROM payment_requests WHERE status = :status ORDER BY createdAt DESC")
    fun getRequestsByStatus(status: String): Flow<List<PaymentRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentRequest(request: PaymentRequestEntity): Long

    @Update
    suspend fun updatePaymentRequest(request: PaymentRequestEntity)

    @Query("UPDATE payment_requests SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deletePaymentRequest(request: PaymentRequestEntity)
}
