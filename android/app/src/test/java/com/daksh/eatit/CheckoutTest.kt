package com.daksh.eatit

import org.junit.Assert.*
import org.junit.Test

class CheckoutTest {
    private val dish1 = Dish("1", "Bowl", "bowls", 24900)
    private val dish2 = Dish("2", "Pizza", "pizza", 32900)

    @Test
    fun cartReconciliationDetectsPriceChanges() {
        val initialCart = listOf(CartLine(dish1, 1))
        val updatedCatalog = Catalog(
            listOf(Category("bowls", "Bowls")),
            listOf(dish1.copy(pricePaise = 29900))
        )

        val reconciliation = reconcileCart(initialCart, updatedCatalog)

        assertEquals(1, reconciliation.updatedLines.size)
        assertEquals(29900L, reconciliation.updatedLines[0].dish.pricePaise)
        assertEquals(1, reconciliation.issues.size)
        assertEquals(CartIssueType.PRICE_CHANGED, reconciliation.issues[0].issueType)
        assertEquals(24900L, reconciliation.issues[0].oldPricePaise)
        assertEquals(29900L, reconciliation.issues[0].newPricePaise)
    }

    @Test
    fun cartReconciliationDetectsRemovedDishes() {
        val initialCart = listOf(CartLine(dish1, 1), CartLine(dish2, 2))
        val updatedCatalog = Catalog(
            listOf(Category("bowls", "Bowls")),
            listOf(dish1)
        )

        val reconciliation = reconcileCart(initialCart, updatedCatalog)

        assertEquals(1, reconciliation.updatedLines.size)
        assertEquals("1", reconciliation.updatedLines[0].dish.id)
        assertEquals(1, reconciliation.issues.size)
        assertEquals(CartIssueType.REMOVED, reconciliation.issues[0].issueType)
        assertEquals("2", reconciliation.issues[0].dishId)
    }

    @Test
    fun priceMismatchExceptionContainsUpdatedTotal() {
        val ex = PriceMismatchException(35000L)
        assertEquals(35000L, ex.updatedTotalPaise)
        assertTrue(ex.message!!.contains("Menu prices have updated"))
    }
}
