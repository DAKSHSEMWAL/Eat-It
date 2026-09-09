package com.daksh.eatit

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

data class Category(val id: String, val name: String)
data class Dish(val id: String, val name: String, val categoryId: String, val pricePaise: Long, val image: String = "", val description: String = "")
data class CartLine(val dish: Dish, val quantity: Int) {
    init { require(quantity in 1..99); require(dish.pricePaise >= 0) }
    val totalPaise: Long get() = Math.multiplyExact(dish.pricePaise, quantity.toLong())
}
data class Customer(val id: String, val name: String, val email: String)
data class Delivery(val name: String = "", val phone: String = "", val address: String = "") {
    fun valid() = name.trim().length >= 2 && phone.matches(Regex("\\+?[0-9]{10,15}")) && address.trim().length >= 10
}
data class Purchase(val id: String, val totalPaise: Long, val status: String, val address: String)
data class Catalog(val categories: List<Category>, val dishes: List<Dish>)
fun money(paise: Long): String = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(BigDecimal.valueOf(paise, 2))
fun parsePrice(value: String): Long? = try {
    BigDecimal(value).setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact().takeIf { it in 0..100_000_000 }
} catch (_: Exception) { null }
fun cartTotal(lines: List<CartLine>): Long = lines.fold(0L) { total, line -> Math.addExact(total, line.totalPaise) }
fun updateCart(lines: List<CartLine>, dish: Dish, quantity: Int): List<CartLine> {
    require(quantity in 0..99)
    return lines.filterNot { it.dish.id == dish.id } + if (quantity == 0) emptyList() else listOf(CartLine(dish, quantity))
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
