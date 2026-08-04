package com.example.repositories

import com.example.core.database.dao.ExpenseCategoryDao
import com.example.core.database.entity.ExpenseCategoryEntity
import kotlinx.coroutines.flow.Flow

class ExpenseCategoryRepository(
    private val expenseCategoryDao: ExpenseCategoryDao
) {
    val allCategories: Flow<List<ExpenseCategoryEntity>> = expenseCategoryDao.getAllCategories()

    suspend fun getCategoryById(id: Long): ExpenseCategoryEntity? {
        return expenseCategoryDao.getCategoryById(id)
    }

    suspend fun saveCategory(category: ExpenseCategoryEntity): Long {
        return if (category.id == 0L) {
            expenseCategoryDao.insertCategory(category)
        } else {
            expenseCategoryDao.updateCategory(category)
            category.id
        }
    }

    suspend fun deleteCategory(category: ExpenseCategoryEntity) {
        if (!category.isSystemDefault) {
            expenseCategoryDao.deleteCategory(category)
        }
    }
}
