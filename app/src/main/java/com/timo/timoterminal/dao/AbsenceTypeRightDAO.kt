package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timo.timoterminal.entityClasses.AbsenceTypeRightEntity

@Dao
interface AbsenceTypeRightDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = AbsenceTypeRightEntity::class)
    suspend fun insertAll(entities: List<AbsenceTypeRightEntity>)

    @Query("DELETE FROM AbsenceTypeRightEntity")
    suspend fun deleteAll()

    @Query("SELECT * FROM AbsenceTypeRightEntity")
    suspend fun getAll(): List<AbsenceTypeRightEntity>

    @Query("SELECT * FROM AbsenceTypeRightEntity WHERE absence_type_id = :absenceTypeId AND user_id = :userId")
    suspend fun getByAbsenceTypeIdAndUserId(absenceTypeId: Int, userId: Int): AbsenceTypeRightEntity?
}