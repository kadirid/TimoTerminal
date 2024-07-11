package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.BookingBUDAO
import com.timo.timoterminal.entityClasses.BookingBUEntity
import com.timo.timoterminal.entityClasses.BookingEntity

class BookingBURepository(private val bookingBUDAO: BookingBUDAO) {

    @WorkerThread
    suspend fun insertBookingEntity(entity: BookingEntity) {
        bookingBUDAO.insert(entity)
    }

    suspend fun getAllAsList(): List<BookingBUEntity> = bookingBUDAO.getAllAsList()

    suspend fun setIsSend(id: Long): Int = bookingBUDAO.setIsSend(id, true)

    suspend fun deleteOldBUBookings() = bookingBUDAO.deleteOldBUBookings()

    suspend fun deleteAll() = bookingBUDAO.deleteAll()

    suspend fun getPageAsList(currentPage: Int): List<BookingBUEntity> =
        bookingBUDAO.getPageAsList(currentPage * 50)
}