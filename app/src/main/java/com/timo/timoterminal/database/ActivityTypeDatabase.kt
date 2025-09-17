package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.ActivityTypeDAO
import com.timo.timoterminal.entityClasses.ActivityTypeEntity

@Database(entities = [ActivityTypeEntity::class], version = 1, exportSchema = false)
abstract class ActivityTypeDatabase : RoomDatabase() {
    abstract fun activityTypeDao(): ActivityTypeDAO
}