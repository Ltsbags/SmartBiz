package com.example.features.payment

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.PaymentEntity
import com.example.core.database.entity.PaymentGatewayLogEntity
import com.example.core.database.entity.PaymentRequestEntity
import com.example.core.database.entity.RefundEntity
import com.example.repositories.PaymentRepository
import com.example.services.payment.PaymentReconciliationService.GatewayStatementItem
import com.example.services.payment.models.PaymentEngineRequest
import com.example.services.payment.models.PaymentGatewayConfig
import com.example.services.payment.models.ReconciliationReport
import com.example.services.payment.models.RefundEngineRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentEngineViewModel(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    val payments: StateFlow<List<PaymentEntity>> = paymentRepository.allPayments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val paymentRequests: StateFlow<List<PaymentRequestEntity>> = paymentRepository.allPaymentRequests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val logs: StateFlow<List<PaymentGatewayLogEntity>> = paymentRepository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val refunds: StateFlow<List<RefundEntity>> = paymentRepository.allRefunds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalCollected: StateFlow<Double> = paymentRepository.totalCollectedAmount
        .map { it ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    private val _reconciliationReport = MutableStateFlow<ReconciliationReport?>(null)
    val reconciliationReport: StateFlow<ReconciliationReport?> = _reconciliationReport.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun processPayment(request: PaymentEngineRequest, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = paymentRepository.processPayment(request)
            _isProcessing.value = false
            if (result.isSuccess) {
                _actionMessage.value = "Payment ${result.paymentNumber} processed successfully via ${request.paymentMethod}"
                onComplete?.invoke(true)
            } else {
                _actionMessage.value = "Payment failed: ${result.errorMessage}"
                onComplete?.invoke(false)
            }
        }
    }

    fun generatePaymentLink(request: PaymentEngineRequest, expiryHours: Int = 72, onComplete: ((PaymentRequestEntity) -> Unit)? = null) {
        viewModelScope.launch {
            _isProcessing.value = true
            val linkReq = paymentRepository.generatePaymentLink(request, expiryHours)
            _isProcessing.value = false
            _actionMessage.value = "Payment Link ${linkReq.requestNumber} generated!"
            onComplete?.invoke(linkReq)
        }
    }

    fun processRefund(paymentId: Long, amount: Double, reason: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isProcessing.value = true
            val req = RefundEngineRequest(paymentId = paymentId, amount = amount, reason = reason)
            val res = paymentRepository.processRefund(req)
            _isProcessing.value = false
            if (res.isSuccess) {
                _actionMessage.value = "Refund ${res.refundNumber} issued successfully!"
                onComplete?.invoke(true)
            } else {
                _actionMessage.value = "Refund failed: ${res.errorMessage}"
                onComplete?.invoke(false)
            }
        }
    }

    fun reconcileGateway(provider: String, mockStatements: List<GatewayStatementItem>? = null) {
        viewModelScope.launch {
            _isProcessing.value = true
            val currentPayments = payments.value.filter { it.gatewayProvider.equals(provider, ignoreCase = true) }
            val statementsToUse = mockStatements ?: currentPayments.map {
                GatewayStatementItem(
                    transactionId = it.gatewayTransactionId.ifBlank { it.transactionRef },
                    amount = it.amount,
                    status = it.status,
                    timestamp = it.timestamp
                )
            }
            val report = paymentRepository.reconcileGateway(provider, statementsToUse)
            _reconciliationReport.value = report
            _isProcessing.value = false
            _actionMessage.value = "Reconciliation complete for $provider. Matched: ${report.matchedCount}, Discrepancies: ${report.unmatchedCount}"
        }
    }

    fun configureProvider(config: PaymentGatewayConfig) {
        viewModelScope.launch {
            paymentRepository.configureProvider(config)
            _actionMessage.value = "Provider ${config.provider} configuration saved."
        }
    }

    fun generateQrBitmap(payload: String): Bitmap {
        return paymentRepository.generateQrBitmap(payload)
    }

    fun clearMessage() {
        _actionMessage.value = null
    }
}
