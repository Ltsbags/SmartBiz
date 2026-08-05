package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE mobileNumber = :mobile LIMIT 1")
    suspend fun getUserByMobile(mobile: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY id ASC LIMIT 1")
    suspend fun getPrimaryUser(): UserEntity?

    @Query("SELECT * FROM users ORDER BY id ASC LIMIT 1")
    fun getPrimaryUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersList(): List<UserEntity>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("UPDATE users SET pinHash = :newPinHash, updatedDate = :updatedAt WHERE userId = :userId")
    suspend fun updatePinHash(userId: String, newPinHash: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE users SET lastLogin = :loginTime WHERE userId = :userId")
    suspend fun updateLastLogin(userId: String, loginTime: Long = System.currentTimeMillis())
}
