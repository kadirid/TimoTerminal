package com.timo.timoterminal.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class AbsenceTypeMigration {

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE AbsenceTypeEntity ADD COLUMN absence_type_marked_as_favorite INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

}