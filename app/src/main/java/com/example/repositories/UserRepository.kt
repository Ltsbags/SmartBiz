package com.example.repositories

import com.example.core.database.dao.UserDao
import com.example.core.database.entity.UserEntity
import com.example.core.services.SharedPreferencesService
import com.example.core.utils.ValidationUtils
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao,
    private val prefsService: SharedPreferencesService
) {

    fun getPrimaryUserFlow(): Flow<UserEntity?> {
        return userDao.getPrimaryUserFlow()
    }

    fun getAllUsersFlow(): Flow<List<UserEntity>> {
        return userDao.getAllUsersFlow()
    }

    suspend fun getAllUsersList(): List<UserEntity> {
        return userDao.getAllUsersList()
    }

    suspend fun getPrimaryUser(): UserEntity? {
        return userDao.getPrimaryUser()
    }

    suspend fun updateUserProfile(
        userId: String,
        fullName: String,
        displayName: String,
        businessName: String,
        designation: String,
        mobileNumber: String,
        alternateNumber: String,
        email: String,
        dob: String,
        gender: String,
        languagePreference: String,
        timeZone: String
    ): Pair<Boolean, String> {
        if (!ValidationUtils.isValidName(fullName)) {
            return Pair(false, "Full name must be at least 2 characters")
        }
        if (!ValidationUtils.isValidPhone(mobileNumber)) {
            return Pair(false, "Invalid mobile phone number format")
        }
        if (alternateNumber.isNotBlank() && !ValidationUtils.isValidPhone(alternateNumber)) {
            return Pair(false, "Invalid alternate phone number format")
        }
        if (!ValidationUtils.isValidEmail(email)) {
            return Pair(false, "Invalid email address format")
        }

        val existingUser = userDao.getUserById(userId) ?: userDao.getPrimaryUser()
            ?: return Pair(false, "User profile not found")

        val updatedUser = existingUser.copy(
            fullName = fullName.trim(),
            displayName = displayName.ifBlank { fullName.trim() },
            businessName = businessName.trim().ifBlank { existingUser.businessName },
            designation = designation.trim(),
            mobileNumber = mobileNumber.trim(),
            alternateNumber = alternateNumber.trim(),
            email = email.trim(),
            dob = dob.trim(),
            gender = gender.trim(),
            languagePreference = languagePreference,
            timeZone = timeZone,
            updatedDate = System.currentTimeMillis()
        )

        userDao.updateUser(updatedUser)

        // Sync with SharedPreferences for quick app-wide UI consistency
        if (businessName.isNotBlank()) prefsService.businessName = businessName.trim()
        if (mobileNumber.isNotBlank()) prefsService.businessPhone = mobileNumber.trim()
        if (email.isNotBlank()) prefsService.businessEmail = email.trim()

        return Pair(true, "User profile updated successfully")
    }

    suspend fun updateProfileImage(userId: String, imagePath: String): Boolean {
        val user = userDao.getUserById(userId) ?: userDao.getPrimaryUser() ?: return false
        val updatedUser = user.copy(
            profileImage = imagePath,
            updatedDate = System.currentTimeMillis()
        )
        userDao.updateUser(updatedUser)
        return true
    }
}
