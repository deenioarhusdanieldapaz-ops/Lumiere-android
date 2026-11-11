package com.lumiere.app.data.repository

import com.lumiere.app.data.local.dao.*
import com.lumiere.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import com.lumiere.app.utils.LumiereLogger

class HabitRepository(
    private val habitDao: HabitDao,
    private val checkInDao: HabitCheckInDao,
    private val exceptionDao: HabitExceptionDao
) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllActiveHabits()

    suspend fun insertHabit(habit: Habit): Long = try { habitDao.insertHabit(habit) } catch (e: Exception) { LumiereLogger.e("HabitRepo", "Falha ao inserir habito", e); -1L }
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)
    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)

    fun getCheckIns(habitId: Long): Flow<List<HabitCheckIn>> = checkInDao.getCheckInsForHabit(habitId)
    val allCheckIns: Flow<List<HabitCheckIn>> = checkInDao.getAllCheckIns()
    suspend fun insertCheckIn(checkIn: HabitCheckIn): Long = try { checkInDao.insertCheckIn(checkIn) } catch (e: Exception) { LumiereLogger.e("HabitRepo", "Falha ao inserir check-in", e); -1L }
    suspend fun updateCheckIn(checkIn: HabitCheckIn) = checkInDao.updateCheckIn(checkIn)
    suspend fun deleteCheckIn(checkIn: HabitCheckIn) = checkInDao.deleteCheckIn(checkIn)
    suspend fun getCheckInsForPeriod(habitId: Long, start: Long, end: Long): List<HabitCheckIn> = checkInDao.getCheckInsForPeriod(habitId, start, end)
    suspend fun getCheckInForDay(habitId: Long, start: Long, end: Long): HabitCheckIn? = checkInDao.getCheckInForDay(habitId, start, end)
    suspend fun countCheckIns(habitId: Long): Int = checkInDao.countForHabit(habitId)
    suspend fun deleteCheckInsForHabit(habitId: Long) = checkInDao.deleteForHabit(habitId)

    fun getExceptions(habitId: Long): Flow<List<HabitException>> = exceptionDao.getExceptionsForHabit(habitId)
    suspend fun insertException(exception: HabitException): Long = exceptionDao.insertException(exception)
}
