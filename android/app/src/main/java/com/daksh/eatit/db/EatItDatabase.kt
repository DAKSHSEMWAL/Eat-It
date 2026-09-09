package com.daksh.eatit.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CategoryEntity::class, DishEntity::class, CartLineEntity::class, CatalogSyncMetaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EatItDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: EatItDatabase? = null

        fun getInstance(context: Context): EatItDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EatItDatabase::class.java,
                    "eatit_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
