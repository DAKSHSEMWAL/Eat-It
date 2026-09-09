package com.daksh.eatit

import org.junit.Assert.*
import org.junit.Test

class StaffTest {

    @Test
    fun orderStatusStateTransitions() {
        assertEquals("1", nextOrderStatus("0"))
        assertEquals("2", nextOrderStatus("1"))
        assertNull(nextOrderStatus("2"))
        assertNull(nextOrderStatus("unknown"))
    }

    @Test
    fun orderStatusLabels() {
        assertEquals("Placed", orderStatusLabel("0"))
        assertEquals("On its way", orderStatusLabel("1"))
        assertEquals("Delivered", orderStatusLabel("2"))
        assertEquals("Unknown", orderStatusLabel("99"))
    }

    @Test
    fun staffRoleIdentification() {
        val staffUser = Customer("1", "Staff Member", "staff@example.com", UserRole.STAFF)
        val customerUser = Customer("2", "Food Lover", "customer@example.com", UserRole.CUSTOMER)

        assertEquals(UserRole.STAFF, staffUser.role)
        assertEquals(UserRole.CUSTOMER, customerUser.role)
    }
}
