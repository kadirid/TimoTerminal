package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.BookingDAO
import com.timo.timoterminal.entityClasses.BookingEntity

class BookingRepository(private val bookingDAO: BookingDAO) {

    @WorkerThread
    suspend fun insertBookingEntities(entities: List<BookingEntity>){
        bookingDAO.insertAll(entities)
    }

    @WorkerThread
    suspend fun insertBookingEntity(entity: BookingEntity){
        bookingDAO.insert(entity)
    }

    fun getAll() = bookingDAO.getAll()

    suspend fun count() = bookingDAO.count()

    suspend fun getAllAsList(): List<BookingEntity> = bookingDAO.getAllAsList()

    suspend fun delete(entity: BookingEntity) = bookingDAO.delete(entity)

    suspend fun deleteAll() = bookingDAO.deleteAll()
}