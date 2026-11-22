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

    @Query("SELECT COUNT(*) FROM habit_checkins WHERE habitId = :habitId AND type = 'SUCCESS' AND timestamp >= :since")
    suspend fun countSuccessSince(habitId: Long, since: Long): Int

    @Query("SELECT timestamp FROM habit_checkins WHERE habitId = :habitId AND type = 'SUCCESS' ORDER BY timestamp DESC")
    suspend fun successTimestampsForHabit(habitId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCheckIn(checkIn: HabitCheckIn): Long

    @Update
    suspend fun updateCheckIn(checkIn: HabitCheckIn)

    @Delete
    suspend fun deleteCheckIn(checkIn: HabitCheckIn)
}
