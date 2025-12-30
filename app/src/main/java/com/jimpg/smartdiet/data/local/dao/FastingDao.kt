package com.jimpg.smartdiet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jimpg.smartdiet.data.local.entity.FastingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FastingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun startFast(session: FastingSessionEntity)

    @Update
    suspend fun updateFast(session: FastingSessionEntity)

    // Get the most recent session (active or finished)
    @Query("SELECT * FROM fasting_sessions ORDER BY startTime DESC LIMIT 1")
    fun getCurrentFast(): Flow<FastingSessionEntity?>
    
    @Query("SELECT * FROM fasting_sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getCurrentFastSync(): FastingSessionEntity?
}
