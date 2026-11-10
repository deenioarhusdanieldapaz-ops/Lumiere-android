package com.lumiere.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE habits ADD COLUMN timerStartedAt INTEGER")
        db.execSQL("ALTER TABLE habits ADD COLUMN timerAccumulatedMinutes INTEGER NOT NULL DEFAULT 0")
    }
}
