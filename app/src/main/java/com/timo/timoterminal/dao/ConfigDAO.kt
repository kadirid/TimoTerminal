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

    @Query("SELECT * FROM ConfigEntity WHERE type = ${ConfigRepository.TYPE_URL} LIMIT 1")
    suspend fun getUrl() : ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg entities : ConfigEntity)

    @Delete
    suspend fun delete(entity: ConfigEntity)

    @Update
    suspend fun updateEntity(entities: List<ConfigEntity>) : Int
}