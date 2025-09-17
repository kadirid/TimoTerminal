package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.CustomerDAO
import com.timo.timoterminal.entityClasses.CustomerEntity

@Database(entities = [CustomerEntity::class], version = 1, exportSchema = false)
abstract class CustomerDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDAO
}