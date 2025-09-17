package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.Customer2TaskDAO
import com.timo.timoterminal.entityClasses.Customer2TaskEntity

@Database(entities = [Customer2TaskEntity::class], version = 1, exportSchema = false)
abstract class Customer2TaskDatabase : RoomDatabase() {
    abstract fun customer2TaskDao(): Customer2TaskDAO
}