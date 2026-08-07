package com.example.services.payment.PaymentReconciliationService

import com.example.core.database.dao.PaymentDao
import com.example.core.database.dao.PaymentGatewayLogDao
import com.example.core.database.entity.PaymentEntity
import com.example.core.database.entity.PaymentGatewayLogEntity
import com.example.services.payment.models.DiscrepancyType
import com.example.services.payment.models.ReconciliationDiscrepancy
import com.example.services.payment.models.ReconciliationReport
import kotlinx.coroutines.flow.first

class PaymentReconciliationService(
    private val paymentDao: PaymentDao,
    private val paymentGatewayLogDao: PaymentGatewayLogDao
) {
    suspend fun reconcileGatewayTransactions(
        provider: String,
        gatewayStatements: List<GatewayStatementItem>
    ): ReconciliationReport {
        val localPayments = paymentDao.getAllPayments().first().filter { 
            it.gatewayProvider.equals(provider, ignoreCase = true) || it.paymentMethod.equals(provider, ignoreCase = true)
        }

        val discrepancies = mutableListOf<ReconciliationDiscrepancy>()
        var matchedCount = 0
        var totalGatewayAmount = 0.0
        var totalLocalAmount = localPayments.sumOf { it.amount }

        val localMapByTxn = localPayments.associateBy { it.gatewayTransactionId.ifBlank { it.transactionRef } }

        for (item in gatewayStatements) {
            totalGatewayAmount += item.amount
            val localMatch = localMapByTxn[item.transactionId]

            if (localMatch == null) {
                discrepancies.add(
                    ReconciliationDiscrepancy(
                        localPaymentId = null,
                        localPaymentNumber = "NONE",
                        gatewayTxnId = item.transactionId,
                        localAmount = 0.0,
                        gatewayAmount = item.amount,
                        discrepancyType = DiscrepancyType.UNMATCHED_GATEWAY,
                        description = "Gateway transaction ${item.transactionId} has no local payment record."
                    )
                )
            } else {
                if (Math.abs(localMatch.amount - item.amount) > 0.01) {
                    discrepancies.add(
                        ReconciliationDiscrepancy(
                            localPaymentId = localMatch.id,
                            localPaymentNumber = localMatch.paymentNumber,
                            gatewayTxnId = item.transactionId,
                            localAmount = localMatch.amount,
                            gatewayAmount = item.amount,
                            discrepancyType = DiscrepancyType.AMOUNT_MISMATCH,
                            description = "Amount discrepancy: Local=${localMatch.amount}, Gateway=${item.amount}"
                        )
                    )
                } else if (!localMatch.status.equals(item.status, ignoreCase = true)) {
                    discrepancies.add(
                        ReconciliationDiscrepancy(
                            localPaymentId = localMatch.id,
                            localPaymentNumber = localMatch.paymentNumber,
                            gatewayTxnId = item.transactionId,
                            localAmount = localMatch.amount,
                            gatewayAmount = item.amount,
                            discrepancyType = DiscrepancyType.STATUS_MISMATCH,
                            description = "Status mismatch: Local=${localMatch.status}, Gateway=${item.status}"
                        )
                    )
                } else {
                    matchedCount++
                }
            }
        }

        // Check for unmatched local payments
        val gatewayTxnIds = gatewayStatements.map { it.transactionId }.toSet()
        for (local in localPayments) {
            val key = local.gatewayTransactionId.ifBlank { local.transactionRef }
            if (key.isNotBlank() && !gatewayTxnIds.contains(key)) {
                discrepancies.add(
                    ReconciliationDiscrepancy(
                        localPaymentId = local.id,
                        localPaymentNumber = local.paymentNumber,
                        gatewayTxnId = key,
                        localAmount = local.amount,
                        gatewayAmount = 0.0,
                        discrepancyType = DiscrepancyType.UNMATCHED_LOCAL,
                        description = "Local payment ${local.paymentNumber} not found in gateway settlement statement."
                    )
                )
            }
        }

        val report = ReconciliationReport(
            totalGatewayAmount = totalGatewayAmount,
            totalLocalAmount = totalLocalAmount,
            matchedCount = matchedCount,
            unmatchedCount = discrepancies.size,
            discrepancies = discrepancies
        )

        // Log reconciliation event
        paymentGatewayLogDao.insertLog(
            PaymentGatewayLogEntity(
                provider = provider,
                eventType = "RECONCILIATION",
                requestPayload = "Statement items: ${gatewayStatements.size}",
                responsePayload = "Matched: $matchedCount, Discrepancies: ${discrepancies.size}",
                statusCode = 200,
                timestamp = System.currentTimeMillis()
            )
        )

        return report
    }
}

data class GatewayStatementItem(
    val transactionId: String,
    val amount: Double,
    val status: String = "SUCCESS",
    val timestamp: Long = System.currentTimeMillis()
)
