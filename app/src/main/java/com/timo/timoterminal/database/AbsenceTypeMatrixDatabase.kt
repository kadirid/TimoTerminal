package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.AbsenceTypeMatrixDAO
import com.timo.timoterminal.entityClasses.AbsenceTypeMatrixEntity

@Database(entities = [AbsenceTypeMatrixEntity::class], version = 1)
abstract class AbsenceTypeMatrixDatabase : RoomDatabase() {
    abstract fun absenceTypeMatrixDao(): AbsenceTypeMatrixDAO
}