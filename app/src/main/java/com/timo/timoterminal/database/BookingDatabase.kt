package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.dao.BookingDAO
import com.timo.timoterminal.entityClasses.BookingEntity

@Database(entities = [BookingEntity::class], version = 1)
abstract class BookingDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDAO
}