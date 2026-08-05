package com.example.services

import com.example.core.database.DatabaseHelper
import java.security.MessageDigest

class IntegrityService(
    private val dbHelper: DatabaseHelper,
    private val encryptionService: EncryptionService
) {

    data class IntegrityCheckResult(
        val isHealthy: Boolean,
        val databaseStatus: String,
        val tableCheckSummary: String,
        val checkTimestamp: Long = System.currentTimeMillis()
    )

    suspend fun verifyDatabaseIntegrity(): IntegrityCheckResult {
        return try {
            val userCount = dbHelper.userDao.getUserCount()

            IntegrityCheckResult(
                isHealthy = true,
                databaseStatus = "VERIFIED_OK",
                tableCheckSummary = "Primary tables operational. Query check passed. Registered Users: $userCount"
            )
        } catch (e: Exception) {
            IntegrityCheckResult(
                isHealthy = false,
                databaseStatus = "FAILED_INTEGRITY",
                tableCheckSummary = "Database query failure: ${e.message}"
            )
        }
    }

    fun calculateSha256Checksum(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun validateBackupFileHeader(encryptedBackupBytes: ByteArray, expectedChecksumHex: String? = null): Boolean {
        if (encryptedBackupBytes.size < 32) return false

        if (!expectedChecksumHex.isNullOrBlank()) {
            val actualChecksum = calculateSha256Checksum(encryptedBackupBytes)
            if (!actualChecksum.equals(expectedChecksumHex, ignoreCase = true)) {
                return false
            }
        }

        return try {
            val decrypted = encryptionService.decryptBytes(encryptedBackupBytes)
            decrypted.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }
}
