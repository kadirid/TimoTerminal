package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.AbsenceTypeFavoriteDAO
import com.timo.timoterminal.entityClasses.AbsenceTypeFavoriteEntity

@Database(entities = [AbsenceTypeFavoriteEntity::class], version = 1)
abstract class AbsenceTypeFavoriteDatabase : RoomDatabase() {
    abstract fun absenceTypeFavoriteDao(): AbsenceTypeFavoriteDAO
}

