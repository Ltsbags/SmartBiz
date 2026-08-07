package com.example.publicapi.mappers

import com.example.core.database.entity.CustomerEntity
import com.example.core.database.entity.InventoryItemEntity
import com.example.core.database.entity.InvoiceEntity
import com.example.core.database.entity.InvoiceItemEntity
import com.example.publicapi.dto.PublicCustomerDto
import com.example.publicapi.dto.PublicInventoryDto
import com.example.publicapi.dto.PublicInvoiceDto
import com.example.publicapi.dto.PublicInvoiceItemDto

object PublicApiMappers {

    fun toPublicInvoiceDto(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity> = emptyList()
    ): PublicInvoiceDto {
        return PublicInvoiceDto(
            id = invoice.id,
            invoiceNumber = invoice.invoiceNumber,
            customerName = invoice.customerName,
            customerPhone = invoice.customerPhone,
            subTotal = invoice.subtotal,
            taxTotal = invoice.taxAmount,
            discountTotal = invoice.discountAmount,
            grandTotal = invoice.totalAmount,
            paidAmount = invoice.paidAmount,
            balanceDue = invoice.totalAmount - invoice.paidAmount,
            paymentStatus = invoice.paymentStatus,
            createdDate = invoice.createdDate,
            items = items.map { toPublicInvoiceItemDto(it) }
        )
    }

    fun toPublicInvoiceItemDto(item: InvoiceItemEntity): PublicInvoiceItemDto {
        return PublicInvoiceItemDto(
            itemId = item.id,
            itemName = item.productName,
            quantity = item.quantity,
            unitPrice = item.sellingPrice,
            taxRatePercentage = item.gstPercentage,
            totalPrice = item.lineTotal
        )
    }

    fun toPublicCustomerDto(customer: CustomerEntity): PublicCustomerDto {
        return PublicCustomerDto(
            id = customer.id,
            customerCode = customer.customerCode,
            name = customer.name,
            company = customer.company,
            phone = maskPhoneIfNeeded(customer.phone),
            email = customer.email,
            city = customer.city,
            state = customer.state,
            customerType = customer.customerType,
            totalPurchases = customer.totalPurchases,
            outstandingBalance = customer.outstandingBalance,
            createdDate = customer.createdDate
        )
    }

    fun toPublicInventoryDto(item: InventoryItemEntity): PublicInventoryDto {
        return PublicInventoryDto(
            id = item.id,
            sku = item.sku,
            name = item.name,
            categoryName = item.category,
            sellingPrice = item.unitPrice,
            stockQuantity = item.stockQuantity.toDouble(),
            unit = item.unit,
            inStock = item.stockQuantity > 0,
            lowStockAlert = item.stockQuantity <= item.minStockThreshold
        )
    }

    private fun maskPhoneIfNeeded(phone: String): String {
        if (phone.length < 6) return phone
        val start = phone.take(3)
        val end = phone.takeLast(2)
        return "$start*****$end"
    }
}
