package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.DemoDAO
import com.timo.timoterminal.entityClasses.DemoEntity

@Database(entities = [DemoEntity::class], version = 1)
abstract class DemoDatabase  : RoomDatabase (){
    abstract fun demoDao() : DemoDAO

    //The database is initalized in the appModule.kt

}