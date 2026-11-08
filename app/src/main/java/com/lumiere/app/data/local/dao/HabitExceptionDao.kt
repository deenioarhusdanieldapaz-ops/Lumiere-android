package com.lumiere.app.data.local.dao

import androidx.room.*
import com.lumiere.app.data.local.entity.HabitException
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitExceptionDao {
    @Query("SELECT * FROM habit_exceptions WHERE habitId = :habitId ORDER BY startDate DESC")
    fun getExceptionsForHabit(habitId: Long): Flow<List<HabitException>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertException(exception: HabitException): Long
}
