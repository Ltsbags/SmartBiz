package com.example.services

class InputSanitizationService {

    fun sanitizeText(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        return input
            .replace("\u0000", "")
            .replace("<script", "&lt;script", ignoreCase = true)
            .replace("</script>", "&lt;/script&gt;", ignoreCase = true)
            .trim()
    }

    fun sanitizeSqlSearchQuery(query: String?): String {
        if (query.isNullOrEmpty()) return ""
        return query
            .replace("'", "''")
            .replace(";", "")
            .replace("--", "")
            .replace("/*", "")
            .replace("*/", "")
            .trim()
    }

    fun sanitizeAmount(input: String): Double {
        val clean = input.replace("[^0-9.]".toRegex(), "")
        return clean.toDoubleOrNull() ?: 0.0
    }

    fun sanitizePin(input: String): String {
        return input.replace("[^0-9]".toRegex(), "").take(6)
    }
}
