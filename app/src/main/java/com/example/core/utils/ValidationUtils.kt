package com.example.core.utils

object ValidationUtils {

    fun isValidPhone(phone: String): Boolean {
        if (phone.isBlank()) return false
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        return cleanPhone.length in 8..15
    }

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return true // Email optional in offline mode
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return emailRegex.matches(email.trim())
    }

    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }

    fun validatePinStrength(pin: String): Pair<Boolean, String> {
        if (pin.length !in 4..6) {
            return Pair(false, "PIN must be between 4 and 6 digits")
        }

        if (!pin.all { it.isDigit() }) {
            return Pair(false, "PIN must contain digits only")
        }

        // Check for repeating digits (e.g., 0000, 1111)
        if (pin.toSet().size == 1) {
            return Pair(false, "PIN cannot consist of repeating digits")
        }

        // Check for simple sequential PINs (1234, 4321)
        val isAscending = pin.zipWithNext().all { (a, b) -> b - a == 1 }
        val isDescending = pin.zipWithNext().all { (a, b) -> a - b == 1 }

        if (isAscending || isDescending) {
            return Pair(false, "PIN cannot be a simple sequential sequence (e.g. 1234)")
        }

        return Pair(true, "PIN strength is strong")
    }
}
