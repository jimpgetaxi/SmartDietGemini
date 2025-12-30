package com.jimpg.smartdiet.domain.repository

import com.jimpg.smartdiet.data.local.entity.MealEntity
import com.jimpg.smartdiet.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    fun getAllMeals(): Flow<List<MealEntity>>
    suspend fun insertMeal(meal: MealEntity)
    suspend fun deleteMeal(meal: MealEntity)
    suspend fun getMealById(id: Long): MealEntity?
    fun getCaloriesForPeriod(startTime: Long, endTime: Long): Flow<Int?>
    suspend fun getLast20Meals(): List<MealEntity>
    
    // User Profile
    fun getUserProfile(): Flow<UserProfileEntity?>
    suspend fun getUserProfileSync(): UserProfileEntity?
    suspend fun saveUserProfile(profile: UserProfileEntity)
}
