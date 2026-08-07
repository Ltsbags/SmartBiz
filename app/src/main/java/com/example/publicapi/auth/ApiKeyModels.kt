package com.example.publicapi.auth

enum class ApiEnvironment {
    LIVE, TEST
}

enum class RateLimitTier(val requestsPerMinute: Int) {
    FREE(60),
    PRO(1000),
    ENTERPRISE(10000)
}

enum class ApiKeyStatus {
    ACTIVE, REVOKED, EXPIRED
}

data class ApiKey(
    val id: String,
    val name: String,
    val keyHash: String,
    val prefix: String, // e.g. "sb_live_a1b2..."
    val environment: ApiEnvironment = ApiEnvironment.LIVE,
    val scopes: List<String> = emptyList(),
    val ipWhitelist: List<String> = emptyList(),
    val rateLimitTier: RateLimitTier = RateLimitTier.FREE,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = 0L, // 0 = Never
    val status: ApiKeyStatus = ApiKeyStatus.ACTIVE,
    val lastUsedAt: Long = 0L,
    val usageCount: Long = 0L
)

data class OAuthClient(
    val clientId: String,
    val clientName: String,
    val clientSecretHash: String,
    val redirectUris: List<String>,
    val allowedScopes: List<String>,
    val isConfidential: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE"
)

data class OAuthToken(
    val accessToken: String,
    val refreshToken: String,
    val clientId: String,
    val scopes: List<String>,
    val expiresInSeconds: Long = 3600L, // 1 hour
    val issuedAt: Long = System.currentTimeMillis(),
    val tokenType: String = "Bearer"
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > (issuedAt + expiresInSeconds * 1000)
}
