package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.AbsenceTypeRightDAO
import com.timo.timoterminal.entityClasses.AbsenceTypeRightEntity

@Database(entities = [AbsenceTypeRightEntity::class], version = 2)
abstract class AbsenceTypeRightDatabase : RoomDatabase() {
    abstract fun absenceTypeRightDao(): AbsenceTypeRightDAO
}