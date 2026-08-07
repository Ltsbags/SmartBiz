package com.example.communication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.DatabaseHelper
import com.example.core.database.entity.CommunicationAutomationRuleEntity
import com.example.repositories.CommunicationRepository
import com.example.services.communication.CommunicationEngineService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AutomationRuleTest {

    private lateinit var context: Context
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var repository: CommunicationRepository
    private lateinit var engine: CommunicationEngineService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dbHelper = DatabaseHelper.getInstance(context)
        repository = CommunicationRepository(dbHelper.communicationDao)
        engine = CommunicationEngineService(repository)
    }

    @Test
    fun testDomainEventTriggersAutomation() = runBlocking {
        repository.seedDefaultTemplatesIfEmpty()

        val vars = mapOf(
            "customer_name" to "Bob Ross",
            "invoice_number" to "INV-2026-888",
            "invoice_amount" to "₹8,900",
            "due_date" to "20-Aug-2026",
            "business_name" to "SmartBiz Enterprise"
        )

        engine.handleBusinessDomainEvent(
            context = context,
            eventType = "INVOICE_CREATED",
            recipient = "+919876543210",
            recipientName = "Bob Ross",
            variables = vars,
            relatedEntityType = "INVOICE",
            relatedEntityId = "INV-2026-888"
        )

        val messages = repository.allMessages.first()
        assertTrue(messages.isNotEmpty())
        val matched = messages.find { it.relatedEntityId == "INV-2026-888" }
        assertTrue(matched != null)
        assertEquals("WHATSAPP", matched!!.channel)
        assertTrue(matched.body.contains("INV-2026-888"))
    }
}
