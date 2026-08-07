package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.DatabaseHelper
import com.example.repositories.PaymentRepository
import com.example.repositories.ReportingRepository
import com.example.services.bi.AggregationService
import com.example.services.bi.BIService
import com.example.services.bi.ForecastingService
import com.example.services.bi.KPIEngineService
import com.example.services.payment.PaymentEngineService
import com.example.services.payment.PaymentLinkService.PaymentLinkService
import com.example.services.payment.PaymentReconciliationService.GatewayStatementItem
import com.example.services.payment.PaymentReconciliationService.PaymentReconciliationService
import com.example.services.payment.QrPaymentService.QrPaymentService
import com.example.services.payment.RefundService.RefundService
import com.example.services.payment.models.PaymentEngineRequest
import com.example.services.payment.models.RefundEngineRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PaymentAndBIIntegrationTest {

    private lateinit var context: Context
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var reportingRepository: ReportingRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dbHelper = DatabaseHelper.getInstance(context)
        paymentRepository = PaymentRepository(dbHelper)

        val aggregationService = AggregationService(
            dbHelper.reportDao,
            dbHelper.aggregatedMetricsDao,
            dbHelper.branchMetricsDao
        )
        val kpiEngineService = KPIEngineService(dbHelper.kpiDefinitionDao)
        val forecastingService = ForecastingService(dbHelper.forecastingSnapshotDao)

        val biService = BIService(
            reportDao = dbHelper.reportDao,
            aggregatedMetricsDao = dbHelper.aggregatedMetricsDao,
            branchMetricsDao = dbHelper.branchMetricsDao,
            kpiDefinitionDao = dbHelper.kpiDefinitionDao,
            reportDefinitionDao = dbHelper.reportDefinitionDao,
            savedReportSnapshotDao = dbHelper.savedReportSnapshotDao,
            forecastingSnapshotDao = dbHelper.forecastingSnapshotDao,
            aggregationService = aggregationService,
            kpiEngineService = kpiEngineService,
            forecastingService = forecastingService
        )
        reportingRepository = ReportingRepository(biService)
    }

    @Test
    fun testPaymentProcessingAndAccountingIntegration() = runBlocking {
        val request = PaymentEngineRequest(
            amount = 1500.0,
            customerName = "Acme Corp",
            invoiceNumber = "INV-2026-001",
            paymentMethod = "UPI",
            preferredProvider = "UPI"
        )

        val result = paymentRepository.processPayment(request)
        assertTrue(result.isSuccess)
        assertNotNull(result.paymentId)
        assertTrue(result.paymentNumber.startsWith("PAY-"))

        val payments = paymentRepository.allPayments.first()
        assertTrue(payments.isNotEmpty())
        assertEquals("SUCCESS", payments.first().status)
    }

    @Test
    fun testPaymentLinkAndQrGeneration() = runBlocking {
        val request = PaymentEngineRequest(
            amount = 2500.0,
            customerName = "Jane Doe",
            invoiceNumber = "INV-2026-002",
            paymentMethod = "RAZORPAY"
        )

        val linkReq = paymentRepository.generatePaymentLink(request, 48)
        assertNotNull(linkReq.id)
        assertTrue(linkReq.paymentLinkUrl.contains("rzp.io"))
        assertTrue(linkReq.qrCodePayload.startsWith("upi://pay"))

        val qrBmp = paymentRepository.generateQrBitmap(linkReq.qrCodePayload)
        assertNotNull(qrBmp)
        assertEquals(300, qrBmp.width)
    }

    @Test
    fun testRefundProcessing() = runBlocking {
        val request = PaymentEngineRequest(
            amount = 1000.0,
            customerName = "John Smith",
            invoiceNumber = "INV-2026-003",
            paymentMethod = "CASH"
        )
        val payRes = paymentRepository.processPayment(request)
        assertTrue(payRes.isSuccess)

        val refundReq = RefundEngineRequest(
            paymentId = payRes.paymentId!!,
            amount = 500.0,
            reason = "Customer returned item"
        )

        val refundRes = paymentRepository.processRefund(refundReq)
        assertTrue(refundRes.isSuccess)
        assertNotNull(refundRes.refundId)

        val refunds = paymentRepository.allRefunds.first()
        assertTrue(refunds.isNotEmpty())
        assertEquals(500.0, refunds.first().amount, 0.01)
    }

    @Test
    fun testGatewayReconciliationEngine() = runBlocking {
        val request = PaymentEngineRequest(
            amount = 3000.0,
            customerName = "Global Logistics",
            invoiceNumber = "INV-2026-004",
            paymentMethod = "UPI"
        )
        val payRes = paymentRepository.processPayment(request)
        val allUpiPayments = paymentRepository.allPayments.first().filter { 
            it.gatewayProvider.equals("UPI", ignoreCase = true) || it.paymentMethod.equals("UPI", ignoreCase = true) 
        }

        val gatewayStatements = allUpiPayments.map { payment ->
            GatewayStatementItem(
                transactionId = payment.gatewayTransactionId.ifBlank { payment.transactionRef },
                amount = payment.amount,
                status = payment.status
            )
        }

        val report = paymentRepository.reconcileGateway("UPI", gatewayStatements)
        assertNotNull(report)
        assertEquals(allUpiPayments.size, report.matchedCount)
        assertEquals(0, report.unmatchedCount)
    }

    @Test
    fun testExecutiveDashboardAndKpiEngine() = runBlocking {
        val now = System.currentTimeMillis()
        val summary = reportingRepository.getExecutiveSummary(now - 86400000L * 30, now)

        assertNotNull(summary)
        assertTrue(summary.kpiEvaluations.isNotEmpty())
        assertTrue(summary.branchConsolidation.isNotEmpty())
    }

    @Test
    fun testAIForecastingService() = runBlocking {
        val (revenueForecast, expenseForecast) = reportingRepository.runRevenueAndExpenseForecast(60, 14)

        assertNotNull(revenueForecast)
        assertNotNull(expenseForecast)
        assertEquals(14, revenueForecast.size)
        assertEquals(14, expenseForecast.size)
        assertTrue(revenueForecast.first().predictedValue >= 0.0)
    }
}
