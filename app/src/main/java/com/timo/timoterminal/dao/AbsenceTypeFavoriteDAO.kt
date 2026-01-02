package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timo.timoterminal.entityClasses.AbsenceTypeFavoriteEntity

@Dao
interface AbsenceTypeFavoriteDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = AbsenceTypeFavoriteEntity::class)
    suspend fun insert(favorite: AbsenceTypeFavoriteEntity)

    @Query("DELETE FROM AbsenceTypeFavoriteEntity WHERE user_id = :userId AND absence_type_id = :absenceTypeId")
    suspend fun deleteForUser(userId: Long, absenceTypeId: Long)

    @Query("SELECT absence_type_id FROM AbsenceTypeFavoriteEntity WHERE user_id = :userId")
    suspend fun getFavoritesForUser(userId: Long): List<Long>

    @Query("SELECT COUNT(*) FROM AbsenceTypeFavoriteEntity WHERE user_id = :userId AND absence_type_id = :absenceTypeId")
    suspend fun isFavorite(userId: Long, absenceTypeId: Long): Int
}

