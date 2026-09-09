package com.daksh.eatit.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.daksh.eatit.Category
import com.daksh.eatit.Dish
import com.daksh.eatit.SavedAddress

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String
) {
    fun toModel() = Category(id = id, name = name)
}

fun Category.toEntity() = CategoryEntity(id = id, name = name)

@Entity(tableName = "dishes")
data class DishEntity(
    @PrimaryKey val id: String,
    val name: String,
    val categoryId: String,
    val pricePaise: Long,
    val image: String = "",
    val description: String = ""
) {
    fun toModel() = Dish(
        id = id,
        name = name,
        categoryId = categoryId,
        pricePaise = pricePaise,
        image = image,
        description = description
    )
}

fun Dish.toEntity() = DishEntity(
    id = id,
    name = name,
    categoryId = categoryId,
    pricePaise = pricePaise,
    image = image,
    description = description
)

@Entity(tableName = "cart_lines", primaryKeys = ["userId", "dishId"])
data class CartLineEntity(
    val userId: String,
    val dishId: String,
    val quantity: Int
)

@Entity(tableName = "catalog_sync_meta")
data class CatalogSyncMetaEntity(
    @PrimaryKey val key: String = "meta",
    val lastSyncedTimestamp: Long = 0L
)

@Entity(tableName = "favorites", primaryKeys = ["userId", "dishId"])
data class FavoriteEntity(
    val userId: String,
    val dishId: String
)

@Entity(tableName = "saved_addresses")
data class SavedAddressEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val label: String,
    val name: String,
    val phone: String,
    val address: String,
    val isDefault: Boolean
) {
    fun toModel() = SavedAddress(
        id = id,
        userId = userId,
        label = label,
        name = name,
        phone = phone,
        address = address,
        isDefault = isDefault
    )
}

fun SavedAddress.toEntity() = SavedAddressEntity(
    id = id,
    userId = userId,
    label = label,
    name = name,
    phone = phone,
    address = address,
    isDefault = isDefault
)
