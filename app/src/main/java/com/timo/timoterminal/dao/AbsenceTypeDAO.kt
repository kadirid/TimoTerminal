package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timo.timoterminal.entityClasses.AbsenceTypeEntity

@Dao
interface AbsenceTypeDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = AbsenceTypeEntity::class)
    suspend fun insertAll(entities: List<AbsenceTypeEntity>)

    @Query("Select * From AbsenceTypeEntity Where absence_type_id = :id")
    suspend fun getById(id: Int): AbsenceTypeEntity?

    @Query("SELECT * FROM AbsenceTypeEntity")
    suspend fun getAll(): List<AbsenceTypeEntity>

    @Query("SELECT * FROM AbsenceTypeEntity WHERE absence_type_marked_as_favorite = 1")
    suspend fun getAllFavorites(): List<AbsenceTypeEntity>

    @Query("UPDATE AbsenceTypeEntity SET absence_type_marked_as_favorite = 1 WHERE absence_type_id = :id")
    suspend fun markAsFavorite(id: Long)

    @Query("UPDATE AbsenceTypeEntity SET absence_type_marked_as_favorite = 0 WHERE absence_type_id = :id")
    suspend fun unmarkAsFavorite(id: Long)
}