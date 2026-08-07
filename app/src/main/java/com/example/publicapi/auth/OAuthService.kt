package com.example.publicapi.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

class OAuthService {

    private val random = SecureRandom()
    private val activeTokens = mutableMapOf<String, OAuthToken>()

    fun registerClient(
        clientName: String,
        redirectUris: List<String>,
        allowedScopes: List<String>
    ): Pair<OAuthClient, String> {
        val clientId = "client_${UUID.randomUUID().toString().take(16)}"
        val rawSecret = generateRandomString(40)
        val secretHash = hashSecret(rawSecret)

        val client = OAuthClient(
            clientId = clientId,
            clientName = clientName,
            clientSecretHash = secretHash,
            redirectUris = redirectUris,
            allowedScopes = allowedScopes,
            isConfidential = true,
            createdAt = System.currentTimeMillis()
        )

        return Pair(client, rawSecret)
    }

    fun authenticateClient(client: OAuthClient, rawSecret: String): Boolean {
        if (client.status != "ACTIVE") return false
        return hashSecret(rawSecret) == client.clientSecretHash
    }

    fun issueAccessToken(
        client: OAuthClient,
        requestedScopes: List<String>
    ): OAuthToken {
        val validScopes = requestedScopes.filter { client.allowedScopes.contains(it) || client.allowedScopes.contains("*") }
        val accessToken = "sbo_at_${UUID.randomUUID().toString().replace("-", "")}"
        val refreshToken = "sbo_rt_${UUID.randomUUID().toString().replace("-", "")}"

        val token = OAuthToken(
            accessToken = accessToken,
            refreshToken = refreshToken,
            clientId = client.clientId,
            scopes = if (validScopes.isEmpty()) client.allowedScopes else validScopes,
            expiresInSeconds = 3600L,
            issuedAt = System.currentTimeMillis()
        )

        activeTokens[accessToken] = token
        return token
    }

    fun validateAccessToken(accessToken: String, requiredScope: String? = null): TokenValidationResult {
        val token = activeTokens[accessToken]
            ?: return TokenValidationResult.Invalid("Token not found or revoked")

        if (token.isExpired()) {
            activeTokens.remove(accessToken)
            return TokenValidationResult.Invalid("Token expired")
        }

        if (!requiredScope.isNullOrBlank() && !token.scopes.contains(requiredScope) && !token.scopes.contains("*")) {
            return TokenValidationResult.Invalid("Token missing scope: $requiredScope")
        }

        return TokenValidationResult.Valid(token)
    }

    fun revokeToken(accessToken: String) {
        activeTokens.remove(accessToken)
    }

    private fun hashSecret(secret: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(secret.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
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

sealed class TokenValidationResult {
    data class Valid(val token: OAuthToken) : TokenValidationResult()
    data class Invalid(val reason: String) : TokenValidationResult()
}
