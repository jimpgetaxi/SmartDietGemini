package com.jimpg.smartdiet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val timestamp: Long,
    val imagePath: String? = null,
    
    // Analysis Results
    val calories: Int? = null,
    val protein: Float? = null, // in grams
    val carbs: Float? = null,   // in grams
    val fat: Float? = null,     // in grams
    val rawAnalysis: String? = null // Full text response from Gemini
)
