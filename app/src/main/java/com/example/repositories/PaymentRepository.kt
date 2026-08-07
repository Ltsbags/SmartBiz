package com.example.repositories

import android.graphics.Bitmap
import com.example.core.database.DatabaseHelper
import com.example.core.database.entity.PaymentEntity
import com.example.core.database.entity.PaymentGatewayLogEntity
import com.example.core.database.entity.PaymentRequestEntity
import com.example.core.database.entity.RefundEntity
import com.example.services.payment.PaymentEngineService
import com.example.services.payment.PaymentLinkService.PaymentLinkService
import com.example.services.payment.PaymentReconciliationService.GatewayStatementItem
import com.example.services.payment.PaymentReconciliationService.PaymentReconciliationService
import com.example.services.payment.QrPaymentService.QrPaymentService
import com.example.services.payment.RefundService.RefundService
import com.example.services.payment.models.PaymentEngineRequest
import com.example.services.payment.models.PaymentEngineResult
import com.example.services.payment.models.PaymentGatewayConfig
import com.example.services.payment.models.ReconciliationReport
import com.example.services.payment.models.RefundEngineRequest
import com.example.services.payment.models.RefundEngineResult
import kotlinx.coroutines.flow.Flow

class PaymentRepository(
    private val dbHelper: DatabaseHelper
) {
    val paymentDao = dbHelper.paymentDao
    val paymentRequestDao = dbHelper.paymentRequestDao
    val paymentGatewayLogDao = dbHelper.paymentGatewayLogDao
    val refundDao = dbHelper.refundDao

    val paymentEngineService = PaymentEngineService(
        paymentDao = paymentDao,
        paymentRequestDao = paymentRequestDao,
        paymentGatewayLogDao = paymentGatewayLogDao,
        customerDao = dbHelper.customerDao,
        customerLedgerDao = dbHelper.customerLedgerDao,
        cashBookDao = dbHelper.cashBookDao,
        invoiceDao = dbHelper.invoiceDao
    )

    val paymentLinkService = PaymentLinkService(paymentRequestDao)
    val qrPaymentService = QrPaymentService()
    val refundService = RefundService(
        paymentDao = paymentDao,
        refundDao = refundDao,
        customerDao = dbHelper.customerDao,
        customerLedgerDao = dbHelper.customerLedgerDao,
        cashBookDao = dbHelper.cashBookDao,
        invoiceDao = dbHelper.invoiceDao
    )
    val reconciliationService = PaymentReconciliationService(paymentDao, paymentGatewayLogDao)

    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()
    val allPaymentRequests: Flow<List<PaymentRequestEntity>> = paymentRequestDao.getAllPaymentRequests()
    val allLogs: Flow<List<PaymentGatewayLogEntity>> = paymentGatewayLogDao.getAllLogs()
    val allRefunds: Flow<List<RefundEntity>> = refundDao.getAllRefunds()
    val totalCollectedAmount: Flow<Double?> = paymentDao.getTotalCollectedAmount()

    suspend fun processPayment(request: PaymentEngineRequest): PaymentEngineResult {
        return paymentEngineService.processPayment(request)
    }

    suspend fun generatePaymentLink(request: PaymentEngineRequest, expiryHours: Int = 72): PaymentRequestEntity {
        return paymentLinkService.generatePaymentLink(request, expiryHours)
    }

    suspend fun processRefund(request: RefundEngineRequest): RefundEngineResult {
        val payment = paymentDao.getPaymentById(request.paymentId)
            ?: return RefundEngineResult(isSuccess = false, errorMessage = "Payment ID ${request.paymentId} not found")

        val adapter = paymentEngineService.getAdapter(payment.gatewayProvider)
        return refundService.processRefund(request, adapter)
    }

    suspend fun reconcileGateway(provider: String, statements: List<GatewayStatementItem>): ReconciliationReport {
        return reconciliationService.reconcileGatewayTransactions(provider, statements)
    }

    suspend fun configureProvider(config: PaymentGatewayConfig) {
        paymentEngineService.configureProvider(config)
    }

    fun generateQrBitmap(payload: String): Bitmap {
        return qrPaymentService.generateQrBitmap(payload)
    }
}
