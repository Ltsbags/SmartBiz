package com.example.repositories

import com.example.core.database.dao.CashBookDao
import com.example.core.database.entity.CashBookEntryEntity
import kotlinx.coroutines.flow.Flow

class CashBookRepository(
    private val cashBookDao: CashBookDao
) {
    val allEntries: Flow<List<CashBookEntryEntity>> = cashBookDao.getAllEntries()
    val totalCashIn: Flow<Double?> = cashBookDao.getTotalCashIn()
    val totalCashOut: Flow<Double?> = cashBookDao.getTotalCashOut()

    fun getEntriesByDateRange(startDate: Long, endDate: Long): Flow<List<CashBookEntryEntity>> {
        return cashBookDao.getEntriesByDateRange(startDate, endDate)
    }

    fun getEntriesByType(entryType: String): Flow<List<CashBookEntryEntity>> {
        return cashBookDao.getEntriesByType(entryType)
    }

    suspend fun addManualCashEntry(
        entryType: String, // CASH_IN or CASH_OUT
        amount: Double,
        entityName: String,
        description: String,
        paymentMode: String = "CASH",
        entryDate: Long = System.currentTimeMillis()
    ): Long {
        val latestEntry = cashBookDao.getLatestEntry()
        val currentBalance = latestEntry?.balanceAfter ?: 0.0
        val newBalance = if (entryType == "CASH_IN") {
            currentBalance + amount
        } else {
            currentBalance - amount
        }

        val refNo = if (entryType == "CASH_IN") "MAN-IN-${System.currentTimeMillis().toString().takeLast(5)}"
        else "MAN-OUT-${System.currentTimeMillis().toString().takeLast(5)}"

        val entry = CashBookEntryEntity(
            entryDate = entryDate,
            entryType = entryType,
            sourceType = "MANUAL_ADJUSTMENT",
            referenceNumber = refNo,
            entityName = entityName,
            description = description,
            amount = amount,
            paymentMode = paymentMode,
            balanceAfter = newBalance,
            createdDate = System.currentTimeMillis()
        )
        return cashBookDao.insertEntry(entry)
    }

    suspend fun deleteEntry(entry: CashBookEntryEntity) {
        cashBookDao.deleteEntry(entry)
    }
}
