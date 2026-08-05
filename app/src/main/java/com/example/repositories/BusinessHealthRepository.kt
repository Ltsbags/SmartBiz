package com.example.repositories

import com.example.core.database.dao.BusinessHealthDao
import com.example.core.database.entity.BusinessHealthEntity
import kotlinx.coroutines.flow.Flow

class BusinessHealthRepository(
    private val healthDao: BusinessHealthDao
) {

    val latestHealthFlow: Flow<BusinessHealthEntity?> = healthDao.getLatestHealthFlow()
    val healthHistoryFlow: Flow<List<BusinessHealthEntity>> = healthDao.getHealthHistoryFlow()

    suspend fun saveHealthRecord(record: BusinessHealthEntity): Long = healthDao.insertHealthRecord(record)

    suspend fun getLatestHealth(): BusinessHealthEntity? = healthDao.getLatestHealth()
}
