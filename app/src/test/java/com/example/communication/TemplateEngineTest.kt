package com.example.communication

import com.example.core.database.entity.CommunicationTemplateEntity
import com.example.services.communication.TemplateEngineService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TemplateEngineTest {

    private lateinit var templateEngine: TemplateEngineService

    @Before
    fun setUp() {
        templateEngine = TemplateEngineService()
    }

    @Test
    fun testVariableReplacementInTemplate() {
        val template = CommunicationTemplateEntity(
            templateId = "INV_TEST",
            name = "Test Invoice Template",
            channel = "WHATSAPP",
            subjectTemplate = "Invoice {{invoice_number}} for {{business_name}}",
            bodyTemplate = "Hello {{customer_name}}, your invoice #{{invoice_number}} of amount {{invoice_amount}} is ready.",
            category = "BILLING"
        )

        val vars = mapOf(
            "invoice_number" to "INV-2026-999",
            "business_name" to "Acme Corp",
            "customer_name" to "Alice Smith",
            "invoice_amount" to "₹12,500"
        )

        val rendered = templateEngine.renderTemplate(template, vars)

        assertEquals("Invoice INV-2026-999 for Acme Corp", rendered.subject)
        assertEquals("Hello Alice Smith, your invoice #INV-2026-999 of amount ₹12,500 is ready.", rendered.body)
        assertTrue(rendered.missingVariables.isEmpty())
    }

    @Test
    fun testMissingVariablesHandling() {
        val template = CommunicationTemplateEntity(
            templateId = "REM_TEST",
            name = "Reminder",
            channel = "SMS",
            subjectTemplate = "",
            bodyTemplate = "Dear {{customer_name}}, payment {{amount}} is past due {{due_date}}.",
            category = "BILLING"
        )

        val vars = mapOf(
            "customer_name" to "Bob"
        )

        val rendered = templateEngine.renderTemplate(template, vars)

        assertTrue(rendered.missingVariables.contains("amount"))
        assertTrue(rendered.missingVariables.contains("due_date"))
        assertTrue(rendered.body.contains("[Missing: amount]"))
    }

    @Test
    fun testExtractVariablesFromText() {
        val text = "Hi {{customer_name}}, invoice #{{invoice_number}} amount {{invoice_amount}} balance {{outstanding_amount}}."
        val extracted = templateEngine.extractVariables(text)

        assertEquals(4, extracted.size)
        assertTrue(extracted.contains("customer_name"))
        assertTrue(extracted.contains("invoice_number"))
        assertTrue(extracted.contains("invoice_amount"))
        assertTrue(extracted.contains("outstanding_amount"))
    }
}
