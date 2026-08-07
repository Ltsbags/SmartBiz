package com.example.features.publicapi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.publicapi.auth.ApiEnvironment
import com.example.publicapi.auth.ApiKey
import com.example.publicapi.auth.RateLimitTier
import com.example.publicapi.dto.CreateInvoiceApiRequest
import com.example.publicapi.dto.CreateInvoiceItemApiRequest
import com.example.publicapi.gateway.PublicApiGateway
import com.example.publicapi.sdk.PublicApiSdkSpec
import com.example.publicapi.webhooks.WebhookSubscription
import com.example.repositories.ApiRequestAuditLog
import com.example.repositories.PublicApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DeveloperPortalUiState(
    val apiKeys: List<ApiKey> = emptyList(),
    val newlyCreatedKeySecret: String? = null,
    val webhooks: List<WebhookSubscription> = emptyList(),
    val newlyCreatedWebhookSecret: String? = null,
    val auditLogs: List<ApiRequestAuditLog> = emptyList(),
    val openApiSpecJson: String = "",
    val sdkSnippetJs: String = "",
    val sdkSnippetFlutter: String = "",
    val sdkSnippetKotlin: String = "",
    val sandboxOutputJson: String = "",
    val isLoading: Boolean = false,
    val selectedTab: Int = 0 // 0 = API Keys, 1 = Webhooks, 2 = API Sandbox, 3 = Documentation & SDKs, 4 = Audit Logs
)

class DeveloperPortalViewModel(
    val publicApiRepository: PublicApiRepository,
    val publicApiGateway: PublicApiGateway
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeveloperPortalUiState())
    val uiState: StateFlow<DeveloperPortalUiState> = _uiState.asStateFlow()

    init {
        loadData()
        generateDocsAndSnippets("sb_live_sample_key_12345")
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    fun loadData() {
        viewModelScope.launch {
            val keys = publicApiRepository.getAllApiKeys()
            val webhooks = publicApiRepository.webhookManager.getAllSubscriptions()
            val logs = publicApiRepository.getAuditLogs()

            _uiState.value = _uiState.value.copy(
                apiKeys = keys,
                webhooks = webhooks,
                auditLogs = logs
            )
        }
    }

    fun createApiKey(name: String, tier: RateLimitTier, scopes: List<String>) {
        viewModelScope.launch {
            val (apiKey, rawSecret) = publicApiRepository.createApiKey(
                name = name,
                environment = ApiEnvironment.LIVE,
                scopes = scopes,
                rateLimitTier = tier
            )
            _uiState.value = _uiState.value.copy(
                newlyCreatedKeySecret = rawSecret,
                apiKeys = publicApiRepository.getAllApiKeys()
            )
            generateDocsAndSnippets(rawSecret)
        }
    }

    fun revokeApiKey(id: String) {
        viewModelScope.launch {
            publicApiRepository.revokeApiKey(id)
            _uiState.value = _uiState.value.copy(
                apiKeys = publicApiRepository.getAllApiKeys()
            )
        }
    }

    fun registerWebhook(targetUrl: String, events: List<String>) {
        viewModelScope.launch {
            val (sub, secret) = publicApiRepository.webhookManager.registerSubscription(targetUrl, events)
            _uiState.value = _uiState.value.copy(
                newlyCreatedWebhookSecret = secret,
                webhooks = publicApiRepository.webhookManager.getAllSubscriptions()
            )
        }
    }

    fun runSandboxQuery(endpoint: String, rawApiKey: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val output = when (endpoint) {
                "GET /v1/invoices" -> {
                    val res = publicApiGateway.getInvoices(apiKeyHeader = rawApiKey)
                    formatJsonResult(res)
                }
                "GET /v1/customers" -> {
                    val res = publicApiGateway.getCustomers(apiKeyHeader = rawApiKey)
                    formatJsonResult(res)
                }
                "GET /v1/inventory" -> {
                    val res = publicApiGateway.getInventory(apiKeyHeader = rawApiKey)
                    formatJsonResult(res)
                }
                "GET /v1/analytics/summary" -> {
                    val res = publicApiGateway.getAnalyticsSummary(apiKeyHeader = rawApiKey)
                    formatJsonResult(res)
                }
                "POST /v1/invoices" -> {
                    val req = CreateInvoiceApiRequest(
                        customerName = "Sandbox Client",
                        customerPhone = "+91 99999 88888",
                        items = listOf(CreateInvoiceItemApiRequest("API Test Item", 1.0, 1500.0, 18.0))
                    )
                    val res = publicApiGateway.createInvoice(req, apiKeyHeader = rawApiKey)
                    formatJsonResult(res)
                }
                else -> "Unknown Endpoint"
            }

            _uiState.value = _uiState.value.copy(
                sandboxOutputJson = output,
                auditLogs = publicApiRepository.getAuditLogs(),
                isLoading = false
            )
        }
    }

    fun dismissSecretDialog() {
        _uiState.value = _uiState.value.copy(
            newlyCreatedKeySecret = null,
            newlyCreatedWebhookSecret = null
        )
    }

    private fun generateDocsAndSnippets(apiKey: String) {
        _uiState.value = _uiState.value.copy(
            openApiSpecJson = PublicApiSdkSpec.generateOpenApiSpecJson(),
            sdkSnippetJs = PublicApiSdkSpec.generateNodeJsSnippet(apiKey),
            sdkSnippetFlutter = PublicApiSdkSpec.generateFlutterSnippet(apiKey),
            sdkSnippetKotlin = PublicApiSdkSpec.generateKotlinSnippet(apiKey)
        )
    }

    private fun <T> formatJsonResult(response: com.example.publicapi.dto.PublicApiResponse<T>): String {
        return if (response.success) {
            "{\n  \"success\": true,\n  \"data\": ${response.data},\n  \"meta\": ${response.meta}\n}"
        } else {
            "{\n  \"success\": false,\n  \"error\": ${response.error}\n}"
        }
    }

    class Factory(
        private val publicApiRepository: PublicApiRepository,
        private val publicApiGateway: PublicApiGateway
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeveloperPortalViewModel(publicApiRepository, publicApiGateway) as T
        }
    }
}
