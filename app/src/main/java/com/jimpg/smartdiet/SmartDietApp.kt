package com.jimpg.smartdiet

import android.app.Application
import androidx.room.Room
import com.jimpg.smartdiet.data.local.SmartDietDatabase

class SmartDietApp : Application() {
    lateinit var database: SmartDietDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            SmartDietDatabase::class.java,
            "smart_diet_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
