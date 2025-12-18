package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timo.timoterminal.entityClasses.AbsenceDeputyEntity

@Dao
interface AbsenceDeputyDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = AbsenceDeputyEntity::class)
    suspend fun insertAll(entities: List<AbsenceDeputyEntity>)

    @Query("SELECT * FROM AbsenceDeputyEntity WHERE user_id = :userId")
    suspend fun getByUserId(userId: Int): List<AbsenceDeputyEntity>

    @Query("SELECT * FROM AbsenceDeputyEntity")
    suspend fun getAll(): List<AbsenceDeputyEntity>
}