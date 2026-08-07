package com.example.communication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.DatabaseHelper
import com.example.repositories.CommunicationRepository
import com.example.services.communication.CommunicationEngineService
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
class CommunicationEngineTest {

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
    fun testDirectMessageDispatch() = runBlocking {
        val messageId = engine.sendDirectMessage(
            context = context,
            channel = "WHATSAPP",
            recipient = "+919876543210",
            recipientName = "John Doe",
            subject = "Direct Notice",
            body = "Welcome to SmartBiz ERP"
        )

        assertTrue(messageId > 0)
        val msg = repository.getMessageById(messageId)
        assertNotNull(msg)
        assertEquals("DELIVERED", msg!!.status)
    }

    @Test
    fun testTemplatedMessageDispatch() = runBlocking {
        repository.seedDefaultTemplatesIfEmpty()

        val vars = mapOf(
            "customer_name" to "Alice Johnson",
            "invoice_number" to "INV-2026-001",
            "invoice_amount" to "₹4,200",
            "due_date" to "12-Aug-2026",
            "business_name" to "SmartBiz Enterprise"
        )

        val messageId = engine.sendTemplatedMessage(
            context = context,
            templateId = "INV_SEND",
            recipient = "+919988776655",
            recipientName = "Alice Johnson",
            variables = vars
        )

        assertNotNull(messageId)
        val msg = repository.getMessageById(messageId!!)
        assertNotNull(msg)
        assertTrue(msg!!.body.contains("INV-2026-001"))
        assertTrue(msg.body.contains("₹4,200"))
    }
}
