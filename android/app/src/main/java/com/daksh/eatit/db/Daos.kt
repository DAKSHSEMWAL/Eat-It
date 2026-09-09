package com.daksh.eatit.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM dishes ORDER BY name ASC")
    fun getDishes(): Flow<List<DishEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDishes(dishes: List<DishEntity>)

    @Query("DELETE FROM categories")
    suspend fun clearCategories()

    @Query("DELETE FROM dishes")
    suspend fun clearDishes()

    @Query("SELECT lastSyncedTimestamp FROM catalog_sync_meta WHERE `key` = 'meta'")
    fun getSyncMeta(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSyncMeta(meta: CatalogSyncMetaEntity)

    @Transaction
    suspend fun replaceCatalog(categories: List<CategoryEntity>, dishes: List<DishEntity>, syncTimestamp: Long) {
        clearCategories()
        clearDishes()
        insertCategories(categories)
        insertDishes(dishes)
        setSyncMeta(CatalogSyncMetaEntity("meta", syncTimestamp))
    }
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_lines WHERE userId = :userId")
    fun getCartLines(userId: String): Flow<List<CartLineEntity>>

    @Query("SELECT * FROM cart_lines WHERE userId = :userId")
    suspend fun getCartLinesSync(userId: String): List<CartLineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartLine(line: CartLineEntity)

    @Query("DELETE FROM cart_lines WHERE userId = :userId AND dishId = :dishId")
    suspend fun deleteCartLine(userId: String, dishId: String)

    @Query("DELETE FROM cart_lines WHERE userId = :userId")
    suspend fun clearCart(userId: String)

    @Transaction
    suspend fun replaceCart(userId: String, lines: List<CartLineEntity>) {
        clearCart(userId)
        lines.forEach { insertCartLine(it) }
    }
}

@Dao
interface FavoriteDao {
    @Query("SELECT dishId FROM favorites WHERE userId = :userId")
    fun getFavoriteDishIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND dishId = :dishId")
    suspend fun removeFavorite(userId: String, dishId: String)
}

@Dao
interface SavedAddressDao {
    @Query("SELECT * FROM saved_addresses WHERE userId = :userId")
    fun getSavedAddresses(userId: String): Flow<List<SavedAddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: SavedAddressEntity)

    @Query("DELETE FROM saved_addresses WHERE id = :id AND userId = :userId")
    suspend fun deleteAddress(id: String, userId: String)
}
