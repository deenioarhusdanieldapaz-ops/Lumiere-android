package com.lumiere.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lumiere.app.data.local.dao.*
import com.lumiere.app.data.local.entity.*
import com.lumiere.app.core.memory.CoreMemory

@Database(
    entities = [Task::class, Project::class, Subtask::class, TaskHistory::class, Habit::class, HabitCheckIn::class, HabitException::class, CoreMemory::class],
    version = 19,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCheckInDao(): HabitCheckInDao
    abstract fun habitExceptionDao(): HabitExceptionDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun taskHistoryDao(): TaskHistoryDao
    abstract fun coreMemoryDao(): com.lumiere.app.core.memory.CoreMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lumiere_database"
                )
                    .addMigrations(MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL("PRAGMA foreign_keys = ON")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
