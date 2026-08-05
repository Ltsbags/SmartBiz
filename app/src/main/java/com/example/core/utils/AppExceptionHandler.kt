package com.example.core.utils

import android.database.sqlite.SQLiteException

object AppExceptionHandler {

    fun getErrorMessage(throwable: Throwable): String {
        AppLogger.e("AppExceptionHandler", "Handled exception: ${throwable.localizedMessage}", throwable)

        return when (throwable) {
            is SQLiteException -> "Database operation failed. Please try again or restart the app."
            is java.io.IOException -> "File storage access error. Please check storage permissions."
            is IllegalArgumentException -> throwable.message ?: "Invalid parameters provided."
            is IllegalStateException -> throwable.message ?: "Application state error."
            else -> throwable.localizedMessage ?: "An unexpected error occurred. Please try again."
        }
    }

    suspend fun <T> safeDatabaseCall(
        actionName: String,
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            AppLogger.e("DatabaseError", "Error during $actionName", e)
            Result.failure(e)
        }
    }
}
