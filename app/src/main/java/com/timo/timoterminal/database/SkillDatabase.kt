package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.SkillDAO
import com.timo.timoterminal.entityClasses.SkillEntity

@Database(entities = [SkillEntity::class], version = 1, exportSchema = false)
abstract class SkillDatabase : RoomDatabase() {
    abstract fun skillDao(): SkillDAO
}