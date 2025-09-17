package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDAO {

    @Query("SELECT * FROM TicketEntity")
    fun getAllTicketsFlow(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM TicketEntity")
    suspend fun getAllTickets(): List<TicketEntity>

    @Query("SELECT * FROM TicketEntity WHERE ticket_id = :id")
    suspend fun getTicketById(id: Long): TicketEntity?

    @Query("SELECT COUNT(*) FROM TicketEntity")
    suspend fun countTickets(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTickets(tickets: List<TicketEntity>)

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Delete
    suspend fun deleteTicket(ticket: TicketEntity)

    @Query("DELETE FROM TicketEntity")
    suspend fun deleteAllTickets()
}