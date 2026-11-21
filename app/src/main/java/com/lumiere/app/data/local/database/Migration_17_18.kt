package com.lumiere.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `core_memory` (
                `key` TEXT NOT NULL PRIMARY KEY,
                `value` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `sensitivity` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `expiresAt` INTEGER
            )
        """)
    }
}
