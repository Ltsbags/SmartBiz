package com.example.publicapi.dto

data class PublicApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: PublicApiError? = null,
    val meta: PublicApiMeta? = null
) {
    companion object {
        fun <T> success(data: T, meta: PublicApiMeta? = null): PublicApiResponse<T> {
            return PublicApiResponse(success = true, data = data, meta = meta)
        }

        fun <T> error(code: String, message: String, details: List<String> = emptyList()): PublicApiResponse<T> {
            return PublicApiResponse(
                success = false,
                error = PublicApiError(code = code, message = message, details = details)
            )
        }
    }
}

data class PublicApiError(
    val code: String,
    val message: String,
    val details: List<String> = emptyList()
)

data class PublicApiMeta(
    val page: Int? = null,
    val limit: Int? = null,
    val totalCount: Long? = null,
    val requestId: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis()
)

data class PublicInvoiceDto(
    val id: Long,
    val invoiceNumber: String,
    val customerName: String,
    val customerPhone: String,
    val subTotal: Double,
    val taxTotal: Double,
    val discountTotal: Double,
    val grandTotal: Double,
    val paidAmount: Double,
    val balanceDue: Double,
    val paymentStatus: String,
    val createdDate: Long,
    val items: List<PublicInvoiceItemDto> = emptyList()
)

data class PublicInvoiceItemDto(
    val itemId: Long,
    val itemName: String,
    val quantity: Double,
    val unitPrice: Double,
    val taxRatePercentage: Double,
    val totalPrice: Double
)

data class PublicCustomerDto(
    val id: Long,
    val customerCode: String,
    val name: String,
    val company: String,
    val phone: String,
    val email: String,
    val city: String,
    val state: String,
    val customerType: String,
    val totalPurchases: Double,
    val outstandingBalance: Double,
    val createdDate: Long
)

data class PublicInventoryDto(
    val id: Long,
    val sku: String,
    val name: String,
    val categoryName: String,
    val sellingPrice: Double,
    val stockQuantity: Double,
    val unit: String,
    val inStock: Boolean,
    val lowStockAlert: Boolean
)

data class PublicReportSummaryDto(
    val totalInvoices: Long,
    val totalSalesAmount: Double,
    val totalCustomers: Long,
    val lowStockCount: Long,
    val currency: String = "INR",
    val generatedAt: Long = System.currentTimeMillis()
)

data class PublicWebhookSubscriptionDto(
    val id: String,
    val targetUrl: String,
    val events: List<String>,
    val status: String,
    val secretPrefix: String,
    val createdAt: Long
)

data class CreateInvoiceApiRequest(
    val customerName: String,
    val customerPhone: String,
    val items: List<CreateInvoiceItemApiRequest>,
    val notes: String = ""
)

data class CreateInvoiceItemApiRequest(
    val itemName: String,
    val quantity: Double,
    val unitPrice: Double,
    val taxPercentage: Double = 0.0
)

data class CreateCustomerApiRequest(
    val name: String,
    val phone: String,
    val email: String = "",
    val company: String = "",
    val city: String = "",
    val state: String = ""
)

data class CreateWebhookApiRequest(
    val targetUrl: String,
    val events: List<String>
)
