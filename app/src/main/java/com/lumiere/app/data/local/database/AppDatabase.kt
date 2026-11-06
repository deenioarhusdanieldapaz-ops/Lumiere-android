package com.lumiere.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lumiere.app.data.local.dao.TaskDao
import com.lumiere.app.data.local.entity.Project
import com.lumiere.app.data.local.entity.Subtask
import com.lumiere.app.data.local.entity.Task

@Database(entities = [Task::class, Project::class, Subtask::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

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
                .fallbackToDestructiveMigration() // Apaga dados antigos porque mudámos o esquema
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
