package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.BookingBUDAO
import com.timo.timoterminal.entityClasses.BookingBUEntity

@Database(entities = [BookingBUEntity::class], version = 1)
abstract class BookingBUDatabase : RoomDatabase() {
    abstract fun bookingBUDao(): BookingBUDAO
}