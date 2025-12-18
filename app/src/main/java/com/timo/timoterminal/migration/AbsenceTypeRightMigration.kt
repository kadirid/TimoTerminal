package com.timo.timoterminal.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class AbsenceTypeRightMigration {
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE AbsenceTypeRightEntity ADD COLUMN deputy_required INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}