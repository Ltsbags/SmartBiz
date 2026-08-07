package com.example.communication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.DatabaseHelper
import com.example.core.database.entity.CommunicationMessageEntity
import com.example.repositories.CommunicationRepository
import com.example.services.communication.DeliveryStatus
import com.example.services.communication.DeliveryTrackingService
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
class DeliveryTrackingTest {

    private lateinit var context: Context
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var repository: CommunicationRepository
    private lateinit var deliveryTracking: DeliveryTrackingService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dbHelper = DatabaseHelper.getInstance(context)
        repository = CommunicationRepository(dbHelper.communicationDao)
        deliveryTracking = DeliveryTrackingService(repository)
    }

    @Test
    fun testRecordStatusChangeAndLogCreation() = runBlocking {
        val msgId = repository.saveMessage(
            CommunicationMessageEntity(
                messageGuid = "guid-101",
                channel = "WHATSAPP",
                recipient = "+919876543210",
                recipientName = "Test User",
                subject = "Test Subject",
                body = "Test Body",
                status = "QUEUED"
            )
        )

        deliveryTracking.recordStatusChange(msgId, DeliveryStatus.SENDING, "Dispatching to WhatsApp API")

        val updatedMsg = repository.getMessageById(msgId)
        assertNotNull(updatedMsg)
        assertEquals("SENDING", updatedMsg!!.status)

        val logs = repository.getLogsForMessage(msgId).first()
        assertTrue(logs.isNotEmpty())
        assertEquals("SENDING", logs.first().eventType)
    }

    @Test
    fun testWebhookDeliveryReceiptProcessing() = runBlocking {
        val msgId = repository.saveMessage(
            CommunicationMessageEntity(
                messageGuid = "guid-102",
                channel = "EMAIL",
                recipient = "test@example.com",
                subject = "Invoice Email",
                body = "Invoice body",
                status = "SENDING"
            )
        )

        val success = deliveryTracking.processWebhookDeliveryReceipt(
            messageId = msgId,
            providerStatus = "DELIVERED",
            providerMessageId = "sg_12345"
        )

        assertTrue(success)
        val updated = repository.getMessageById(msgId)
        assertEquals("DELIVERED", updated!!.status)
    }
}
