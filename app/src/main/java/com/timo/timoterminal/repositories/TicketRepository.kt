package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.TicketDAO
import com.timo.timoterminal.entityClasses.TicketEntity

class TicketRepository(private val ticketDAO: TicketDAO) {

    fun getAllTicketsFlow() = ticketDAO.getAllTicketsFlow()

    suspend fun getAllTickets() = ticketDAO.getAllTickets()

    suspend fun getTicketById(id: Long) = ticketDAO.getTicketById(id)

    suspend fun countTickets() = ticketDAO.countTickets()

    @WorkerThread
    suspend fun insertTicket(ticket: TicketEntity): Long {
        return ticketDAO.insertTicket(ticket)
    }

    @WorkerThread
    suspend fun insertAllTickets(tickets: List<TicketEntity>) {
        ticketDAO.insertAllTickets(tickets)
    }

    suspend fun updateTicket(ticket: TicketEntity) {
        ticketDAO.updateTicket(ticket)
    }

    suspend fun deleteTicket(ticket: TicketEntity) {
        ticketDAO.deleteTicket(ticket)
    }

    suspend fun deleteAllTickets() {
        ticketDAO.deleteAllTickets()
    }
}