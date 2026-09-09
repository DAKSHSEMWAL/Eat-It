package com.daksh.eatit.search

import com.daksh.eatit.Category
import com.daksh.eatit.Dish

object DeterministicSearchEngine {

    fun search(query: String, dishes: List<Dish>, categories: List<Category>): List<Dish> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return dishes

        val terms = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }

        return dishes.mapNotNull { dish ->
            val score = computeRelevanceScore(dish, categories, terms, trimmed)
            if (score > 0) dish to score else null
        }.sortedByDescending { it.second }.map { it.first }
    }

    private fun computeRelevanceScore(
        dish: Dish,
        categories: List<Category>,
        terms: List<String>,
        fullQuery: String
    ): Int {
        var score = 0
        val dishNameLower = dish.name.lowercase()
        val categoryNameLower = categories.find { it.id == dish.categoryId }?.name?.lowercase().orEmpty()
        val descriptionLower = dish.description.lowercase()

        if (dishNameLower == fullQuery) score += 50
        else if (dishNameLower.startsWith(fullQuery)) score += 30

        for (term in terms) {
            if (dishNameLower.contains(term)) score += 20
            if (categoryNameLower.contains(term)) score += 15
            if (descriptionLower.contains(term)) score += 5
        }

        return score
    }
}
