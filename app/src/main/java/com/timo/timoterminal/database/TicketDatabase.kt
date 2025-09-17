package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timo.timoterminal.entityClasses.TicketEntity

@Database(entities = [TicketEntity::class], version = 1, exportSchema = false)
abstract class TicketDatabase : RoomDatabase() {
    abstract fun ticketDao(): com.timo.timoterminal.dao.TicketDAO
}