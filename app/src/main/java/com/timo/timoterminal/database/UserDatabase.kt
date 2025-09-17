package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.UserDAO
import com.timo.timoterminal.entityClasses.UserEntity

@Database(entities = [UserEntity::class], version = 2)
abstract class UserDatabase  : RoomDatabase() {
    abstract fun userDao(): UserDAO
}