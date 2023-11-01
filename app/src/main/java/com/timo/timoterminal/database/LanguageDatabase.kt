package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.LanguageDAO
import com.timo.timoterminal.entityClasses.LanguageEntity

@Database(entities = [LanguageEntity::class], version = 1)
abstract class LanguageDatabase  : RoomDatabase() {
    abstract fun languageDao(): LanguageDAO
}