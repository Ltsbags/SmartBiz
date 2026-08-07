package com.example.services.payment.RefundService

import com.example.core.database.dao.CashBookDao
import com.example.core.database.dao.CustomerDao
import com.example.core.database.dao.CustomerLedgerDao
import com.example.core.database.dao.InvoiceDao
import com.example.core.database.dao.PaymentDao
import com.example.core.database.dao.RefundDao
import com.example.core.database.entity.CashBookEntryEntity
import com.example.core.database.entity.CustomerLedgerEntity
import com.example.core.database.entity.RefundEntity
import com.example.services.payment.adapters.PaymentGatewayAdapter
import com.example.services.payment.models.RefundEngineRequest
import com.example.services.payment.models.RefundEngineResult

class RefundService(
    private val paymentDao: PaymentDao,
    private val refundDao: RefundDao,
    private val customerDao: CustomerDao,
    private val customerLedgerDao: CustomerLedgerDao,
    private val cashBookDao: CashBookDao,
    private val invoiceDao: InvoiceDao
) {
    suspend fun processRefund(
        request: RefundEngineRequest,
        adapter: PaymentGatewayAdapter
    ): RefundEngineResult {
        val payment = paymentDao.getPaymentById(request.paymentId)
            ?: return RefundEngineResult(isSuccess = false, errorMessage = "Payment ID ${request.paymentId} not found")

        if (payment.status == "REFUNDED") {
            return RefundEngineResult(isSuccess = false, errorMessage = "Payment already fully refunded")
        }

        if (request.amount <= 0 || request.amount > payment.amount) {
            return RefundEngineResult(isSuccess = false, errorMessage = "Invalid refund amount ${request.amount}")
        }

        val adapterResult = adapter.processRefund(request, payment)
        if (!adapterResult.isSuccess) {
            return adapterResult
        }

        val refundNo = adapterResult.refundNumber.ifBlank { "RFD-" + System.currentTimeMillis().toString().takeLast(8) }
        val now = System.currentTimeMillis()

        val refundEntity = RefundEntity(
            refundNumber = refundNo,
            paymentId = payment.id,
            paymentNumber = payment.paymentNumber,
            invoiceId = payment.invoiceId,
            customerId = payment.customerId,
            customerName = payment.customerName,
            amount = request.amount,
            reason = request.reason,
            status = "COMPLETED",
            gatewayRefundId = adapterResult.gatewayRefundId,
            createdAt = now,
            completedAt = now,
            updatedAt = now
        )

        val refundId = refundDao.insertRefund(refundEntity)

        // Update Payment Status
        val isFullRefund = request.amount >= payment.amount
        val updatedPaymentStatus = if (isFullRefund) "REFUNDED" else "PARTIALLY_REFUNDED"
        paymentDao.updatePayment(payment.copy(status = updatedPaymentStatus, updatedAt = now))

        // Accounting Reversal Integration:
        // 1. Customer Outstanding Balance (customer balance increases because payment was refunded)
        payment.customerId?.let { custId ->
            val customer = customerDao.getCustomerById(custId)
            customer?.let { c ->
                val newOutstanding = c.outstandingBalance + request.amount
                customerDao.updateCustomer(c.copy(outstandingBalance = newOutstanding, updatedDate = now))

                val newLedgerBalance = newOutstanding
                customerLedgerDao.insertLedger(
                    CustomerLedgerEntity(
                        customerId = custId,
                        transactionType = "REFUND",
                        referenceNumber = refundNo,
                        amount = request.amount, // positive for debit/restored balance
                        balanceAfter = newLedgerBalance,
                        description = "Refund for Payment ${payment.paymentNumber} - ${request.reason}",
                        transactionDate = now
                    )
                )
            }
        }

        // 2. Cash Book Outflow
        cashBookDao.insertEntry(
            CashBookEntryEntity(
                entryDate = now,
                entryType = "CASH_OUT",
                sourceType = "REFUND",
                referenceId = refundId,
                referenceNumber = refundNo,
                entityName = payment.customerName.ifBlank { "Customer" },
                description = "Refund issued for Payment ${payment.paymentNumber}",
                amount = request.amount,
                paymentMode = payment.paymentMethod,
                balanceAfter = 0.0, // calculate if needed
                createdDate = now
            )
        )

        // 3. Invoice Balance Update
        payment.invoiceId?.let { invId ->
            val invoice = invoiceDao.getInvoiceById(invId)
            invoice?.let { inv ->
                val newPaidAmount = (inv.paidAmount - request.amount).coerceAtLeast(0.0)
                val newBalanceAmount = inv.totalAmount - newPaidAmount
                val newPaymentStatus = when {
                    newPaidAmount <= 0.0 -> "UNPAID"
                    newPaidAmount < inv.totalAmount -> "PARTIAL"
                    else -> "PAID"
                }
                invoiceDao.updateInvoice(
                    inv.copy(
                        paidAmount = newPaidAmount,
                        balanceAmount = newBalanceAmount,
                        paymentStatus = newPaymentStatus,
                        updatedDate = now
                    )
                )
            }
        }

        return adapterResult.copy(refundId = refundId, isSuccess = true)
    }
}
