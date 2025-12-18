package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.AbsenceEntryDAO
import com.timo.timoterminal.entityClasses.AbsenceEntryEntity

@Database(entities = [AbsenceEntryEntity::class], version = 1)
abstract class AbsenceEntryDatabase : RoomDatabase() {
    abstract fun absenceEntryDao(): AbsenceEntryDAO
}