package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.CustomerLedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerLedgerDao {
    @Query("SELECT * FROM customer_ledgers WHERE customerId = :customerId ORDER BY transactionDate DESC")
    fun getLedgersForCustomer(customerId: Long): Flow<List<CustomerLedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedger(ledger: CustomerLedgerEntity): Long

    @Query("DELETE FROM customer_ledgers WHERE customerId = :customerId")
    suspend fun deleteLedgersForCustomer(customerId: Long)
}
