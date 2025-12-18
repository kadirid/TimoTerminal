package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timo.timoterminal.entityClasses.AbsenceTypeMatrixEntity

@Dao
interface AbsenceTypeMatrixDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = AbsenceTypeMatrixEntity::class)
    suspend fun insertAll(entities: List<AbsenceTypeMatrixEntity>)

    @Query("SELECT * FROM AbsenceTypeMatrixEntity WHERE absence_type_id = :absenceTypeId")
    suspend fun getByAbsenceTypeId(absenceTypeId: Int): List<AbsenceTypeMatrixEntity>

    @Query("SELECT * FROM AbsenceTypeMatrixEntity")
    suspend fun getAll(): List<AbsenceTypeMatrixEntity>
}