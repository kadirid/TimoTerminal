package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.ConfigEntity
import com.timo.timoterminal.repositories.ConfigRepository
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDAO {

    @Query("Select * from ConfigEntity")
    fun getAll(): Flow<List<ConfigEntity>>

    @Query("SELECT * FROM ConfigEntity")
    suspend fun getAllAsList() : List<ConfigEntity>

    @Query("SELECT * FROM ConfigEntity WHERE type = ${ConfigRepository.TYPE_COMPANY} LIMIT 1")
    suspend fun getCompany() : ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities : List<ConfigEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOne(entity : ConfigEntity)

    @Delete
    suspend fun delete(entity: ConfigEntity)

    @Update
    suspend fun updateEntities(entities: List<ConfigEntity>) : Int

    @Update
    suspend fun updateEntity(entity: ConfigEntity) : Int

    @Query("DELETE FROM ConfigEntity")
    suspend fun deleteAll()

    @Query("SELECT COUNT(id) FROM ConfigEntity")
    suspend fun getDataCount(): Int
}