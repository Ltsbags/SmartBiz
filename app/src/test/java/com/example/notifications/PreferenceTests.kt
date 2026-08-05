package com.example.notifications

import com.example.core.database.entity.NotificationPreferenceEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferenceTests {

    @Test
    fun testPreferenceEntity() {
        val pref = NotificationPreferenceEntity(
            key = "low_stock_alerts",
            title = "Low Stock Alerts",
            description = "Notify on low inventory levels",
            category = "INVENTORY",
            isEnabled = true
        )

        assertEquals("low_stock_alerts", pref.key)
        assertTrue(pref.isEnabled)
    }
}
