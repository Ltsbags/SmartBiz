package com.example.publicapi.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

class ApiKeyService {

    private val random = SecureRandom()

    fun generateApiKey(
        name: String,
        environment: ApiEnvironment = ApiEnvironment.LIVE,
        scopes: List<String> = listOf("read:all"),
        rateLimitTier: RateLimitTier = RateLimitTier.FREE,
        ipWhitelist: List<String> = emptyList(),
        expiresAt: Long = 0L
    ): Pair<ApiKey, String> {
        val envPrefix = if (environment == ApiEnvironment.LIVE) "sb_live_" else "sb_test_"
        val rawSecret = generateRandomString(32)
        val fullRawKey = "$envPrefix$rawSecret"
        val prefixDisplay = "$envPrefix${rawSecret.take(6)}..."
        val keyHash = hashKey(fullRawKey)

        val apiKey = ApiKey(
            id = UUID.randomUUID().toString(),
            name = name,
            keyHash = keyHash,
            prefix = prefixDisplay,
            environment = environment,
            scopes = scopes,
            ipWhitelist = ipWhitelist,
            rateLimitTier = rateLimitTier,
            createdAt = System.currentTimeMillis(),
            expiresAt = expiresAt,
            status = ApiKeyStatus.ACTIVE
        )

        return Pair(apiKey, fullRawKey)
    }

    fun hashKey(rawKey: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(rawKey.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun validateKey(
        rawKey: String,
        key: ApiKey,
        clientIp: String? = null,
        requiredScope: String? = null
    ): KeyValidationResult {
        if (key.status != ApiKeyStatus.ACTIVE) {
            return KeyValidationResult.Invalid("API Key is ${key.status}")
        }

        if (key.expiresAt > 0 && System.currentTimeMillis() > key.expiresAt) {
            return KeyValidationResult.Invalid("API Key has expired")
        }

        val computedHash = hashKey(rawKey)
        if (computedHash != key.keyHash) {
            return KeyValidationResult.Invalid("Invalid API Key credentials")
        }

        if (!clientIp.isNullOrBlank() && key.ipWhitelist.isNotEmpty()) {
            if (!key.ipWhitelist.contains(clientIp)) {
                return KeyValidationResult.Invalid("IP $clientIp is not whitelisted for this API key")
            }
        }

        if (!requiredScope.isNullOrBlank()) {
            if (!hasScope(key.scopes, requiredScope)) {
                return KeyValidationResult.Invalid("API Key lacks required scope: $requiredScope")
            }
        }

        return KeyValidationResult.Valid(key)
    }

    fun hasScope(grantedScopes: List<String>, requiredScope: String): Boolean {
        if (grantedScopes.contains("read:all") && requiredScope.startsWith("read:")) return true
        if (grantedScopes.contains("write:all") && requiredScope.startsWith("write:")) return true
        if (grantedScopes.contains("admin") || grantedScopes.contains("*")) return true
        return grantedScopes.contains(requiredScope)
    }

    private fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val sb = StringBuilder(length)
        for (i in 0 until length) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        return sb.toString()
    }
}

sealed class KeyValidationResult {
    data class Valid(val key: ApiKey) : KeyValidationResult()
    data class Invalid(val reason: String) : KeyValidationResult()
}
