package com.jimpg.smartdiet.data.repository

import com.jimpg.smartdiet.data.local.dao.MealDao
import com.jimpg.smartdiet.data.local.dao.UserProfileDao
import com.jimpg.smartdiet.data.local.entity.MealEntity
import com.jimpg.smartdiet.data.local.entity.UserProfileEntity
import com.jimpg.smartdiet.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow

class MealRepositoryImpl(
    private val mealDao: MealDao,
    private val userProfileDao: UserProfileDao
) : MealRepository {
    override fun getAllMeals(): Flow<List<MealEntity>> = mealDao.getAllMeals()

    override suspend fun insertMeal(meal: MealEntity) {
        mealDao.insertMeal(meal)
    }

    override suspend fun deleteMeal(meal: MealEntity) {
        mealDao.deleteMeal(meal)
    }

    override suspend fun getMealById(id: Long): MealEntity? {
        return mealDao.getMealById(id)
    }

    override fun getCaloriesForPeriod(startTime: Long, endTime: Long): Flow<Int?> {
        return mealDao.getCaloriesForPeriod(startTime, endTime)
    }

    override suspend fun getLast20Meals(): List<MealEntity> {
        return mealDao.getLast20Meals()
    }

    override fun getUserProfile(): Flow<UserProfileEntity?> {
        return userProfileDao.getUserProfile()
    }

    override suspend fun getUserProfileSync(): UserProfileEntity? {
        return userProfileDao.getUserProfileSync()
    }

    override suspend fun saveUserProfile(profile: UserProfileEntity) {
        userProfileDao.insertOrUpdateProfile(profile)
    }
}
