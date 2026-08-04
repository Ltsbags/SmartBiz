package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers WHERE isArchived = 0 ORDER BY supplierName ASC")
    fun getAllActiveSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers ORDER BY supplierName ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: Long): SupplierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    @Query("UPDATE suppliers SET isArchived = :isArchived, updatedDate = :updatedDate WHERE id = :id")
    suspend fun setArchivedStatus(id: Long, isArchived: Boolean, updatedDate: Long = System.currentTimeMillis())

    @Query("UPDATE suppliers SET outstandingBalance = outstandingBalance + :amount, updatedDate = :updatedDate WHERE id = :id")
    suspend fun updateOutstandingBalance(id: Long, amount: Double, updatedDate: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM suppliers WHERE isArchived = 0")
    fun getSupplierCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM suppliers")
    suspend fun getRawSupplierCount(): Int
}
