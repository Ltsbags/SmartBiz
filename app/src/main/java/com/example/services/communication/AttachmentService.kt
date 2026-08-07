package com.example.services.communication

import android.content.Context
import android.net.Uri
import java.io.File

enum class AttachmentType {
    PDF,
    IMAGE,
    CSV,
    EXCEL,
    OTHER
}

data class CommunicationAttachment(
    val fileName: String,
    val mimeType: String,
    val localUriPath: String,
    val type: AttachmentType,
    val sizeBytes: Long = 0L
)

class AttachmentService(
    private val context: Context
) {
    fun prepareInvoicePdfAttachment(invoiceNumber: String): CommunicationAttachment {
        val file = File(context.cacheDir, "Invoice_$invoiceNumber.pdf")
        if (!file.exists()) {
            file.writeText("%PDF-1.4 Mock Invoice PDF Payload for Invoice #$invoiceNumber")
        }
        return CommunicationAttachment(
            fileName = "Invoice_$invoiceNumber.pdf",
            mimeType = "application/pdf",
            localUriPath = file.absolutePath,
            type = AttachmentType.PDF,
            sizeBytes = file.length()
        )
    }

    fun preparePurchasePdfAttachment(poNumber: String): CommunicationAttachment {
        val file = File(context.cacheDir, "PurchaseOrder_$poNumber.pdf")
        if (!file.exists()) {
            file.writeText("%PDF-1.4 Mock Purchase Order PDF Payload for PO #$poNumber")
        }
        return CommunicationAttachment(
            fileName = "PurchaseOrder_$poNumber.pdf",
            mimeType = "application/pdf",
            localUriPath = file.absolutePath,
            type = AttachmentType.PDF,
            sizeBytes = file.length()
        )
    }

    fun prepareCustomerStatementAttachment(customerCode: String): CommunicationAttachment {
        val file = File(context.cacheDir, "Statement_$customerCode.pdf")
        if (!file.exists()) {
            file.writeText("%PDF-1.4 Mock Statement PDF Payload for Customer $customerCode")
        }
        return CommunicationAttachment(
            fileName = "Statement_$customerCode.pdf",
            mimeType = "application/pdf",
            localUriPath = file.absolutePath,
            type = AttachmentType.PDF,
            sizeBytes = file.length()
        )
    }

    fun prepareCatalogCsvAttachment(): CommunicationAttachment {
        val file = File(context.cacheDir, "ProductCatalog.csv")
        if (!file.exists()) {
            file.writeText("SKU,Name,Category,Price\nSKU-1001,Wireless Barcode Scanner,Hardware,149.99\nSKU-1002,Thermal Paper Roll,Supplies,29.50")
        }
        return CommunicationAttachment(
            fileName = "ProductCatalog.csv",
            mimeType = "text/csv",
            localUriPath = file.absolutePath,
            type = AttachmentType.CSV,
            sizeBytes = file.length()
        )
    }

    fun serializeAttachments(attachments: List<CommunicationAttachment>): String {
        return attachments.joinToString(";") { "${it.fileName}|${it.mimeType}|${it.localUriPath}|${it.type}" }
    }

    fun deserializeAttachments(serialized: String): List<CommunicationAttachment> {
        if (serialized.isBlank()) return emptyList()
        return serialized.split(";").mapNotNull { part ->
            val tokens = part.split("|")
            if (tokens.size >= 4) {
                CommunicationAttachment(
                    fileName = tokens[0],
                    mimeType = tokens[1],
                    localUriPath = tokens[2],
                    type = try { AttachmentType.valueOf(tokens[3]) } catch (_: Exception) { AttachmentType.OTHER }
                )
            } else null
        }
    }
}
