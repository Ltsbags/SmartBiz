package com.example.services.payment

import com.example.core.database.dao.CashBookDao
import com.example.core.database.dao.CustomerDao
import com.example.core.database.dao.CustomerLedgerDao
import com.example.core.database.dao.InvoiceDao
import com.example.core.database.dao.PaymentDao
import com.example.core.database.dao.PaymentGatewayLogDao
import com.example.core.database.dao.PaymentRequestDao
import com.example.core.database.entity.CashBookEntryEntity
import com.example.core.database.entity.CustomerLedgerEntity
import com.example.core.database.entity.PaymentEntity
import com.example.core.database.entity.PaymentGatewayLogEntity
import com.example.services.payment.adapters.OfflineCashAdapter
import com.example.services.payment.adapters.PaymentGatewayAdapter
import com.example.services.payment.adapters.RazorpayAdapter
import com.example.services.payment.adapters.StripeAdapter
import com.example.services.payment.adapters.UpiAdapter
import com.example.services.payment.models.PaymentEngineRequest
import com.example.services.payment.models.PaymentEngineResult
import com.example.services.payment.models.PaymentGatewayConfig
import java.util.concurrent.ConcurrentHashMap

class PaymentEngineService(
    private val paymentDao: PaymentDao,
    private val paymentRequestDao: PaymentRequestDao,
    private val paymentGatewayLogDao: PaymentGatewayLogDao,
    private val customerDao: CustomerDao,
    private val customerLedgerDao: CustomerLedgerDao,
    private val cashBookDao: CashBookDao,
    private val invoiceDao: InvoiceDao
) {
    private val adapters = ConcurrentHashMap<String, PaymentGatewayAdapter>()
    private var activeConfigs = ConcurrentHashMap<String, PaymentGatewayConfig>()

    init {
        // Register default adapters
        registerAdapter(UpiAdapter())
        registerAdapter(RazorpayAdapter())
        registerAdapter(StripeAdapter())
        registerAdapter(OfflineCashAdapter())
    }

    fun registerAdapter(adapter: PaymentGatewayAdapter) {
        adapters[adapter.providerName.uppercase()] = adapter
    }

    suspend fun configureProvider(config: PaymentGatewayConfig) {
        val providerKey = config.provider.uppercase()
        activeConfigs[providerKey] = config
        adapters[providerKey]?.initialize(config)
    }

    fun getAdapter(provider: String): PaymentGatewayAdapter {
        val key = provider.uppercase()
        return adapters[key] ?: adapters["UPI"] ?: OfflineCashAdapter()
    }

    /**
     * Primary Payment Processing Entry Point
     * Routes request to designated Gateway Adapter, logs transaction, and executes automatic accounting updates.
     */
    suspend fun processPayment(request: PaymentEngineRequest): PaymentEngineResult {
        val providerKey = when (request.paymentMethod.uppercase()) {
            "RAZORPAY" -> "RAZORPAY"
            "STRIPE" -> "STRIPE"
            "CASH" -> "OFFLINE_CASH"
            "BANK_TRANSFER" -> "OFFLINE_CASH"
            else -> "UPI"
        }

        val adapter = getAdapter(providerKey)
        val now = System.currentTimeMillis()

        // 1. Log Request
        paymentGatewayLogDao.insertLog(
            PaymentGatewayLogEntity(
                provider = providerKey,
                eventType = "REQUEST",
                requestPayload = "Amount: ${request.amount}, Inv: ${request.invoiceNumber}, Cust: ${request.customerName}",
                statusCode = 200,
                timestamp = now
            )
        )

        // 2. Execute Gateway / Provider processing
        val adapterResult = try {
            adapter.processPayment(request, "")
        } catch (e: Exception) {
            paymentGatewayLogDao.insertLog(
                PaymentGatewayLogEntity(
                    provider = providerKey,
                    eventType = "ERROR",
                    errorMessage = e.message ?: "Gateway Exception",
                    statusCode = 500,
                    timestamp = now
                )
            )
            return PaymentEngineResult(
                isSuccess = false,
                errorMessage = "Payment failed: ${e.message}"
            )
        }

        if (!adapterResult.isSuccess) {
            paymentGatewayLogDao.insertLog(
                PaymentGatewayLogEntity(
                    provider = providerKey,
                    eventType = "RESPONSE",
                    responsePayload = adapterResult.gatewayResponse,
                    statusCode = 400,
                    errorMessage = adapterResult.errorMessage,
                    timestamp = now
                )
            )
            return adapterResult
        }

        // 3. Save Local Payment Entity
        val paymentNumber = adapterResult.paymentNumber.ifBlank { "PAY-" + now.toString().takeLast(8) }
        val paymentEntity = PaymentEntity(
            paymentNumber = paymentNumber,
            invoiceId = request.invoiceId,
            invoiceNumber = request.invoiceNumber,
            customerId = request.customerId,
            customerName = request.customerName,
            amount = request.amount,
            paymentMethod = request.paymentMethod,
            transactionRef = adapterResult.transactionId,
            status = "SUCCESS",
            gatewayProvider = providerKey,
            gatewayTransactionId = adapterResult.transactionId,
            currency = request.currency,
            notes = request.notes,
            timestamp = now,
            updatedAt = now,
            isOfflineProcessed = request.isOfflineProcessed || providerKey == "OFFLINE_CASH"
        )

        val insertedId = paymentDao.insertPayment(paymentEntity)

        // 4. Log Success Response
        paymentGatewayLogDao.insertLog(
            PaymentGatewayLogEntity(
                paymentId = insertedId,
                provider = providerKey,
                eventType = "RESPONSE",
                responsePayload = adapterResult.gatewayResponse,
                statusCode = 200,
                timestamp = now
            )
        )

        // 5. AUTOMATIC ACCOUNTING INTEGRATION
        executeAccountingUpdates(paymentEntity.copy(id = insertedId))

        return adapterResult.copy(
            paymentId = insertedId,
            paymentNumber = paymentNumber,
            isSuccess = true
        )
    }

    /**
     * Executes automatic updates to Customer Outstanding, Customer Ledger, Cash Book, and Invoice status.
     */
    private suspend fun executeAccountingUpdates(payment: PaymentEntity) {
        val now = System.currentTimeMillis()

        // A. Update Customer Outstanding Balance & Customer Ledger
        payment.customerId?.let { custId ->
            val customer = customerDao.getCustomerById(custId)
            customer?.let { c ->
                val newOutstanding = (c.outstandingBalance - payment.amount).coerceAtLeast(0.0)
                customerDao.updateCustomer(c.copy(outstandingBalance = newOutstanding, updatedDate = now))

                customerLedgerDao.insertLedger(
                    CustomerLedgerEntity(
                        customerId = custId,
                        transactionType = "PAYMENT",
                        referenceNumber = payment.paymentNumber,
                        amount = -payment.amount, // negative for credit/reduction of debt
                        balanceAfter = newOutstanding,
                        description = "Payment received via ${payment.paymentMethod} (Ref: ${payment.transactionRef})",
                        transactionDate = now
                    )
                )
            }
        }

        // B. Update Cash Book (Inflow)
        cashBookDao.insertEntry(
            CashBookEntryEntity(
                entryDate = now,
                entryType = "CASH_IN",
                sourceType = "SALES_INVOICE",
                referenceId = payment.id,
                referenceNumber = payment.paymentNumber,
                entityName = payment.customerName.ifBlank { "Customer" },
                description = "Payment received for Invoice ${payment.invoiceNumber.ifBlank { "Direct" }}",
                amount = payment.amount,
                paymentMode = payment.paymentMethod,
                balanceAfter = 0.0,
                createdDate = now
            )
        )

        // C. Update Invoice Payment Status
        payment.invoiceId?.let { invId ->
            val invoice = invoiceDao.getInvoiceById(invId)
            invoice?.let { inv ->
                val newPaidAmount = inv.paidAmount + payment.amount
                val newBalanceAmount = (inv.totalAmount - newPaidAmount).coerceAtLeast(0.0)
                val newPaymentStatus = when {
                    newBalanceAmount <= 0.0 -> "PAID"
                    newPaidAmount > 0.0 -> "PARTIAL"
                    else -> "UNPAID"
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
    }
}
