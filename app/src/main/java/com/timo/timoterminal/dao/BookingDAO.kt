package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDAO {

    @Query("SELECT * FROM BookingEntity")
    fun getAll(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM BookingEntity ORDER BY id")
    suspend fun getAllAsList(): List<BookingEntity>

    @Query("SELECT count(*) FROM BookingEntity")
    suspend fun count() : Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities : List<BookingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity : BookingEntity)

    @Delete
    suspend fun delete(bookingEntity: BookingEntity)

    @Query("DELETE FROM BookingEntity")
    suspend fun deleteAll()

    @Update
    suspend fun updateEntity(bookingEntity: BookingEntity)
}