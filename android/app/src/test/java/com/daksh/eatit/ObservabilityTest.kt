package com.daksh.eatit

import com.daksh.eatit.telemetry.EatItLogger
import org.junit.Assert.*
import org.junit.Test

class ObservabilityTest {

    @Test
    fun testPhoneRedaction() {
        val phone = "+12345678901"
        val redacted = EatItLogger.redactPhone(phone)
        assertFalse(redacted.contains("3456789"))
        assertTrue(redacted.startsWith("+1"))
        assertTrue(redacted.endsWith("01"))
    }

    @Test
    fun testEmailRedaction() {
        val email = "daksh@example.com"
        val redacted = EatItLogger.redactEmail(email)
        assertFalse(redacted.contains("aksh"))
        assertTrue(redacted.startsWith("d***h@example.com"))
    }

    @Test
    fun testAddressRedaction() {
        val address = "123 Main Street"
        val redacted = EatItLogger.redactAddress(address)
        assertFalse(redacted.contains("Main"))
        assertFalse(redacted.contains("Street"))
        assertTrue(redacted.contains("M***n"))
        assertTrue(redacted.contains("S***t"))
    }

    @Test
    fun testNameRedaction() {
        val name = "Daksh Semwal"
        val redacted = EatItLogger.redactName(name)
        assertFalse(redacted.contains("aksh"))
        assertFalse(redacted.contains("emwal"))
        assertTrue(redacted.contains("D***"))
        assertTrue(redacted.contains("S***"))
    }

    @Test
    fun testSanitizeValueMap() {
        val details = mapOf(
            "name" to "Daksh Semwal",
            "phone" to "+12345678901",
            "address" to "123 Main St",
            "requestId" to "req-999"
        )
        val sanitizedName = EatItLogger.sanitizeValue("name", details["name"]!!)
        val sanitizedPhone = EatItLogger.sanitizeValue("phone", details["phone"]!!)
        val sanitizedAddress = EatItLogger.sanitizeValue("address", details["address"]!!)
        val sanitizedReqId = EatItLogger.sanitizeValue("requestId", details["requestId"]!!)

        assertFalse(sanitizedName.contains("Semwal"))
        assertFalse(sanitizedPhone.contains("345678"))
        assertFalse(sanitizedAddress.contains("Main"))
        assertEquals("req-999", sanitizedReqId)
    }
}
