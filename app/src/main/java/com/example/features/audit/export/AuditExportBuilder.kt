package com.example.features.audit.export

import com.example.core.database.entity.AuditLogEntity
import com.example.features.audit.model.ExportFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuditExportBuilder {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun buildCsvString(audits: List<AuditLogEntity>): String {
        val sb = StringBuilder()
        sb.append("Audit ID,Timestamp,Module,Action,Severity,User ID,User Name,Entity Name,Entity ID,Description,Device,IP Address\n")

        for (audit in audits) {
            val dateStr = dateFormat.format(Date(audit.timestamp))
            val escapedDesc = escapeCsv(audit.description)
            val escapedUser = escapeCsv(audit.userName)
            val escapedDevice = escapeCsv(audit.deviceName)

            sb.append("${audit.auditId},\"$dateStr\",\"${audit.module}\",\"${audit.action}\",\"${audit.severity}\",\"${audit.userId}\",\"$escapedUser\",\"${audit.entityName}\",\"${audit.entityId}\",\"$escapedDesc\",\"$escapedDevice\",\"${audit.ipAddress}\"\n")
        }

        return sb.toString()
    }

    fun prepareExportMetadata(audits: List<AuditLogEntity>, format: ExportFormat): ExportMetadata {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Audit_Report_$timestamp.${format.extension}"

        return ExportMetadata(
            fileName = fileName,
            format = format,
            recordCount = audits.size,
            generatedAt = System.currentTimeMillis()
        )
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    data class ExportMetadata(
        val fileName: String,
        val format: ExportFormat,
        val recordCount: Int,
        val generatedAt: Long
    )
}
