package com.daksh.eatit

import com.daksh.eatit.search.DeterministicSearchEngine
import org.junit.Assert.*
import org.junit.Test

class SearchEvaluationTest {

    private val catalog = DemoCatalog

    @Test
    fun testPizzaQueryRelevance() {
        val results = DeterministicSearchEngine.search("pizza", catalog.dishes, catalog.categories)
        assertEquals(2, results.size)
        assertTrue(results.any { it.name == "Classic margherita" })
        assertTrue(results.any { it.name == "Garden pizza" })
    }

    @Test
    fun testBowlQueryRelevance() {
        val results = DeterministicSearchEngine.search("bowl", catalog.dishes, catalog.categories)
        assertEquals(2, results.size)
        assertTrue(results.all { it.name.contains("bowl", ignoreCase = true) })
    }

    @Test
    fun testBurgerExactRelevance() {
        val results = DeterministicSearchEngine.search("burger", catalog.dishes, catalog.categories)
        assertEquals(1, results.size)
        assertEquals("Crispy chicken burger", results[0].name)
    }

    @Test
    fun testDescriptionKeywordMatching() {
        val results = DeterministicSearchEngine.search("vegetables", catalog.dishes, catalog.categories)
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.name == "Green harvest bowl" })
    }

    @Test
    fun testEmptyQueryReturnsAllDishes() {
        val results = DeterministicSearchEngine.search("", catalog.dishes, catalog.categories)
        assertEquals(catalog.dishes.size, results.size)
    }
}
