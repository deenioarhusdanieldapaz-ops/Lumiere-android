package com.lumiere.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração 16 → 17 — saneamento antes de ativar FK.
 * Estratégia não-destrutiva:
 *  - Filhos órfãos (check-ins, exceptions, subtasks, history) → DELETE.
 *  - Referências órfãs em tasks (projectId, blockedByTaskId) → SET NULL.
 * A FK é ativada pelo onConfigure da AppDatabase v17.
 */
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Filhos órfãos — apagar
        db.execSQL("DELETE FROM habit_checkins WHERE habitId NOT IN (SELECT id FROM habits)")
        db.execSQL("DELETE FROM habit_exceptions WHERE habitId NOT IN (SELECT id FROM habits)")
        db.execSQL("DELETE FROM subtasks WHERE taskId NOT IN (SELECT id FROM tasks)")
        db.execSQL("DELETE FROM task_history WHERE taskId NOT IN (SELECT id FROM tasks)")
        // Referências órfãs em tasks — anular, não apagar
        db.execSQL("UPDATE tasks SET projectId = NULL WHERE projectId IS NOT NULL AND projectId NOT IN (SELECT id FROM projects)")
        db.execSQL("UPDATE tasks SET blockedByTaskId = NULL WHERE blockedByTaskId IS NOT NULL AND blockedByTaskId NOT IN (SELECT id FROM tasks)")
    }
}
