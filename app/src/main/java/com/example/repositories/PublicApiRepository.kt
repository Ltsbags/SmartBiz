package com.example.repositories

import com.example.publicapi.auth.ApiEnvironment
import com.example.publicapi.auth.ApiKey
import com.example.publicapi.auth.ApiKeyService
import com.example.publicapi.auth.ApiKeyStatus
import com.example.publicapi.auth.KeyValidationResult
import com.example.publicapi.auth.OAuthClient
import com.example.publicapi.auth.OAuthService
import com.example.publicapi.auth.OAuthToken
import com.example.publicapi.auth.RateLimitTier
import com.example.publicapi.ratelimit.RateLimitResult
import com.example.publicapi.ratelimit.RateLimiter
import com.example.publicapi.webhooks.WebhookDeliveryLog
import com.example.publicapi.webhooks.WebhookManagerService
import com.example.publicapi.webhooks.WebhookSubscription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

data class ApiRequestAuditLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val clientName: String,
    val endpoint: String,
    val httpMethod: String,
    val statusCode: Int,
    val responseTimeMs: Long,
    val clientIp: String,
    val timestamp: Long = System.currentTimeMillis()
)

class PublicApiRepository(
    val apiKeyService: ApiKeyService = ApiKeyService(),
    val oAuthService: OAuthService = OAuthService(),
    val rateLimiter: RateLimiter = RateLimiter(),
    val webhookManager: WebhookManagerService = WebhookManagerService()
) {

    private val apiKeysMap = ConcurrentHashMap<String, ApiKey>()
    private val oauthClientsMap = ConcurrentHashMap<String, OAuthClient>()
    private val auditLogsList = mutableListOf<ApiRequestAuditLog>()

    private val _apiKeysFlow = MutableStateFlow<List<ApiKey>>(emptyList())
    val apiKeysFlow: StateFlow<List<ApiKey>> = _apiKeysFlow.asStateFlow()

    private val _auditLogsFlow = MutableStateFlow<List<ApiRequestAuditLog>>(emptyList())
    val auditLogsFlow: StateFlow<List<ApiRequestAuditLog>> = _auditLogsFlow.asStateFlow()

    init {
        // Seed default API Key for demonstration
        val (defaultKey, _) = apiKeyService.generateApiKey(
            name = "Default Developer Key",
            environment = ApiEnvironment.LIVE,
            scopes = listOf("read:all", "write:all", "webhook:manage"),
            rateLimitTier = RateLimitTier.PRO
        )
        apiKeysMap[defaultKey.id] = defaultKey
        updateKeysFlow()
    }

    fun createApiKey(
        name: String,
        environment: ApiEnvironment = ApiEnvironment.LIVE,
        scopes: List<String> = listOf("read:all"),
        rateLimitTier: RateLimitTier = RateLimitTier.FREE,
        ipWhitelist: List<String> = emptyList(),
        expiresAt: Long = 0L
    ): Pair<ApiKey, String> {
        val (apiKey, rawSecret) = apiKeyService.generateApiKey(
            name = name,
            environment = environment,
            scopes = scopes,
            rateLimitTier = rateLimitTier,
            ipWhitelist = ipWhitelist,
            expiresAt = expiresAt
        )
        apiKeysMap[apiKey.id] = apiKey
        updateKeysFlow()
        return Pair(apiKey, rawSecret)
    }

    fun getAllApiKeys(): List<ApiKey> {
        return apiKeysMap.values.toList()
    }

    fun revokeApiKey(id: String): Boolean {
        val key = apiKeysMap[id] ?: return false
        apiKeysMap[id] = key.copy(status = ApiKeyStatus.REVOKED)
        updateKeysFlow()
        return true
    }

    fun findApiKeyByRawSecret(rawKey: String): ApiKey? {
        val hash = apiKeyService.hashKey(rawKey)
        return apiKeysMap.values.find { it.keyHash == hash }
    }

    fun registerOAuthClient(
        clientName: String,
        redirectUris: List<String>,
        allowedScopes: List<String>
    ): Pair<OAuthClient, String> {
        val (client, rawSecret) = oAuthService.registerClient(clientName, redirectUris, allowedScopes)
        oauthClientsMap[client.clientId] = client
        return Pair(client, rawSecret)
    }

    fun logApiRequest(log: ApiRequestAuditLog) {
        synchronized(auditLogsList) {
            auditLogsList.add(log)
            _auditLogsFlow.value = auditLogsList.takeLast(100)
        }
    }

    fun getAuditLogs(): List<ApiRequestAuditLog> {
        synchronized(auditLogsList) {
            return auditLogsList.toList()
        }
    }

    private fun updateKeysFlow() {
        _apiKeysFlow.value = apiKeysMap.values.toList()
    }
}
