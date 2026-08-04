package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.core.database.entity.InvoiceEntity
import com.example.core.database.entity.InvoiceItemEntity
import com.example.core.database.entity.InvoiceWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Long): InvoiceEntity?

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceWithItemsById(id: Long): InvoiceWithItems?

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun getInvoiceWithItemsFlow(id: Long): Flow<InvoiceWithItems?>

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE status = 'COMPLETED' OR paymentStatus = 'PAID'")
    fun getTotalPaidRevenue(): Flow<Double?>

    @Query("SELECT SUM(balanceAmount) FROM invoices WHERE status != 'CANCELLED'")
    fun getTotalPendingAmount(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM invoices")
    fun getInvoiceCount(): Flow<Int>

    @Query("SELECT MAX(id) FROM invoices")
    suspend fun getMaxInvoiceId(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>): List<Long>

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteInvoiceItemsByInvoiceId(invoiceId: Long)

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: Long): List<InvoiceItemEntity>

    @Query("DELETE FROM invoices")
    suspend fun deleteAll()
}

