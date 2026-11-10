package com.lumiere.app.data.local.dao

import androidx.room.*
import com.lumiere.app.data.local.entity.HabitCheckIn
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCheckInDao {
    @Query("SELECT * FROM habit_checkins WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun getCheckInsForHabit(habitId: Long): Flow<List<HabitCheckIn>>

    @Query("SELECT * FROM habit_checkins ORDER BY timestamp DESC")
    fun getAllCheckIns(): Flow<List<HabitCheckIn>>

    @Query("SELECT * FROM habit_checkins WHERE habitId = :habitId AND timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    suspend fun getCheckInsForPeriod(habitId: Long, start: Long, end: Long): List<HabitCheckIn>

    @Query("SELECT * FROM habit_checkins WHERE habitId = :habitId AND timestamp BETWEEN :start AND :end ORDER BY timestamp DESC LIMIT 1")
    suspend fun getCheckInForDay(habitId: Long, start: Long, end: Long): HabitCheckIn?

    @Query("SELECT COUNT(*) FROM habit_checkins WHERE habitId = :habitId")
    suspend fun countForHabit(habitId: Long): Int

    @Query("DELETE FROM habit_checkins WHERE habitId = :habitId")
    suspend fun deleteForHabit(habitId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: HabitCheckIn): Long

    @Update
    suspend fun updateCheckIn(checkIn: HabitCheckIn)

    @Delete
    suspend fun deleteCheckIn(checkIn: HabitCheckIn)
}
