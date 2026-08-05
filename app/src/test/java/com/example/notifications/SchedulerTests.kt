package com.example.notifications

import com.example.core.database.entity.ScheduledTaskEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SchedulerTests {

    @Test
    fun testScheduledTaskEntity() {
        val task = ScheduledTaskEntity(
            taskName = "Daily Business Summary",
            taskType = "DAILY_SUMMARY",
            cronOrFrequency = "DAILY"
        )

        assertEquals("DAILY_SUMMARY", task.taskType)
        assertEquals("PENDING", task.status)
        assertTrue(task.isEnabled)
    }
}
