package com.example.notifications

import com.example.core.database.entity.ReminderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderTests {

    @Test
    fun testReminderEntityDefaults() {
        val reminder = ReminderEntity(
            title = "Supplier Invoice Due",
            description = "Pay Invoice #1023 to Global Wholesale",
            module = "SUPPLIER_PAYMENT",
            repeatType = "WEEKLY"
        )

        assertTrue(reminder.isEnabled)
        assertEquals("SUPPLIER_PAYMENT", reminder.module)
        assertEquals("WEEKLY", reminder.repeatType)
    }
}
