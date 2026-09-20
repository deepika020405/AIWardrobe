package com.example.aiwardrobe.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ClothingEntity)

    @Query("SELECT * FROM clothing_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<ClothingEntity>>

    @Delete
    suspend fun deleteItem(item: ClothingEntity)
}
