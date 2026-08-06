package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.DataAccessPolicyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DataAccessPolicyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePolicy(policy: DataAccessPolicyEntity): Long

    @Query("SELECT * FROM data_access_policies WHERE policyId = :policyId LIMIT 1")
    suspend fun getPolicyById(policyId: String): DataAccessPolicyEntity?

    @Query("SELECT * FROM data_access_policies WHERE policyId = :policyId LIMIT 1")
    fun getPolicyByIdFlow(policyId: String): Flow<DataAccessPolicyEntity?>

    @Query("SELECT * FROM data_access_policies WHERE roleId = :roleId OR roleId = 'ALL' LIMIT 1")
    suspend fun getPolicyForRole(roleId: String): DataAccessPolicyEntity?

    @Query("SELECT * FROM data_access_policies")
    fun getAllPoliciesFlow(): Flow<List<DataAccessPolicyEntity>>
}
