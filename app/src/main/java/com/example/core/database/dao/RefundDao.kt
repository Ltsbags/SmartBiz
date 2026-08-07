package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.RefundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RefundDao {
    @Query("SELECT * FROM refunds ORDER BY createdAt DESC")
    fun getAllRefunds(): Flow<List<RefundEntity>>

    @Query("SELECT * FROM refunds WHERE id = :id")
    suspend fun getRefundById(id: Long): RefundEntity?

    @Query("SELECT * FROM refunds WHERE paymentId = :paymentId ORDER BY createdAt DESC")
    fun getRefundsByPaymentId(paymentId: Long): Flow<List<RefundEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefund(refund: RefundEntity): Long

    @Update
    suspend fun updateRefund(refund: RefundEntity)

    @Delete
    suspend fun deleteRefund(refund: RefundEntity)
}
