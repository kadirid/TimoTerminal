package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timo.timoterminal.entityClasses.BookingBUEntity
import com.timo.timoterminal.entityClasses.BookingEntity

@Dao
interface BookingBUDAO {

    @Query("SELECT * FROM BookingBUEntity ORDER BY id")
    suspend fun getAllAsList(): List<BookingBUEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE, entity = BookingBUEntity::class)
    suspend fun insertAll(entities: List<BookingEntity>)

    @Query("UPDATE BookingBUEntity SET isSend = :isSend WHERE id = :id")
    suspend fun setIsSend(id: Long, isSend: Boolean): Int

    @Query("DELETE FROM BookingBUEntity WHERE isSend = :isSend AND DATETIME(createdTime) < DATETIME('now','start of day', '-1 month','-1 day')")
    suspend fun deleteOldBUBookings(isSend: Boolean = true)
}