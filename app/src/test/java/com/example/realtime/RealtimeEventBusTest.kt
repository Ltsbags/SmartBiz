package com.example.realtime

import com.example.core.realtime.EventBus
import com.example.core.realtime.RealtimeEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RealtimeEventBusTest {

    private lateinit var eventBus: EventBus

    @Before
    fun setUp() {
        eventBus = EventBus.getInstance()
    }

    @Test
    fun testPublishAndSubscribeEventBus() = runTest {
        val testInvoice = RealtimeEvent.InvoiceCreated(
            id = "INV_TEST_001",
            invoiceNumber = "INV-2026-99",
            customerName = "Acme Corp",
            totalAmount = 2500.0,
            createdBy = "Unit Test"
        )

        eventBus.publish(testInvoice)

        val received = eventBus.subscribeByEventType("INVOICE_CREATED").first()
        assertNotNull(received)
        assertEquals("INV_TEST_001", received.eventId)
        assertEquals("SALES", received.module)
        assertEquals("INVOICE_CREATED", received.eventType)
    }

    @Test
    fun testSubscribeByModuleFilter() = runTest {
        val securityAlert = RealtimeEvent.SecurityAlert(
            alertId = "SEC_001",
            alertTitle = "Brute Force Warning",
            message = "Multiple failed PIN attempts",
            alertSeverity = "HIGH"
        )

        eventBus.publish(securityAlert)

        val received = eventBus.subscribeByModule("SECURITY").first()
        assertEquals("SECURITY", received.module)
        assertEquals("SEC_001", received.eventId)
    }
}
