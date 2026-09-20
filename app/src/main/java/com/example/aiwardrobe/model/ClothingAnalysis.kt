package com.example.aiwardrobe.model

data class ClothingAnalysis(
    val category: String,
    val color: String,
    val pattern: String,
    val style: String,
    val recommendation: String
)