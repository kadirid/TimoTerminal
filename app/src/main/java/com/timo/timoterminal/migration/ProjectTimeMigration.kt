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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ProjectTimeEntity ADD COLUMN isVisible INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE ProjectTimeEntity ADD COLUMN wtId INTEGER DEFAULT NULL")
                database.execSQL("ALTER TABLE ProjectTimeEntity ADD COLUMN editorId TEXT NOT NULL DEFAULT '-1'")
                database.execSQL("ALTER TABLE ProjectTimeEntity ADD COLUMN message TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}