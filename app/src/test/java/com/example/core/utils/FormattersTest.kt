package com.example.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormattersTest {

    @Test
    fun testCurrencyFormatting() {
        val formatted = Formatters.formatCurrency(1250.5, "$")
        assertTrue(formatted.contains("1,250.50") || formatted.contains("1250.50"))
        assertTrue(formatted.startsWith("$"))
    }

    @Test
    fun testTruncateText() {
        val shortText = "Hello"
        assertEquals("Hello", Formatters.truncateText(shortText, 10))

        val longText = "This is a very long string for testing"
        val truncated = Formatters.truncateText(longText, 15)
        assertEquals("This is a ve...", truncated)
    }

    @Test
    fun testFormatPercentage() {
        assertEquals("15.5%", Formatters.formatPercentage(15.5))
    }
}
