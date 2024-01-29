package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.LanguageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguageDAO {

    @Query("Select * from LanguageEntity")
    fun getAll(): Flow<List<LanguageEntity>>

    @Query("SELECT * FROM LanguageEntity")
    suspend fun getAllAsList() : List<LanguageEntity>

    @Query("SELECT * FROM LanguageEntity where language = :lang")
    suspend fun getAllAsListByLanguage(lang: String) : List<LanguageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg entities : LanguageEntity)

    @Delete
    suspend fun delete(entity: LanguageEntity)

    @Query("DELETE FROM LanguageEntity")
    suspend fun deleteAll()

    @Update
    suspend fun updateEntity(entities: List<LanguageEntity>) : Int
}