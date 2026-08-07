package com.example.services.communication

import com.example.core.database.entity.CommunicationTemplateEntity

data class RenderedMessage(
    val subject: String,
    val body: String,
    val channel: String,
    val missingVariables: List<String> = emptyList()
)

class TemplateEngineService {

    companion object {
        val SUPPORTED_VARIABLES = listOf(
            "business_name",
            "customer_name",
            "invoice_number",
            "invoice_amount",
            "due_date",
            "outstanding_amount",
            "branch_name",
            "supplier_name",
            "po_number",
            "statement_period",
            "product_name",
            "stock_quantity",
            "payment_link"
        )
    }

    fun renderTemplate(
        template: CommunicationTemplateEntity,
        variables: Map<String, String>
    ): RenderedMessage {
        val missingVars = mutableListOf<String>()

        val renderedSubject = replacePlaceholders(template.subjectTemplate, variables, missingVars)
        val renderedBody = replacePlaceholders(template.bodyTemplate, variables, missingVars)

        return RenderedMessage(
            subject = renderedSubject,
            body = renderedBody,
            channel = template.channel,
            missingVariables = missingVars.distinct()
        )
    }

    fun renderRawText(
        textTemplate: String,
        variables: Map<String, String>
    ): String {
        val dummyMissing = mutableListOf<String>()
        return replacePlaceholders(textTemplate, variables, dummyMissing)
    }

    private fun replacePlaceholders(
        templateText: String,
        variables: Map<String, String>,
        missingVarsList: MutableList<String>
    ): String {
        if (templateText.isBlank()) return ""

        var result = templateText
        val regex = Regex("\\{\\{([a-zA-Z0-9_]+)\\}\\}|\\{([a-zA-Z0-9_]+)\\}")

        regex.findAll(templateText).forEach { match ->
            val varName = match.groupValues[1].ifEmpty { match.groupValues[2] }
            val replacement = variables[varName]
            if (replacement != null) {
                result = result.replace(match.value, replacement)
            } else {
                missingVarsList.add(varName)
                result = result.replace(match.value, "[Missing: $varName]")
            }
        }

        return result
    }

    fun extractVariables(templateText: String): List<String> {
        val regex = Regex("\\{\\{([a-zA-Z0-9_]+)\\}\\}|\\{([a-zA-Z0-9_]+)\\}")
        return regex.findAll(templateText).map { match ->
            match.groupValues[1].ifEmpty { match.groupValues[2] }
        }.distinct().toList()
    }

    fun formatForWhatsAppMarkdown(text: String): String {
        // Formats headings and labels for WhatsApp text readability
        return text
    }

    fun formatForEmailHtml(subject: String, body: String, businessName: String): String {
        val bodyParagraphs = body.split("\n").joinToString("") { "<p style=\"margin-bottom:12px;\">$it</p>" }
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f6f8; margin:0; padding:20px; }
                    .card { background: #ffffff; border-radius: 8px; padding: 24px; max-width: 600px; margin: 0 auto; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
                    .header { border-bottom: 2px solid #2196f3; padding-bottom: 12px; margin-bottom: 20px; font-weight: bold; color: #1e293b; }
                    .footer { font-size: 12px; color: #64748b; margin-top: 24px; border-top: 1px solid #e2e8f0; padding-top: 12px; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">$businessName</div>
                    <div class="content">
                        $bodyParagraphs
                    </div>
                    <div class="footer">
                        This is an automated communication dispatched from $businessName.
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
