package com.jimpg.smartdiet.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jimpg.smartdiet.data.local.entity.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getMealById(id: Long): MealEntity?
    
    @Query("SELECT SUM(calories) FROM meals WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getCaloriesForPeriod(startTime: Long, endTime: Long): Flow<Int?>

    @Query("SELECT * FROM meals ORDER BY timestamp DESC LIMIT 20")
    suspend fun getLast20Meals(): List<MealEntity>
}
