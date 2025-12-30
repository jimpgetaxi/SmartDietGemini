package com.jimpg.smartdiet.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MealAnalysisResponse(
    val description: String,
    val calories: Int,
    val protein: Float, // grams
    val carbs: Float,   // grams
    val fat: Float,     // grams
    val analysis: String // Short analysis/tips
)
