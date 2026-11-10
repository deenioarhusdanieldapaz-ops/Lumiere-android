package com.lumiere.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE habits ADD COLUMN timerAccumulatedSeconds INTEGER NOT NULL DEFAULT 0")
        // Migrar minutos antigos para segundos (se existirem)
        db.execSQL("UPDATE habits SET timerAccumulatedSeconds = timerAccumulatedMinutes * 60 WHERE timerAccumulatedMinutes > 0")
    }
}
