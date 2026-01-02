package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.AbsenceTypeDAO
import com.timo.timoterminal.entityClasses.AbsenceTypeEntity

@Database(entities = [AbsenceTypeEntity::class], version = 1)
abstract class AbsenceTypeDatabase : RoomDatabase() {
    abstract fun absenceTypeDao(): AbsenceTypeDAO
}