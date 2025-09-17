package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.CustomerGroupDAO
import com.timo.timoterminal.entityClasses.CustomerGroupEntity

@Database( entities = [CustomerGroupEntity::class], version = 1, exportSchema = false)
abstract class CustomerGroupDatabase : RoomDatabase() {
    abstract fun customerGroupDao(): CustomerGroupDAO
}