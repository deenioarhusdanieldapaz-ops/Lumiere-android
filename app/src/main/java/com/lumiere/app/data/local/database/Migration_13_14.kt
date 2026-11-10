package com.lumiere.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Limpar órfãos antes de aplicar FKs
        db.execSQL("DELETE FROM habit_checkins WHERE habitId NOT IN (SELECT id FROM habits)")
        db.execSQL("DELETE FROM habit_exceptions WHERE habitId NOT IN (SELECT id FROM habits)")
        db.execSQL("DELETE FROM subtasks WHERE taskId NOT IN (SELECT id FROM tasks)")
        db.execSQL("DELETE FROM task_history WHERE taskId NOT IN (SELECT id FROM tasks)")
        db.execSQL("UPDATE tasks SET projectId = NULL WHERE projectId IS NOT NULL AND projectId NOT IN (SELECT id FROM projects)")
        db.execSQL("UPDATE tasks SET blockedByTaskId = NULL WHERE blockedByTaskId IS NOT NULL AND blockedByTaskId NOT IN (SELECT id FROM tasks)")

        // habit_checkins
        db.execSQL("CREATE TABLE IF NOT EXISTS habit_checkins_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, habitId INTEGER NOT NULL, timestamp INTEGER NOT NULL, value INTEGER, durationMinutes INTEGER, observation TEXT, origin TEXT NOT NULL, type TEXT NOT NULL, FOREIGN KEY(habitId) REFERENCES habits(id) ON DELETE CASCADE)")
        db.execSQL("INSERT INTO habit_checkins_new SELECT id, habitId, timestamp, value, durationMinutes, observation, origin, type FROM habit_checkins")
        db.execSQL("DROP TABLE habit_checkins")
        db.execSQL("ALTER TABLE habit_checkins_new RENAME TO habit_checkins")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_checkins_habitId_timestamp ON habit_checkins (habitId, timestamp)")

        // habit_exceptions
        db.execSQL("CREATE TABLE IF NOT EXISTS habit_exceptions_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, habitId INTEGER NOT NULL, startDate INTEGER NOT NULL, endDate INTEGER NOT NULL, type TEXT NOT NULL, reason TEXT, FOREIGN KEY(habitId) REFERENCES habits(id) ON DELETE CASCADE)")
        db.execSQL("INSERT INTO habit_exceptions_new SELECT id, habitId, startDate, endDate, type, reason FROM habit_exceptions")
        db.execSQL("DROP TABLE habit_exceptions")
        db.execSQL("ALTER TABLE habit_exceptions_new RENAME TO habit_exceptions")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_exceptions_habitId ON habit_exceptions (habitId)")

        // subtasks
        db.execSQL("CREATE TABLE IF NOT EXISTS subtasks_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, taskId INTEGER NOT NULL, title TEXT NOT NULL, isCompleted INTEGER NOT NULL, FOREIGN KEY(taskId) REFERENCES tasks(id) ON DELETE CASCADE)")
        db.execSQL("INSERT INTO subtasks_new SELECT id, taskId, title, isCompleted FROM subtasks")
        db.execSQL("DROP TABLE subtasks")
        db.execSQL("ALTER TABLE subtasks_new RENAME TO subtasks")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_taskId ON subtasks (taskId)")

        // task_history
        db.execSQL("CREATE TABLE IF NOT EXISTS task_history_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, taskId INTEGER NOT NULL, action TEXT NOT NULL, timestamp INTEGER NOT NULL, FOREIGN KEY(taskId) REFERENCES tasks(id) ON DELETE CASCADE)")
        db.execSQL("INSERT INTO task_history_new SELECT id, taskId, action, timestamp FROM task_history")
        db.execSQL("DROP TABLE task_history")
        db.execSQL("ALTER TABLE task_history_new RENAME TO task_history")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_task_history_taskId ON task_history (taskId)")

        // tasks (2 FKs)
        db.execSQL("CREATE TABLE IF NOT EXISTS tasks_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, description TEXT, startDate INTEGER, dueDate INTEGER, estimatedDurationMinutes INTEGER, timeReservedMinutes INTEGER, actualDurationMinutes INTEGER NOT NULL, timerStartedAt INTEGER, urgency INTEGER NOT NULL, importance INTEGER NOT NULL, energy INTEGER NOT NULL, context TEXT, recurrenceRule TEXT, instructions TEXT, links TEXT, completionCriteria TEXT, cancellationReason TEXT, attachmentUris TEXT, status INTEGER NOT NULL, priority INTEGER NOT NULL, category TEXT, tags TEXT, projectId INTEGER, blockedByTaskId INTEGER, assignee TEXT, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, completedAt INTEGER, FOREIGN KEY(projectId) REFERENCES projects(id) ON DELETE SET NULL, FOREIGN KEY(blockedByTaskId) REFERENCES tasks(id) ON DELETE SET NULL)")
        db.execSQL("INSERT INTO tasks_new SELECT id, title, description, startDate, dueDate, estimatedDurationMinutes, timeReservedMinutes, actualDurationMinutes, timerStartedAt, urgency, importance, energy, context, recurrenceRule, instructions, links, completionCriteria, cancellationReason, attachmentUris, status, priority, category, tags, projectId, blockedByTaskId, assignee, createdAt, updatedAt, completedAt FROM tasks")
        db.execSQL("DROP TABLE tasks")
        db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_projectId ON tasks (projectId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_blockedByTaskId ON tasks (blockedByTaskId)")
    }
}
