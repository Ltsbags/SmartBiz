package com.example.core.utils

import java.util.regex.Pattern

object Validators {

    private val EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$"
    )

    private val PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{7,15}\$"
    )

    private val GSTIN_PATTERN = Pattern.compile(
        "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}\$"
    )

    private val PAN_PATTERN = Pattern.compile(
        "^[A-Z]{5}[0-9]{4}[A-Z]{1}\$"
    )

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return true // Optional unless required
        return EMAIL_PATTERN.matcher(email.trim()).matches()
    }

    fun isValidPhone(phone: String): Boolean {
        if (phone.isBlank()) return false
        val digitsOnly = phone.replace(Regex("[^0-9+]"), "")
        return PHONE_PATTERN.matcher(digitsOnly).matches()
    }

    fun isValidGst(gst: String): Boolean {
        if (gst.isBlank()) return true
        return GSTIN_PATTERN.matcher(gst.trim().uppercase()).matches()
    }

    fun isValidPan(pan: String): Boolean {
        if (pan.isBlank()) return true
        return PAN_PATTERN.matcher(pan.trim().uppercase()).matches()
    }

    fun isValidQuantity(quantityStr: String): Boolean {
        val qty = quantityStr.toIntOrNull() ?: return false
        return qty >= 0
    }

    fun isValidPrice(priceStr: String): Boolean {
        val price = priceStr.toDoubleOrNull() ?: return false
        return price >= 0.0
    }

    fun isValidDiscount(discountStr: String, totalAmount: Double = Double.MAX_VALUE): Boolean {
        val discount = discountStr.toDoubleOrNull() ?: return false
        return discount in 0.0..totalAmount
    }

    fun isValidPercentage(percentageStr: String): Boolean {
        val pct = percentageStr.toDoubleOrNull() ?: return false
        return pct in 0.0..100.0
    }

    fun isNonEmpty(text: String): Boolean {
        return text.trim().isNotEmpty()
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateProduct(name: String, priceStr: String, category: String): ValidationResult {
        if (name.isBlank()) return ValidationResult(false, "Product name cannot be empty")
        if (!isValidPrice(priceStr)) return ValidationResult(false, "Please enter a valid non-negative price")
        if (category.isBlank()) return ValidationResult(false, "Please select a category")
        return ValidationResult(true)
    }

    fun validateCustomer(name: String, phone: String, email: String): ValidationResult {
        if (name.isBlank()) return ValidationResult(false, "Customer name cannot be empty")
        if (phone.isNotBlank() && !isValidPhone(phone)) return ValidationResult(false, "Please enter a valid phone number")
        if (email.isNotBlank() && !isValidEmail(email)) return ValidationResult(false, "Please enter a valid email address")
        return ValidationResult(true)
    }
}
