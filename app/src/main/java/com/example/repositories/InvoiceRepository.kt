package com.example.repositories

import com.example.core.database.dao.CustomerDao
import com.example.core.database.dao.CustomerLedgerDao
import com.example.core.database.dao.InventoryDao
import com.example.core.database.dao.InvoiceDao
import com.example.core.database.entity.CustomerLedgerEntity
import com.example.core.database.entity.InvoiceEntity
import com.example.core.database.entity.InvoiceItemEntity
import com.example.core.database.entity.InvoiceWithItems
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val inventoryDao: InventoryDao,
    private val customerDao: CustomerDao,
    private val customerLedgerDao: CustomerLedgerDao
) {
    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()
    val allInvoicesWithItems: Flow<List<InvoiceWithItems>> = invoiceDao.getAllInvoicesWithItems()
    val totalPaidRevenue: Flow<Double?> = invoiceDao.getTotalPaidRevenue()
    val totalPendingAmount: Flow<Double?> = invoiceDao.getTotalPendingAmount()
    val invoiceCount: Flow<Int> = invoiceDao.getInvoiceCount()

    suspend fun getInvoiceById(id: Long): InvoiceEntity? = invoiceDao.getInvoiceById(id)

    suspend fun getInvoiceWithItemsById(id: Long): InvoiceWithItems? =
        invoiceDao.getInvoiceWithItemsById(id)

    fun getInvoiceWithItemsFlow(id: Long): Flow<InvoiceWithItems?> =
        invoiceDao.getInvoiceWithItemsFlow(id)

    suspend fun generateNextInvoiceNumber(): String {
        val maxId = invoiceDao.getMaxInvoiceId() ?: 0
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        return "INV-$year-${String.format(Locale.US, "%03d", maxId + 1)}"
    }

    suspend fun saveInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): Long {
        val isNew = invoice.id == 0L
        val savedInvoiceId = if (isNew) {
            invoiceDao.insertInvoice(invoice)
        } else {
            invoiceDao.updateInvoice(invoice)
            invoiceDao.deleteInvoiceItemsByInvoiceId(invoice.id)
            invoice.id
        }

        val itemsToInsert = items.map { item ->
            item.copy(invoiceId = savedInvoiceId)
        }
        invoiceDao.insertInvoiceItems(itemsToInsert)

        // If invoice is completed upon saving, apply inventory and customer updates
        if (invoice.status == "COMPLETED") {
            processCompletedInvoice(invoice.copy(id = savedInvoiceId), itemsToInsert)
        }

        return savedInvoiceId
    }

    private suspend fun processCompletedInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ) {
        // 1. Reduce inventory stock for products
        for (item in items) {
            if (item.productId > 0) {
                val product = inventoryDao.getItemById(item.productId)
                if (product != null) {
                    val newStock = (product.stockQuantity - item.quantity.toInt()).coerceAtLeast(0)
                    val updatedProduct = product.copy(
                        stockQuantity = newStock,
                        updatedDate = System.currentTimeMillis()
                    )
                    inventoryDao.updateItem(updatedProduct)
                }
            }
        }

        // 2. Update Customer outstanding balance & record in ledger
        if (invoice.customerId > 0) {
            val customer = customerDao.getCustomerById(invoice.customerId)
            if (customer != null) {
                val newOutstanding = customer.outstandingBalance + invoice.balanceAmount
                val newPurchases = customer.totalPurchases + invoice.totalAmount
                val updatedCustomer = customer.copy(
                    outstandingBalance = newOutstanding,
                    totalPurchases = newPurchases
                )
                customerDao.updateCustomer(updatedCustomer)

                customerLedgerDao.insertLedger(
                    CustomerLedgerEntity(
                        customerId = customer.id,
                        transactionType = "INVOICE",
                        referenceNumber = invoice.invoiceNumber,
                        amount = invoice.totalAmount,
                        balanceAfter = newOutstanding,
                        description = "Sales Invoice #${invoice.invoiceNumber}",
                        transactionDate = invoice.date
                    )
                )
            }
        }
    }

    suspend fun completeInvoice(invoiceId: Long) {
        val invoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId) ?: return
        val invoice = invoiceWithItems.invoice
        if (invoice.status == "COMPLETED") return

        val updatedInvoice = invoice.copy(
            status = "COMPLETED",
            updatedDate = System.currentTimeMillis()
        )
        invoiceDao.updateInvoice(updatedInvoice)
        processCompletedInvoice(updatedInvoice, invoiceWithItems.items)
    }

    suspend fun cancelInvoice(invoiceId: Long) {
        val invoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId) ?: return
        val invoice = invoiceWithItems.invoice
        if (invoice.status == "CANCELLED") return

        // If it was completed, reverse inventory and customer balance
        if (invoice.status == "COMPLETED") {
            // Restore inventory stock
            for (item in invoiceWithItems.items) {
                if (item.productId > 0) {
                    val product = inventoryDao.getItemById(item.productId)
                    if (product != null) {
                        val restoredStock = product.stockQuantity + item.quantity.toInt()
                        inventoryDao.updateItem(
                            product.copy(
                                stockQuantity = restoredStock,
                                updatedDate = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }

            // Restore Customer balance
            if (invoice.customerId > 0) {
                val customer = customerDao.getCustomerById(invoice.customerId)
                if (customer != null) {
                    val newOutstanding = (customer.outstandingBalance - invoice.balanceAmount).coerceAtLeast(0.0)
                    val newPurchases = (customer.totalPurchases - invoice.totalAmount).coerceAtLeast(0.0)
                    customerDao.updateCustomer(
                        customer.copy(
                            outstandingBalance = newOutstanding,
                            totalPurchases = newPurchases
                        )
                    )

                    customerLedgerDao.insertLedger(
                        CustomerLedgerEntity(
                            customerId = customer.id,
                            transactionType = "INVOICE_CANCELLED",
                            referenceNumber = invoice.invoiceNumber,
                            amount = -invoice.totalAmount,
                            balanceAfter = newOutstanding,
                            description = "Cancelled Invoice #${invoice.invoiceNumber}",
                            transactionDate = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        val updatedInvoice = invoice.copy(
            status = "CANCELLED",
            updatedDate = System.currentTimeMillis()
        )
        invoiceDao.updateInvoice(updatedInvoice)
    }

    suspend fun duplicateInvoice(invoiceId: Long): Long? {
        val original = invoiceDao.getInvoiceWithItemsById(invoiceId) ?: return null
        val nextNumber = generateNextInvoiceNumber()

        val newInvoice = original.invoice.copy(
            id = 0,
            invoiceNumber = nextNumber,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + 15 * 86400000L,
            status = "DRAFT",
            paymentStatus = "UNPAID",
            paidAmount = 0.0,
            balanceAmount = original.invoice.totalAmount,
            createdDate = System.currentTimeMillis(),
            updatedDate = System.currentTimeMillis()
        )

        val newItems = original.items.map { item ->
            item.copy(id = 0, invoiceId = 0)
        }

        return saveInvoice(newInvoice, newItems)
    }

    suspend fun deleteInvoice(invoiceId: Long) {
        val invoice = invoiceDao.getInvoiceById(invoiceId) ?: return
        invoiceDao.deleteInvoice(invoice)
    }
}
