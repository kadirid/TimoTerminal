package com.timo.timoterminal.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class UserMigration {
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE UserEntity ADD COLUMN customerBasedProjectTime INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE UserEntity ADD COLUMN timeEntryType INTEGER NOT NULL DEFAULT -1")
                database.execSQL("ALTER TABLE UserEntity ADD COLUMN crossDay INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}