package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.Customer2ProjectDAO
import com.timo.timoterminal.entityClasses.Customer2ProjectEntity

@Database(entities = [Customer2ProjectEntity::class], version = 1, exportSchema = false)
abstract class Customer2ProjectDatabase : RoomDatabase() {
    abstract fun customer2ProjectDao(): Customer2ProjectDAO
}