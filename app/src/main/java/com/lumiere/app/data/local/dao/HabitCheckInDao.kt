package com.lumiere.app.data.local.dao

import androidx.room.*
import com.lumiere.app.data.local.entity.HabitCheckIn
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCheckInDao {
    @Query("SELECT * FROM habit_checkins WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun getCheckInsForHabit(habitId: Long): Flow<List<HabitCheckIn>>

    @Query("SELECT * FROM habit_checkins WHERE habitId = :habitId AND timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    suspend fun getCheckInsForPeriod(habitId: Long, start: Long, end: Long): List<HabitCheckIn>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: HabitCheckIn): Long

    @Delete
    suspend fun deleteCheckIn(checkIn: HabitCheckIn)
}
