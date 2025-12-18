package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.ProjectTimeDAO
import com.timo.timoterminal.entityClasses.ProjectTimeEntity

@Database(entities = [ProjectTimeEntity::class], version = 3)
abstract class ProjectTimeDatabase : RoomDatabase() {
    abstract fun projectTimeDao(): ProjectTimeDAO
}