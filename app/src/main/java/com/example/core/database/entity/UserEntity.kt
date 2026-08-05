package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["mobileNumber"], unique = true),
        Index(value = ["userId"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val fullName: String,
    val displayName: String = "",
    val businessName: String,
    val designation: String = "Business Owner",
    val mobileNumber: String,
    val alternateNumber: String = "",
    val email: String = "",
    val dob: String = "",
    val gender: String = "",
    val languagePreference: String = "en",
    val timeZone: String = "UTC",
    val roleId: String = "ROLE_OWNER",
    val pinHash: String,
    val profileImage: String = "",
    val status: String = "ACTIVE",
    val lastLogin: Long = System.currentTimeMillis(),
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)

