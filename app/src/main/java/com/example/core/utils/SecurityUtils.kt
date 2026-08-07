package com.example.core.utils

import java.security.MessageDigest

object SecurityUtils {

    private const val PIN_SALT = "BillNovaSecurePinSalt_2026_V1"

    /**
     * Generates a secure SHA-256 hash for a given 4-digit or 6-digit PIN.
     * Prevents plain-text PIN storage in local database or SharedPreferences.
     */
    fun hashPin(pin: String): String {
        val saltedInput = "$PIN_SALT:$pin"
        val bytes = MessageDigest.getInstance("SHA-256").digest(saltedInput.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies if the provided plain PIN matches the stored SHA-256 PIN hash.
     */
    fun verifyPin(pin: String, storedHash: String): Boolean {
        if (pin.isBlank() || storedHash.isBlank()) return false
        val computedHash = hashPin(pin)
        return computedHash.equals(storedHash, ignoreCase = true)
    }

    /**
     * Generates a random secure UUID string for sessions or transaction IDs.
     */
    fun generateSecureToken(): String {
        return java.util.UUID.randomUUID().toString()
    }
}
