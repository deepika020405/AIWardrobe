package com.example.aiwardrobe.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clothing_items")
data class ClothingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imageUri: String,
    val category: String,
    val color: String,
    val pattern: String,
    val style: String,
    val recommendation: String,
    val timestamp: Long = System.currentTimeMillis()
)
