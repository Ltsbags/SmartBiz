package com.example.features.audit.model

enum class ExportFormat(val extension: String, val mimeType: String, val displayName: String) {
    PDF("pdf", "application/pdf", "PDF Document"),
    CSV("csv", "text/csv", "CSV Spreadsheet"),
    EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel Workbook")
}
