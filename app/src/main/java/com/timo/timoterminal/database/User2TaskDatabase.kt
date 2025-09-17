package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.User2TaskDAO
import com.timo.timoterminal.entityClasses.User2TaskEntity

@Database(entities = [User2TaskEntity::class], version = 1, exportSchema = false)
abstract class User2TaskDatabase : RoomDatabase() {
    abstract fun user2TaskDao(): User2TaskDAO
}