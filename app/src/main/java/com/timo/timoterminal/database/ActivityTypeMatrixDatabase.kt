package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.ActivityTypeMatrixDAO
import com.timo.timoterminal.entityClasses.ActivityTypeMatrixEntity

@Database(entities = [ActivityTypeMatrixEntity::class], version = 1, exportSchema = false)
abstract class ActivityTypeMatrixDatabase : RoomDatabase() {
    abstract fun activityTypeMatrixDao(): ActivityTypeMatrixDAO
}