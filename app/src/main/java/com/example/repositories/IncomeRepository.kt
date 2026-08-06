package com.example.repositories

import com.example.core.database.dao.CashBookDao
import com.example.core.database.dao.IncomeDao
import com.example.core.database.entity.CashBookEntryEntity
import com.example.core.database.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

class IncomeRepository(
    private val incomeDao: IncomeDao,
    private val cashBookDao: CashBookDao
) {
    val allIncome: Flow<List<IncomeEntity>> = incomeDao.getAllIncome()
    val totalIncomeAmount: Flow<Double?> = incomeDao.getTotalIncome()

    fun getIncomeByDateRange(startDate: Long, endDate: Long): Flow<List<IncomeEntity>> {
        return incomeDao.getIncomeByDateRange(startDate, endDate)
    }

    fun getTotalIncome(): Flow<Double?> {
        return incomeDao.getTotalIncome()
    }

    suspend fun getIncomeById(id: Long): IncomeEntity? {
        return incomeDao.getIncomeById(id)
    }

    suspend fun saveIncome(income: IncomeEntity): Long {
        val incomeId: Long
        val incNumber = if (income.incomeNumber.isBlank()) {
            "INC-${System.currentTimeMillis().toString().takeLast(6)}"
        } else {
            income.incomeNumber
        }

        val entityToSave = income.copy(
            incomeNumber = incNumber,
            updatedDate = System.currentTimeMillis()
        )

        if (entityToSave.id == 0L) {
            incomeId = incomeDao.insertIncome(entityToSave)
        } else {
            incomeDao.updateIncome(entityToSave)
            incomeId = entityToSave.id
            cashBookDao.deleteEntryBySource("INCOME", incomeId)
        }

        // Automatic Cash Book integration for Income
        val latestCashEntry = cashBookDao.getLatestEntry()
        val currentBalance = latestCashEntry?.balanceAfter ?: 0.0
        val newBalance = currentBalance + entityToSave.amount

        val cashEntry = CashBookEntryEntity(
            entryDate = entityToSave.incomeDate,
            entryType = "CASH_IN",
            sourceType = "INCOME",
            referenceId = incomeId,
            referenceNumber = incNumber,
            entityName = entityToSave.customerName.ifBlank { entityToSave.category },
            description = "${entityToSave.category}: ${entityToSave.notes}".trimEnd(':', ' '),
            amount = entityToSave.amount,
            paymentMode = entityToSave.paymentMode,
            balanceAfter = newBalance,
            createdDate = System.currentTimeMillis()
        )
        cashBookDao.insertEntry(cashEntry)

        return incomeId
    }

    suspend fun deleteIncome(income: IncomeEntity) {
        incomeDao.deleteIncome(income)
        cashBookDao.deleteEntryBySource("INCOME", income.id)
    }
}
