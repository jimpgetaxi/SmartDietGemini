package com.jimpg.smartdiet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val nickname: String,
    val weight: Float, // kg
    val height: Float, // cm
    val age: Int,
    val gender: String, // "Male" or "Female"
    val activityLevel: Float, // Multiplier (e.g., 1.2, 1.375, 1.55)
    val bmi: Float,
    val calorieTarget: Int, // Calculated Daily Target
    val healthConditions: String
)
