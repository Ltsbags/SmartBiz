package com.example.communication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.DatabaseHelper
import com.example.repositories.CommunicationRepository
import com.example.services.communication.CommunicationEngineService
import com.example.services.communication.CommunicationRetryService
import com.example.services.communication.DeliveryTrackingService
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
class CommunicationRetryTest {

    private lateinit var context: Context
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var repository: CommunicationRepository
    private lateinit var deliveryTracking: DeliveryTrackingService
    private lateinit var retryService: CommunicationRetryService
    private lateinit var engine: CommunicationEngineService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dbHelper = DatabaseHelper.getInstance(context)
        repository = CommunicationRepository(dbHelper.communicationDao)
        deliveryTracking = DeliveryTrackingService(repository)
        retryService = CommunicationRetryService(repository, deliveryTracking)
        engine = CommunicationEngineService(repository)
    }

    @Test
    fun testBackoffCalculation() {
        val backoff1 = retryService.calculateNextBackoffMs(1)
        val backoff2 = retryService.calculateNextBackoffMs(2)
        val backoff3 = retryService.calculateNextBackoffMs(3)

        assertEquals(5000L, backoff1)
        assertEquals(10000L, backoff2)
        assertEquals(20000L, backoff3)
    }

    @Test
    fun testManualRetryExecution() = runBlocking {
        val msgId = engine.sendDirectMessage(
            context = context,
            channel = "SMS",
            recipient = "+919876543210",
            body = "Test Retry SMS"
        )

        repository.updateMessageStatus(msgId, "FAILED", "Forced failure for test")

        val retried = retryService.scheduleManualRetry(context, msgId) { ctx, msg ->
            engine.dispatchMessage(ctx, msg)
        }

        assertTrue(retried)
        val updatedMsg = repository.getMessageById(msgId)
        assertEquals("DELIVERED", updatedMsg!!.status)
    }
}
