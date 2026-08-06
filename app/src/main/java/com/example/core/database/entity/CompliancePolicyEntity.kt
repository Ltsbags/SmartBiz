package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "compliance_policies")
data class CompliancePolicyEntity(
    @PrimaryKey
    val policyId: String,
    val framework: String, // INTERNAL, GDPR, ISO27001, SOC2
    val title: String,
    val description: String,
    val isEnforced: Boolean = true,
    val complianceRulesJson: String = "{}",
    val status: String = "COMPLIANT", // COMPLIANT, NON_COMPLIANT, PENDING_REVIEW
    val lastEvaluatedAt: Long = System.currentTimeMillis()
)
