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
}