package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.CashBookEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashBookDao {
    @Query("SELECT * FROM cash_book_entries ORDER BY entryDate DESC, id DESC")
    fun getAllEntries(): Flow<List<CashBookEntryEntity>>

    @Query("SELECT * FROM cash_book_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): CashBookEntryEntity?

    @Query("SELECT * FROM cash_book_entries WHERE entryType = :entryType ORDER BY entryDate DESC")
    fun getEntriesByType(entryType: String): Flow<List<CashBookEntryEntity>>

    @Query("SELECT * FROM cash_book_entries WHERE entryDate >= :startDate AND entryDate <= :endDate ORDER BY entryDate DESC")
    fun getEntriesByDateRange(startDate: Long, endDate: Long): Flow<List<CashBookEntryEntity>>

    @Query("SELECT * FROM cash_book_entries ORDER BY entryDate DESC, id DESC LIMIT 1")
    suspend fun getLatestEntry(): CashBookEntryEntity?

    @Query("SELECT SUM(amount) FROM cash_book_entries WHERE entryType = 'CASH_IN'")
    fun getTotalCashIn(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM cash_book_entries WHERE entryType = 'CASH_OUT'")
    fun getTotalCashOut(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CashBookEntryEntity): Long

    @Delete
    suspend fun deleteEntry(entry: CashBookEntryEntity)

    @Query("DELETE FROM cash_book_entries WHERE sourceType = :sourceType AND referenceId = :referenceId")
    suspend fun deleteEntryBySource(sourceType: String, referenceId: Long)
}
