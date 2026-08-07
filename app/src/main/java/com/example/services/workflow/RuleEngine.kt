package com.example.services.workflow

import com.example.core.database.entity.RuleEntity
import com.example.services.workflow.models.DomainEvent

data class RuleValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)

class RuleEngine {

    fun validateRule(rule: RuleEntity): RuleValidationResult {
        val errors = mutableListOf<String>()
        if (rule.name.isBlank()) errors.add("Rule name cannot be empty")
        if (rule.field.isBlank()) errors.add("Rule field must be specified")
        if (rule.operator.isBlank()) errors.add("Rule operator must be specified")
        return RuleValidationResult(isValid = errors.isEmpty(), errors = errors)
    }

    fun evaluateRule(rule: RuleEntity, event: DomainEvent): Boolean {
        if (!rule.isActive) return false

        return when (rule.operator.uppercase()) {
            "AND" -> {
                val subRules = parseNestedRules(rule.nestedRulesJson)
                subRules.all { evaluateRule(it, event) }
            }
            "OR" -> {
                val subRules = parseNestedRules(rule.nestedRulesJson)
                subRules.any { evaluateRule(it, event) }
            }
            "NOT" -> {
                val subRules = parseNestedRules(rule.nestedRulesJson)
                if (subRules.isEmpty()) true else !evaluateRule(subRules.first(), event)
            }
            else -> evaluateSingleCondition(rule, event)
        }
    }

    private fun evaluateSingleCondition(rule: RuleEntity, event: DomainEvent): Boolean {
        val fieldValue = extractFieldValue(rule.field, event) ?: return false
        val targetValue = rule.value

        return when (rule.operator.uppercase()) {
            "EQUALS" -> fieldValue.equals(targetValue, ignoreCase = true)
            "NOT_EQUALS" -> !fieldValue.equals(targetValue, ignoreCase = true)
            "GREATER_THAN" -> (fieldValue.toDoubleOrNull() ?: 0.0) > (targetValue.toDoubleOrNull() ?: 0.0)
            "LESS_THAN" -> (fieldValue.toDoubleOrNull() ?: 0.0) < (targetValue.toDoubleOrNull() ?: 0.0)
            "GREATER_EQUAL" -> (fieldValue.toDoubleOrNull() ?: 0.0) >= (targetValue.toDoubleOrNull() ?: 0.0)
            "LESS_EQUAL" -> (fieldValue.toDoubleOrNull() ?: 0.0) <= (targetValue.toDoubleOrNull() ?: 0.0)
            "CONTAINS" -> fieldValue.contains(targetValue, ignoreCase = true)
            else -> false
        }
    }

    fun extractFieldValue(field: String, event: DomainEvent): String? {
        return when (field.uppercase()) {
            "BUSINESS", "BUSINESS_ID" -> event.businessId
            "BRANCH", "BRANCH_ID" -> event.branchId
            "ROLE" -> event.role
            "AMOUNT" -> event.payload["amount"]?.toString() ?: event.payload["totalAmount"]?.toString()
            "CUSTOMER_TYPE" -> event.payload["customerType"]?.toString()
            "OUTSTANDING" -> event.payload["outstanding"]?.toString()
            "STOCK_LEVEL" -> event.payload["stockLevel"]?.toString() ?: event.payload["stockQuantity"]?.toString()
            "PAYMENT_STATUS" -> event.payload["paymentStatus"]?.toString()
            else -> event.payload[field]?.toString()
        }
    }

    private fun parseNestedRules(json: String?): List<RuleEntity> {
        if (json.isNullOrBlank()) return emptyList()
        // Simple fallback parser for nested rule structures
        return try {
            emptyList() // Placeholder for complex nested rules
        } catch (e: Exception) {
            emptyList()
        }
    }
}
