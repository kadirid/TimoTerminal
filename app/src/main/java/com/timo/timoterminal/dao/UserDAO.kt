package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDAO {

    @Query("SELECT * FROM UserEntity")
    fun getAll() : Flow<List<UserEntity>>

    @Query("SELECT * FROM UserEntity")
    fun getAllAsList() : Flow<List<UserEntity>>

    @Query("SELECT * FROM UserEntity WHERE id = :id")
    suspend fun loadEntityById(id: Long) : List<UserEntity>

    @Query("SELECT count(*) FROM UserEntity")
    suspend fun count() : Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg entities : UserEntity)

    @Insert
    suspend fun insertOne(entity: UserEntity)

    @Delete
    suspend fun delete(userEntity: UserEntity)

    @Update
    suspend fun updateEntity(userEntities: List<UserEntity>) : Int
}