package com.example.repositories

import com.example.core.database.dao.CashBookDao
import com.example.core.database.dao.ExpenseDao
import com.example.core.database.entity.CashBookEntryEntity
import com.example.core.database.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val cashBookDao: CashBookDao
) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val totalExpensesAmount: Flow<Double?> = expenseDao.getTotalExpenses()

    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate)
    }

    fun getTotalExpenses(): Flow<Double?> {
        return expenseDao.getTotalExpenses()
    }

    suspend fun getExpenseById(id: Long): ExpenseEntity? {
        return expenseDao.getExpenseById(id)
    }

    suspend fun saveExpense(expense: ExpenseEntity): Long {
        val expenseId: Long
        val expNumber = if (expense.expenseNumber.isBlank()) {
            "EXP-${System.currentTimeMillis().toString().takeLast(6)}"
        } else {
            expense.expenseNumber
        }

        val entityToSave = expense.copy(
            expenseNumber = expNumber,
            updatedDate = System.currentTimeMillis()
        )

        if (entityToSave.id == 0L) {
            expenseId = expenseDao.insertExpense(entityToSave)
        } else {
            expenseDao.updateExpense(entityToSave)
            expenseId = entityToSave.id
            // Clear existing Cash Book entry for this expense before recreating
            cashBookDao.deleteEntryBySource("EXPENSE", expenseId)
        }

        // Automatic Cash Book integration for paid expense amounts
        if (entityToSave.paidAmount > 0) {
            val latestCashEntry = cashBookDao.getLatestEntry()
            val currentBalance = latestCashEntry?.balanceAfter ?: 0.0
            val newBalance = currentBalance - entityToSave.paidAmount

            val cashEntry = CashBookEntryEntity(
                entryDate = entityToSave.expenseDate,
                entryType = "CASH_OUT",
                sourceType = "EXPENSE",
                referenceId = expenseId,
                referenceNumber = expNumber,
                entityName = entityToSave.payeeName.ifBlank { entityToSave.categoryName },
                description = "${entityToSave.categoryName}: ${entityToSave.notes}".trimEnd(':', ' '),
                amount = entityToSave.paidAmount,
                paymentMode = entityToSave.paymentMode,
                balanceAfter = newBalance,
                createdDate = System.currentTimeMillis()
            )
            cashBookDao.insertEntry(cashEntry)
        }

        return expenseId
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
        cashBookDao.deleteEntryBySource("EXPENSE", expense.id)
    }
}
