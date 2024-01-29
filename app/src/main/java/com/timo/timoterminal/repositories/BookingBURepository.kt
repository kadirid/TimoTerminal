package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.BookingBUDAO
import com.timo.timoterminal.entityClasses.BookingBUEntity
import com.timo.timoterminal.entityClasses.BookingEntity
import kotlinx.coroutines.flow.Flow

class BookingBURepository(private val bookingBUDAO: BookingBUDAO) {

    @WorkerThread
    suspend fun insertBookingEntities(entities: List<BookingEntity>){
        bookingBUDAO.insertAll(entities)
    }

    fun getAllEntities(): Flow<List<BookingBUEntity>> = bookingBUDAO.getAll()

    suspend fun getAllAsList(): List<BookingBUEntity> = bookingBUDAO.getAllAsList()

    suspend fun setIsSend(id: Long) : Int = bookingBUDAO.setIsSend(id, true)

    suspend fun deleteOldBUBookings() = bookingBUDAO.deleteOldBUBookings()

    suspend fun deleteAll() = bookingBUDAO.deleteAll()
}