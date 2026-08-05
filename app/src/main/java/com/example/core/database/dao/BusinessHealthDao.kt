package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.BusinessHealthEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessHealthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(record: BusinessHealthEntity): Long

    @Query("SELECT * FROM business_health ORDER BY calculatedDate DESC LIMIT 1")
    fun getLatestHealthFlow(): Flow<BusinessHealthEntity?>

    @Query("SELECT * FROM business_health ORDER BY calculatedDate DESC LIMIT 1")
    suspend fun getLatestHealth(): BusinessHealthEntity?

    @Query("SELECT * FROM business_health ORDER BY calculatedDate DESC LIMIT 30")
    fun getHealthHistoryFlow(): Flow<List<BusinessHealthEntity>>
}
