package com.example.services

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptionService {

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_ALGORITHM = "AES"
        private const val KDF_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_LENGTH_BYTES = 12
        private const val SALT_LENGTH_BYTES = 16
        private const val ITERATIONS = 10000
        private const val KEY_SIZE_BITS = 256

        private const val DEFAULT_MASTER_SEED = "SmartBizEnterpriseSecuritySeed_V1_2026"
    }

    private var currentKeyVersion = 1
    private val random = SecureRandom()

    private fun deriveKey(secret: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(secret.toCharArray(), salt, ITERATIONS, KEY_SIZE_BITS)
        val factory = SecretKeyFactory.getInstance(KDF_ALGORITHM)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, KEY_ALGORITHM)
    }

    fun encryptText(plainText: String, masterPassphrase: String = DEFAULT_MASTER_SEED): String {
        if (plainText.isEmpty()) return ""
        val salt = ByteArray(SALT_LENGTH_BYTES).apply { random.nextBytes(this) }
        val iv = ByteArray(IV_LENGTH_BYTES).apply { random.nextBytes(this) }

        val key = deriveKey(masterPassphrase, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val payload = ByteArray(1 + SALT_LENGTH_BYTES + IV_LENGTH_BYTES + cipherText.size)
        payload[0] = currentKeyVersion.toByte()
        System.arraycopy(salt, 0, payload, 1, SALT_LENGTH_BYTES)
        System.arraycopy(iv, 0, payload, 1 + SALT_LENGTH_BYTES, IV_LENGTH_BYTES)
        System.arraycopy(cipherText, 0, payload, 1 + SALT_LENGTH_BYTES + IV_LENGTH_BYTES, cipherText.size)

        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    fun decryptText(encryptedPayloadBase64: String, masterPassphrase: String = DEFAULT_MASTER_SEED): String {
        if (encryptedPayloadBase64.isEmpty()) return ""
        val payload = Base64.decode(encryptedPayloadBase64, Base64.NO_WRAP)
        if (payload.size <= 1 + SALT_LENGTH_BYTES + IV_LENGTH_BYTES) return ""

        val salt = ByteArray(SALT_LENGTH_BYTES)
        val iv = ByteArray(IV_LENGTH_BYTES)
        val cipherText = ByteArray(payload.size - 1 - SALT_LENGTH_BYTES - IV_LENGTH_BYTES)

        System.arraycopy(payload, 1, salt, 0, SALT_LENGTH_BYTES)
        System.arraycopy(payload, 1 + SALT_LENGTH_BYTES, iv, 0, IV_LENGTH_BYTES)
        System.arraycopy(payload, 1 + SALT_LENGTH_BYTES + IV_LENGTH_BYTES, cipherText, 0, cipherText.size)

        val key = deriveKey(masterPassphrase, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

        val plainBytes = cipher.doFinal(cipherText)
        return String(plainBytes, Charsets.UTF_8)
    }

    fun encryptBytes(data: ByteArray, masterPassphrase: String = DEFAULT_MASTER_SEED): ByteArray {
        val salt = ByteArray(SALT_LENGTH_BYTES).apply { random.nextBytes(this) }
        val iv = ByteArray(IV_LENGTH_BYTES).apply { random.nextBytes(this) }

        val key = deriveKey(masterPassphrase, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val cipherBytes = cipher.doFinal(data)

        val output = ByteArray(1 + SALT_LENGTH_BYTES + IV_LENGTH_BYTES + cipherBytes.size)
        output[0] = currentKeyVersion.toByte()
        System.arraycopy(salt, 0, output, 1, SALT_LENGTH_BYTES)
        System.arraycopy(iv, 0, output, 1 + SALT_LENGTH_BYTES, IV_LENGTH_BYTES)
        System.arraycopy(cipherBytes, 0, output, 1 + SALT_LENGTH_BYTES + IV_LENGTH_BYTES, cipherBytes.size)

        return output
    }

    fun decryptBytes(encryptedData: ByteArray, masterPassphrase: String = DEFAULT_MASTER_SEED): ByteArray {
        if (encryptedData.size <= 1 + SALT_LENGTH_BYTES + IV_LENGTH_BYTES) {
            throw IllegalArgumentException("Invalid encrypted payload size")
        }

        val salt = ByteArray(SALT_LENGTH_BYTES)
        val iv = ByteArray(IV_LENGTH_BYTES)
        val cipherBytes = ByteArray(encryptedData.size - 1 - SALT_LENGTH_BYTES - IV_LENGTH_BYTES)

        System.arraycopy(encryptedData, 1, salt, 0, SALT_LENGTH_BYTES)
        System.arraycopy(encryptedData, 1 + SALT_LENGTH_BYTES, iv, 0, IV_LENGTH_BYTES)
        System.arraycopy(encryptedData, 1 + SALT_LENGTH_BYTES + IV_LENGTH_BYTES, cipherBytes, 0, cipherBytes.size)

        val key = deriveKey(masterPassphrase, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        return cipher.doFinal(cipherBytes)
    }

    fun rotateKey(): Int {
        currentKeyVersion++
        return currentKeyVersion
    }

    fun getKeyVersion(): Int = currentKeyVersion
}
