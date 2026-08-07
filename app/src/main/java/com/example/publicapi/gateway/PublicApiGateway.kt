package com.example.publicapi.gateway

import com.example.core.database.dao.CustomerDao
import com.example.core.database.dao.InventoryDao
import com.example.core.database.dao.InvoiceDao
import com.example.core.database.entity.CustomerEntity
import com.example.core.database.entity.InventoryItemEntity
import com.example.core.database.entity.InvoiceEntity
import com.example.core.database.entity.InvoiceItemEntity
import com.example.publicapi.auth.ApiKey
import com.example.publicapi.auth.KeyValidationResult
import com.example.publicapi.auth.OAuthToken
import com.example.publicapi.auth.TokenValidationResult
import com.example.publicapi.dto.CreateCustomerApiRequest
import com.example.publicapi.dto.CreateInvoiceApiRequest
import com.example.publicapi.dto.PublicApiMeta
import com.example.publicapi.dto.PublicApiResponse
import com.example.publicapi.dto.PublicCustomerDto
import com.example.publicapi.dto.PublicInventoryDto
import com.example.publicapi.dto.PublicInvoiceDto
import com.example.publicapi.dto.PublicReportSummaryDto
import com.example.publicapi.mappers.PublicApiMappers
import com.example.repositories.ApiRequestAuditLog
import com.example.repositories.PublicApiRepository
import kotlinx.coroutines.flow.firstOrNull

class PublicApiGateway(
    private val publicApiRepository: PublicApiRepository,
    private val invoiceDao: InvoiceDao,
    private val customerDao: CustomerDao,
    private val inventoryDao: InventoryDao
) {

    suspend fun getInvoices(
        apiKeyHeader: String? = null,
        bearerTokenHeader: String? = null,
        clientIp: String = "127.0.0.1",
        page: Int = 1,
        limit: Int = 20
    ): PublicApiResponse<List<PublicInvoiceDto>> {
        val startTime = System.currentTimeMillis()
        val authResult = authenticateAndCheckRateLimit(apiKeyHeader, bearerTokenHeader, clientIp, requiredScope = "read:invoices")
        
        if (!authResult.isAuthenticated) {
            logAudit("GET", "/v1/invoices", 401, startTime, clientIp, authResult.clientName)
            return PublicApiResponse.error("UNAUTHORIZED", authResult.errorMessage ?: "Authentication failed")
        }

        return try {
            val invoiceWithItemsList = invoiceDao.getAllInvoicesWithItems().firstOrNull() ?: emptyList()
            val dtos = invoiceWithItemsList.map { invoiceWithItems ->
                PublicApiMappers.toPublicInvoiceDto(invoiceWithItems.invoice, invoiceWithItems.items)
            }

            val paginated = dtos.drop((page - 1) * limit).take(limit)
            val meta = PublicApiMeta(page = page, limit = limit, totalCount = dtos.size.toLong())
            
            logAudit("GET", "/v1/invoices", 200, startTime, clientIp, authResult.clientName)
            PublicApiResponse.success(paginated, meta)
        } catch (e: Exception) {
            logAudit("GET", "/v1/invoices", 500, startTime, clientIp, authResult.clientName)
            PublicApiResponse.error("INTERNAL_ERROR", e.message ?: "Failed to fetch invoices")
        }
    }

    suspend fun createInvoice(
        request: CreateInvoiceApiRequest,
        apiKeyHeader: String? = null,
        bearerTokenHeader: String? = null,
        clientIp: String = "127.0.0.1"
    ): PublicApiResponse<PublicInvoiceDto> {
        val startTime = System.currentTimeMillis()
        val authResult = authenticateAndCheckRateLimit(apiKeyHeader, bearerTokenHeader, clientIp, requiredScope = "write:invoices")

        if (!authResult.isAuthenticated) {
            logAudit("POST", "/v1/invoices", 401, startTime, clientIp, authResult.clientName)
            return PublicApiResponse.error("UNAUTHORIZED", authResult.errorMessage ?: "Authentication failed")
        }

        if (request.customerName.isBlank() || request.items.isEmpty()) {
            logAudit("POST", "/v1/invoices", 400, startTime, clientIp, authResult.clientName)
            return PublicApiResponse.error("BAD_REQUEST", "Customer name and at least one item are required")
        }

        return try {
            val subTotal = request.items.sumOf { it.quantity * it.unitPrice }
            val taxTotal = request.items.sumOf { (it.quantity * it.unitPrice) * (it.taxPercentage / 100.0) }
            val grandTotal = subTotal + taxTotal
            val invoiceNum = "INV-PUB-${System.currentTimeMillis().toString().takeLast(6)}"

            val invoiceEntity = InvoiceEntity(
                invoiceNumber = invoiceNum,
                customerName = request.customerName,
                customerPhone = request.customerPhone,
                subtotal = subTotal,
                taxAmount = taxTotal,
                discountAmount = 0.0,
                totalAmount = grandTotal,
                paidAmount = 0.0,
                paymentStatus = "UNPAID",
                createdDate = System.currentTimeMillis()
            )

            val invoiceId = invoiceDao.insertInvoice(invoiceEntity)
            val itemEntities = request.items.map { itemReq ->
                val lineTotal = (itemReq.quantity * itemReq.unitPrice) * (1.0 + itemReq.taxPercentage / 100.0)
                InvoiceItemEntity(
                    invoiceId = invoiceId,
                    productName = itemReq.itemName,
                    quantity = itemReq.quantity,
                    sellingPrice = itemReq.unitPrice,
                    gstPercentage = itemReq.taxPercentage,
                    lineTotal = lineTotal
                )
            }
            invoiceDao.insertInvoiceItems(itemEntities)

            val createdInvoice = invoiceEntity.copy(id = invoiceId)
            val dto = PublicApiMappers.toPublicInvoiceDto(createdInvoice, itemEntities)

            // Trigger Webhooks
            publicApiRepository.webhookManager.dispatchEvent(
                eventType = "invoice.created",
                payloadJson = "{\"invoiceId\":$invoiceId,\"invoiceNumber\":\"$invoiceNum\",\"grandTotal\":$grandTotal}"
            )

            logAudit("POST", "/v1/invoices", 201, startTime, clientIp, authResult.clientName)
            PublicApiResponse.success(dto)
        } catch (e: Exception) {
            logAudit("POST", "/v1/invoices", 500, startTime, clientIp, authResult.clientName)
            PublicApiResponse.error("INTERNAL_ERROR", e.message ?: "Failed to create invoice")
        }
    }

    suspend fun getCustomers(
        apiKeyHeader: String? = null,
        bearerTokenHeader: String? = null,
        clientIp: String = "127.0.0.1"
    ): PublicApiResponse<List<PublicCustomerDto>> {
        val startTime = System.currentTimeMillis()
        val authResult = authenticateAndCheckRateLimit(apiKeyHeader, bearerTokenHeader, clientIp, requiredScope = "read:customers")

        if (!authResult.isAuthenticated) {
            logAudit("GET", "/v1/customers", 401, startTime, clientIp, authResult.clientName)
            return PublicApiResponse.error("UNAUTHORIZED", authResult.errorMessage ?: "Authentication failed")
        }

        return try {
            val customers = customerDao.getAllCustomers().firstOrNull() ?: emptyList()
            val dtos = customers.map { PublicApiMappers.toPublicCustomerDto(it) }
            logAudit("GET", "/v1/customers", 200, startTime, clientIp, authResult.clientName)
            PublicApiResponse.success(dtos)
        } catch (e: Exception) {
            logAudit("GET", "/v1/customers", 500, startTime, clientIp, authResult.clientName)
            PublicApiResponse.error("INTERNAL_ERROR", e.message ?: "Failed to fetch customers")
        }
    }

    suspend fun createCustomer(
        request: CreateCustomerApiRequest,
        apiKeyHeader: String? = null,
        bearerTokenHeader: String? = null,
        clientIp: String = "127.0.0.1"
    ): PublicApiResponse<PublicCustomerDto> {
        val startTime = System.currentTimeMillis()
        val authResult = authenticateAndCheckRateLimit(apiKeyHeader, bearerTokenHeader, clientIp, requiredScope = "write:customers")

        if (!authResult.isAuthenticated) {
            logAudit("POST", "/v1/customers", 401, startTime, clientIp, authResult.clientName)
            return PublicApiResponse.error("UNAUTHORIZED", authResult.errorMessage ?: "Authentication failed")
        }

        if (request.name.isBlank() || request.phone.isBlank()) {
            logAudit("POST", "/v1/customers", 400, startTime, clientIp, authResult.clientName)
            return PublicApiResponse.error("BAD_REQUEST", "Customer name and phone number are required")
        }

        return try {
            val custCode = "CUST-API-${System.currentTimeMillis().toString().takeLast(4)}"
            val entity = CustomerEntity(
                customerCode = custCode,
                name = request.name,
                company = request.company,
                phone = request.phone,
                email = request.email,
                city = request.city,
                state = request.state,
                customerType = "Retail",
                createdDate = System.currentTimeMillis()
            )

            val id = customerDao.insertCustomer(entity)
            val created = entity.copy(id = id)
            val dto = PublicApiMappers.toPublicCustomerDto(created)

            publicApiRepository.webhookManager.dispatchEvent(
                eventType = "customer.created",
                payloadJson = "{\"customerId\":$id,\"name\":\"${request.name}\"}"
            )

            logAudit("POST", "/v1/customers", 201, startTime, clientIp, authResult.clientName)
            PublicApiResponse.success(dto)
        } catch (e: Exception) {
            logAudit("POST", "/v1/customers", 500, startTime, clientIp, authResult.clientName)
            PublicApiResponse.error("INTERNAL_ERROR", e.message ?: "Failed to create customer")
        }
    }

    suspend fun getInventory(
        apiKeyHeader: String? = null,
        bearerTokenHeader: String? = null,
        clientIp: String = "127.0.0.1"
    ): PublicApiResponse<List<PublicInventoryDto>> {
        val startTime = System.currentTimeMillis()
        val authResult = authenticateAndCheckRateLimit(apiKeyHeader, bearerTokenHeader, clientIp, requiredScope = "read:inventory")

        if (!authResult.isAuthenticated) {
            logAudit("GET", "/v1/inventory", 401, startTime, clientIp, authResult.clientName)
            return PublicApiResponse.error("UNAUTHORIZED", authResult.errorMessage ?: "Authentication failed")
        }

        return try {
            val items = inventoryDao.getAllItems().firstOrNull() ?: emptyList()
            val dtos = items.map { PublicApiMappers.toPublicInventoryDto(it) }
            logAudit("GET", "/v1/inventory", 200, startTime, clientIp, authResult.clientName)
            PublicApiResponse.success(dtos)
        } catch (e: Exception) {
            logAudit("GET", "/v1/inventory", 500, startTime, clientIp, authResult.clientName)
            PublicApiResponse.error("INTERNAL_ERROR", e.message ?: "Failed to fetch inventory")
        }
    }

    suspend fun getAnalyticsSummary(
        apiKeyHeader: String? = null,
        bearerTokenHeader: String? = null,
        clientIp: String = "127.0.0.1"
    ): PublicApiResponse<PublicReportSummaryDto> {
        val startTime = System.currentTimeMillis()
        val authResult = authenticateAndCheckRateLimit(apiKeyHeader, bearerTokenHeader, clientIp, requiredScope = "read:reports")

        if (!authResult.isAuthenticated) {
            logAudit("GET", "/v1/analytics/summary", 401, startTime, clientIp, authResult.clientName)
            return PublicApiResponse.error("UNAUTHORIZED", authResult.errorMessage ?: "Authentication failed")
        }

        return try {
            val totalInvoices = invoiceDao.getInvoiceCount().firstOrNull()?.toLong() ?: 0L
            val totalRevenue = invoiceDao.getTotalPaidRevenue().firstOrNull() ?: 0.0
            val totalCustomers = customerDao.getCustomerCount().firstOrNull()?.toLong() ?: 0L
            val lowStockCount = inventoryDao.getLowStockCount().firstOrNull()?.toLong() ?: 0L

            val summary = PublicReportSummaryDto(
                totalInvoices = totalInvoices,
                totalSalesAmount = totalRevenue,
                totalCustomers = totalCustomers,
                lowStockCount = lowStockCount
            )

            logAudit("GET", "/v1/analytics/summary", 200, startTime, clientIp, authResult.clientName)
            PublicApiResponse.success(summary)
        } catch (e: Exception) {
            logAudit("GET", "/v1/analytics/summary", 500, startTime, clientIp, authResult.clientName)
            PublicApiResponse.error("INTERNAL_ERROR", e.message ?: "Failed to fetch analytics")
        }
    }

    private fun authenticateAndCheckRateLimit(
        apiKeyHeader: String?,
        bearerTokenHeader: String?,
        clientIp: String,
        requiredScope: String
    ): AuthCheckResult {
        var clientName = "Unknown Client"
        var tier = com.example.publicapi.auth.RateLimitTier.FREE

        if (!apiKeyHeader.isNullOrBlank()) {
            val key = publicApiRepository.findApiKeyByRawSecret(apiKeyHeader)
                ?: return AuthCheckResult(false, clientName, "Invalid API Key")

            val valResult = publicApiRepository.apiKeyService.validateKey(apiKeyHeader, key, clientIp, requiredScope)
            if (valResult is KeyValidationResult.Invalid) {
                return AuthCheckResult(false, key.name, valResult.reason)
            }
            clientName = key.name
            tier = key.rateLimitTier
        } else if (!bearerTokenHeader.isNullOrBlank() && bearerTokenHeader.startsWith("Bearer ")) {
            val tokenStr = bearerTokenHeader.removePrefix("Bearer ").trim()
            val valResult = publicApiRepository.oAuthService.validateAccessToken(tokenStr, requiredScope)
            if (valResult is TokenValidationResult.Invalid) {
                return AuthCheckResult(false, clientName, valResult.reason)
            }
            if (valResult is TokenValidationResult.Valid) {
                clientName = "OAuth Client ${valResult.token.clientId}"
            }
        } else {
            return AuthCheckResult(false, clientName, "Missing API Key or Authorization header")
        }

        // Check Rate Limiter
        val rateLimitResult = publicApiRepository.rateLimiter.checkRateLimit(clientName, tier)
        if (!rateLimitResult.isAllowed) {
            return AuthCheckResult(false, clientName, "Rate limit exceeded. Reset in ${rateLimitResult.resetSeconds}s")
        }

        return AuthCheckResult(true, clientName, null)
    }

    private fun logAudit(method: String, endpoint: String, status: Int, startTime: Long, clientIp: String, clientName: String) {
        val duration = System.currentTimeMillis() - startTime
        publicApiRepository.logApiRequest(
            ApiRequestAuditLog(
                clientName = clientName,
                endpoint = endpoint,
                httpMethod = method,
                statusCode = status,
                responseTimeMs = duration,
                clientIp = clientIp
            )
        )
    }

    private data class AuthCheckResult(
        val isAuthenticated: Boolean,
        val clientName: String,
        val errorMessage: String?
    )
}
