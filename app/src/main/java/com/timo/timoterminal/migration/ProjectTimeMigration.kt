package com.timo.timoterminal.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class ProjectTimeMigration {
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ProjectTimeEntity ADD COLUMN timeEntryType TEXT NOT NULL DEFAULT '3'")
            }
        }
    }
}