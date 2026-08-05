package com.example.services

import com.example.core.services.SharedPreferencesService

class SecureStorageService(
    private val prefsService: SharedPreferencesService,
    private val encryptionService: EncryptionService
) {

    companion object {
        private const val KEY_PIN_HASH = "sec_pin_hash_v1"
        private const val KEY_AUTH_TOKEN = "sec_auth_token_v1"
        private const val KEY_SESSION_KEY = "sec_session_key_v1"
        private const val KEY_ENCRYPTION_KEY = "sec_master_enc_key_v1"
        private const val KEY_BIOMETRIC_SETTING = "sec_biometric_setting_v1"
        private const val KEY_RECOVERY_KEY = "sec_recovery_key_v1"
    }

    fun storePinHash(pinHash: String) {
        val encrypted = encryptionService.encryptText(pinHash)
        prefsService.saveEncrypted(KEY_PIN_HASH, encrypted)
    }

    fun getPinHash(): String? {
        val raw = prefsService.getEncrypted(KEY_PIN_HASH)
        if (raw.isBlank()) return null
        return try {
            encryptionService.decryptText(raw)
        } catch (_: Exception) {
            null
        }
    }

    fun storeAuthToken(token: String) {
        val encrypted = encryptionService.encryptText(token)
        prefsService.saveEncrypted(KEY_AUTH_TOKEN, encrypted)
    }

    fun getAuthToken(): String? {
        val raw = prefsService.getEncrypted(KEY_AUTH_TOKEN)
        if (raw.isBlank()) return null
        return try {
            encryptionService.decryptText(raw)
        } catch (_: Exception) {
            null
        }
    }

    fun storeSessionKey(sessionKey: String) {
        val encrypted = encryptionService.encryptText(sessionKey)
        prefsService.saveEncrypted(KEY_SESSION_KEY, encrypted)
    }

    fun getSessionKey(): String? {
        val raw = prefsService.getEncrypted(KEY_SESSION_KEY)
        if (raw.isBlank()) return null
        return try {
            encryptionService.decryptText(raw)
        } catch (_: Exception) {
            null
        }
    }

    fun storeBiometricEnabled(enabled: Boolean) {
        val encrypted = encryptionService.encryptText(enabled.toString())
        prefsService.saveEncrypted(KEY_BIOMETRIC_SETTING, encrypted)
    }

    fun isBiometricEnabled(): Boolean {
        val raw = prefsService.getEncrypted(KEY_BIOMETRIC_SETTING)
        if (raw.isBlank()) return false
        return try {
            encryptionService.decryptText(raw).toBoolean()
        } catch (_: Exception) {
            false
        }
    }

    fun storeRecoveryKey(recoveryKey: String) {
        val encrypted = encryptionService.encryptText(recoveryKey)
        prefsService.saveEncrypted(KEY_RECOVERY_KEY, encrypted)
    }

    fun getRecoveryKey(): String? {
        val raw = prefsService.getEncrypted(KEY_RECOVERY_KEY)
        if (raw.isBlank()) return null
        return try {
            encryptionService.decryptText(raw)
        } catch (_: Exception) {
            null
        }
    }

    fun clearAllSecureData() {
        prefsService.saveEncrypted(KEY_PIN_HASH, "")
        prefsService.saveEncrypted(KEY_AUTH_TOKEN, "")
        prefsService.saveEncrypted(KEY_SESSION_KEY, "")
        prefsService.saveEncrypted(KEY_ENCRYPTION_KEY, "")
        prefsService.saveEncrypted(KEY_BIOMETRIC_SETTING, "")
        prefsService.saveEncrypted(KEY_RECOVERY_KEY, "")
    }
}
