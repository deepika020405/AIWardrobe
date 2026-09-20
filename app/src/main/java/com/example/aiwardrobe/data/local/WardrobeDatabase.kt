package com.example.aiwardrobe.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ClothingEntity::class], version = 1, exportSchema = false)
abstract class WardrobeDatabase : RoomDatabase() {

    abstract fun clothingDao(): ClothingDao

    companion object {
        @Volatile
        private var INSTANCE: WardrobeDatabase? = null

        fun getDatabase(context: Context): WardrobeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WardrobeDatabase::class.java,
                    "wardrobe_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
