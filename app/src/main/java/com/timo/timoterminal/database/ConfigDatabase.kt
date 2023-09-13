package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.ConfigDAO
import com.timo.timoterminal.entityClasses.ConfigEntity

@Database(entities = [ConfigEntity::class], version = 1)
abstract class ConfigDatabase : RoomDatabase() {
    abstract fun configDao(): ConfigDAO
}