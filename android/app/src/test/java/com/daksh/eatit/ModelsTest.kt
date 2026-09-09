package com.daksh.eatit

import org.junit.Assert.*
import org.junit.Test

class ModelsTest {
    private val dish = Dish("1", "Bowl", "bowls", 24900)

    @Test fun pricesUseExactPaise() {
        assertEquals(24999L, parsePrice("249.99"))
        assertEquals(100L, parsePrice("1.000"))
        listOf("1.001", "-1", "NaN", "", "1000000.01").forEach { assertNull(parsePrice(it)) }
    }
    @Test fun quantityUpdatesReplaceRatherThanDuplicate() {
        val cart = updateCart(listOf(CartLine(dish, 1)), dish, 3)
        assertEquals(1, cart.size)
        assertEquals(74700L, cartTotal(cart))
        assertTrue(updateCart(cart, dish, 0).isEmpty())
    }
    @Test(expected = IllegalArgumentException::class) fun excessiveQuantityRejected() {
        updateCart(emptyList(), dish, 100)
    }
    @Test(expected = ArithmeticException::class) fun overflowDoesNotWrapIntoNegativeTotal() {
        cartTotal(listOf(CartLine(dish.copy(pricePaise = Long.MAX_VALUE), 2)))
    }
    @Test fun deliveryRequiresContactAndCompleteAddress() {
        assertTrue(Delivery("Daksh", "+919876543210", "12 Garden Road, Bengaluru").valid())
        assertFalse(Delivery("D", "123", "Street").valid())
        assertFalse(Delivery("Daksh", "abcdefghij", "12 Garden Road").valid())
    }
}
