package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.SessionPolicyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionPolicyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePolicy(policy: SessionPolicyEntity): Long

    @Query("SELECT * FROM session_policies WHERE policyId = :policyId LIMIT 1")
    suspend fun getPolicyById(policyId: String): SessionPolicyEntity?

    @Query("SELECT * FROM session_policies WHERE policyId = :policyId LIMIT 1")
    fun getPolicyByIdFlow(policyId: String): Flow<SessionPolicyEntity?>

    @Query("SELECT * FROM session_policies LIMIT 1")
    fun getDefaultPolicyFlow(): Flow<SessionPolicyEntity?>

    @Query("SELECT * FROM session_policies LIMIT 1")
    suspend fun getDefaultPolicy(): SessionPolicyEntity?
}
