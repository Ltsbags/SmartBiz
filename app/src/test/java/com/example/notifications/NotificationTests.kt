package com.example.notifications

import com.example.core.database.entity.NotificationEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NotificationTests {

    @Test
    fun testNotificationEntityCreation() {
        val notification = NotificationEntity(
            title = "Low Stock Alert",
            message = "Product ABC is below reorder point",
            type = "INVENTORY",
            severity = "WARNING",
            priority = "HIGH"
        )

        assertNotNull(notification.id)
        assertEquals("INVENTORY", notification.type)
        assertEquals("UNREAD", notification.status)
        assertEquals("HIGH", notification.priority)
    }

    @Test
    fun testNotificationReadStatus() {
        val notification = NotificationEntity(
            title = "Test Notification",
            message = "Test message",
            type = "SYSTEM"
        )

        val readNotif = notification.copy(status = "READ", readDate = System.currentTimeMillis())
        assertEquals("READ", readNotif.status)
        assertNotNull(readNotif.readDate)
    }
}
