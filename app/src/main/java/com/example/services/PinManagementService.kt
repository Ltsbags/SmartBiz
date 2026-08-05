package com.example.services

import com.example.core.utils.SecurityUtils
import com.example.core.utils.ValidationUtils

class PinManagementService {

    fun validateNewPin(pin: String): Pair<Boolean, String> {
        return ValidationUtils.validatePinStrength(pin)
    }

    fun hashPin(pin: String): String {
        return SecurityUtils.hashPin(pin)
    }

    fun verifyPin(pin: String, storedHash: String): Boolean {
        return SecurityUtils.verifyPin(pin, storedHash)
    }
}
