package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.SecurityPolicyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityPolicyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolicy(policy: SecurityPolicyEntity): Long

    @Query("SELECT * FROM security_policies WHERE policyKey = :policyKey")
    suspend fun getPolicyByKey(policyKey: String): SecurityPolicyEntity?

    @Query("SELECT * FROM security_policies WHERE policyKey = :policyKey")
    fun getPolicyByKeyFlow(policyKey: String): Flow<SecurityPolicyEntity?>

    @Query("SELECT * FROM security_policies")
    fun getAllPoliciesFlow(): Flow<List<SecurityPolicyEntity>>

    @Query("SELECT * FROM security_policies")
    suspend fun getAllPoliciesList(): List<SecurityPolicyEntity>
}
