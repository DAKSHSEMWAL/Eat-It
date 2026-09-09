package com.daksh.eatit

import com.daksh.eatit.db.toEntity
import org.junit.Assert.*
import org.junit.Test

class OfflineCatalogTest {

    @Test
    fun catalogOfflineMetaDefaults() {
        val catalog = Catalog(
            categories = DemoCatalog.categories,
            dishes = DemoCatalog.dishes
        )
        assertFalse(catalog.isOffline)
        assertEquals(0L, catalog.lastSyncedTimestamp)
    }

    @Test
    fun categoryAndDishEntityConversion() {
        val category = Category("bowls", "Bowls")
        val categoryEntity = category.toEntity()
        assertEquals("bowls", categoryEntity.id)
        assertEquals("Bowls", categoryEntity.name)
        assertEquals(category, categoryEntity.toModel())

        val dish = Dish("1", "Harvest Bowl", "bowls", 25000, "img.png", "Fresh veggies")
        val dishEntity = dish.toEntity()
        assertEquals("1", dishEntity.id)
        assertEquals("Harvest Bowl", dishEntity.name)
        assertEquals("bowls", dishEntity.categoryId)
        assertEquals(25000L, dishEntity.pricePaise)
        assertEquals(dish, dishEntity.toModel())
    }

    @Test
    fun cartReconciliationDetectsPriceChangeAndRemoval() {
        val oldDish1 = Dish("1", "Bowl", "bowls", 20000)
        val oldDish2 = Dish("2", "Pizza", "pizza", 30000)
        val cartLines = listOf(CartLine(oldDish1, 2), CartLine(oldDish2, 1))

        val updatedDish1 = oldDish1.copy(pricePaise = 22000) // Price changed
        val currentCatalog = Catalog(
            categories = DemoCatalog.categories,
            dishes = listOf(updatedDish1) // Dish 2 removed from catalog
        )

        val result = reconcileCart(cartLines, currentCatalog)

        assertEquals(1, result.updatedLines.size)
        assertEquals(updatedDish1, result.updatedLines[0].dish)
        assertEquals(2, result.issues.size)

        val priceIssue = result.issues.find { it.issueType == CartIssueType.PRICE_CHANGED }
        assertNotNull(priceIssue)
        assertEquals(20000L, priceIssue?.oldPricePaise)
        assertEquals(22000L, priceIssue?.newPricePaise)

        val removedIssue = result.issues.find { it.issueType == CartIssueType.REMOVED }
        assertNotNull(removedIssue)
        assertEquals("2", removedIssue?.dishId)
    }

    @Test
    fun orderStatusProgression() {
        assertEquals("1", nextOrderStatus("0"))
        assertEquals("2", nextOrderStatus("1"))
        assertNull(nextOrderStatus("2"))

        assertEquals("Placed", orderStatusLabel("0"))
        assertEquals("On its way", orderStatusLabel("1"))
        assertEquals("Delivered", orderStatusLabel("2"))
        assertEquals("Unknown", orderStatusLabel("99"))
    }
}
