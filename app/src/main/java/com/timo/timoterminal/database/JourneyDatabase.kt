package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.JourneyDAO
import com.timo.timoterminal.entityClasses.JourneyEntity

@Database(entities = [JourneyEntity::class], version = 1, exportSchema = false)
abstract class JourneyDatabase : RoomDatabase(){
    abstract fun journeyDao(): JourneyDAO
}