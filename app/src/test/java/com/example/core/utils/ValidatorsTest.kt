package com.example.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun testEmailValidation() {
        assertTrue(Validators.isValidEmail("user@example.com"))
        assertTrue(Validators.isValidEmail("")) // optional
        assertFalse(Validators.isValidEmail("invalid-email"))
    }

    @Test
    fun testPhoneValidation() {
        assertTrue(Validators.isValidPhone("+1234567890"))
        assertTrue(Validators.isValidPhone("9876543210"))
        assertFalse(Validators.isValidPhone("123"))
    }

    @Test
    fun testPriceAndQuantityValidation() {
        assertTrue(Validators.isValidPrice("100.50"))
        assertFalse(Validators.isValidPrice("-10.0"))
        assertFalse(Validators.isValidPrice("abc"))

        assertTrue(Validators.isValidQuantity("5"))
        assertFalse(Validators.isValidQuantity("-2"))
    }

    @Test
    fun testProductValidation() {
        val validResult = Validators.validateProduct("Laptop", "999.99", "Electronics")
        assertTrue(validResult.isValid)

        val invalidResult = Validators.validateProduct("", "999.99", "Electronics")
        assertFalse(invalidResult.isValid)
        assertEquals("Product name cannot be empty", invalidResult.errorMessage)
    }
}
