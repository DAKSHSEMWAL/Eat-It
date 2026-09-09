package com.daksh.eatit

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

enum class UserRole { CUSTOMER, STAFF }

data class Category(val id: String, val name: String)
data class Dish(val id: String, val name: String, val categoryId: String, val pricePaise: Long, val image: String = "", val description: String = "")
data class CartLine(val dish: Dish, val quantity: Int) {
    init { require(quantity in 1..99); require(dish.pricePaise >= 0) }
    val totalPaise: Long get() = Math.multiplyExact(dish.pricePaise, quantity.toLong())
}
data class Customer(val id: String, val name: String, val email: String, val role: UserRole = UserRole.CUSTOMER)
data class Delivery(val name: String = "", val phone: String = "", val address: String = "") {
    fun valid() = name.trim().length >= 2 && phone.matches(Regex("\\+?[0-9]{10,15}")) && address.trim().length >= 10
}
data class Purchase(val id: String, val totalPaise: Long, val status: String, val address: String)
data class Catalog(
    val categories: List<Category>,
    val dishes: List<Dish>,
    val lastSyncedTimestamp: Long = 0L,
    val isOffline: Boolean = false
)

enum class CartIssueType { PRICE_CHANGED, REMOVED }

data class CartIssue(
    val dishId: String,
    val dishName: String,
    val issueType: CartIssueType,
    val oldPricePaise: Long = 0,
    val newPricePaise: Long = 0
)

data class CartReconciliation(
    val updatedLines: List<CartLine>,
    val issues: List<CartIssue>
)

class PriceMismatchException(val updatedTotalPaise: Long) : Exception("Menu prices have updated. Please review your cart before retrying.")
class ItemUnavailableException(val itemId: String) : Exception("An item in your cart is no longer available.")

fun nextOrderStatus(currentStatus: String): String? = when (currentStatus) {
    "0" -> "1"
    "1" -> "2"
    else -> null
}

fun orderStatusLabel(status: String): String = when (status) {
    "0" -> "Placed"
    "1" -> "On its way"
    "2" -> "Delivered"
    else -> "Unknown"
}

fun money(paise: Long): String = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(BigDecimal.valueOf(paise, 2))
fun parsePrice(value: String): Long? = try {
    BigDecimal(value).setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact().takeIf { it in 0..100_000_000 }
} catch (_: Exception) { null }
fun cartTotal(lines: List<CartLine>): Long = lines.fold(0L) { total, line -> Math.addExact(total, line.totalPaise) }
fun updateCart(lines: List<CartLine>, dish: Dish, quantity: Int): List<CartLine> {
    require(quantity in 0..99)
    return lines.filterNot { it.dish.id == dish.id } + if (quantity == 0) emptyList() else listOf(CartLine(dish, quantity))
}

fun reconcileCart(lines: List<CartLine>, catalog: Catalog): CartReconciliation {
    if (lines.isEmpty() || catalog.dishes.isEmpty()) return CartReconciliation(lines, emptyList())
    val updatedLines = mutableListOf<CartLine>()
    val issues = mutableListOf<CartIssue>()

    for (line in lines) {
        val canonicalDish = catalog.dishes.find { it.id == line.dish.id }
        if (canonicalDish == null) {
            issues.add(CartIssue(line.dish.id, line.dish.name, CartIssueType.REMOVED, oldPricePaise = line.dish.pricePaise))
        } else {
            if (canonicalDish.pricePaise != line.dish.pricePaise) {
                issues.add(
                    CartIssue(
                        dishId = line.dish.id,
                        dishName = canonicalDish.name,
                        issueType = CartIssueType.PRICE_CHANGED,
                        oldPricePaise = line.dish.pricePaise,
                        newPricePaise = canonicalDish.pricePaise
                    )
                )
            }
            updatedLines.add(CartLine(canonicalDish, line.quantity))
        }
    }

    return CartReconciliation(updatedLines, issues)
}

val DemoCatalog = Catalog(
    listOf(Category("bowls", "Bowls"), Category("pizza", "Pizza"), Category("burgers", "Burgers"), Category("sides", "Sides")),
    listOf(
        Dish("1", "Green harvest bowl", "bowls", 24900, description = "Roasted vegetables, grains and a bright herb dressing. A little freshness in every bite."),
        Dish("2", "Classic margherita", "pizza", 32900, description = "Tomato, mozzarella and fragrant basil on a crisp, golden base."),
        Dish("3", "Crispy chicken burger", "burgers", 26900, description = "Crispy chicken, crunchy slaw and house sauce in a toasted bun."),
        Dish("4", "Smoky paneer bowl", "bowls", 27900, description = "Grilled paneer, seasoned rice and charred vegetables with a smoky finish."),
        Dish("5", "Rosemary wedges", "sides", 14900, description = "Golden potato wedges tossed with rosemary and sea salt."),
        Dish("6", "Garden pizza", "pizza", 35900, description = "Peppers, mushrooms and olives with tomato sauce and melted mozzarella."),
    ),
)
