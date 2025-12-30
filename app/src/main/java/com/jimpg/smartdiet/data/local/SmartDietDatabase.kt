package com.jimpg.smartdiet.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jimpg.smartdiet.data.local.dao.FastingDao
import com.jimpg.smartdiet.data.local.dao.MealDao
import com.jimpg.smartdiet.data.local.dao.UserProfileDao
import com.jimpg.smartdiet.data.local.entity.FastingSessionEntity
import com.jimpg.smartdiet.data.local.entity.MealEntity
import com.jimpg.smartdiet.data.local.entity.UserProfileEntity

@Database(entities = [MealEntity::class, UserProfileEntity::class, FastingSessionEntity::class], version = 3, exportSchema = false)
abstract class SmartDietDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun fastingDao(): FastingDao
}
