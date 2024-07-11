package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.UserEntity

@Dao
interface UserDAO {

    @Query("SELECT * FROM UserEntity")
    suspend fun getAllAsList(): List<UserEntity>

    @Query("SELECT * FROM UserEntity WHERE id = :id")
    suspend fun loadEntityById(id: Long): List<UserEntity>

    @Query("SELECT * FROM UserEntity WHERE card like :card")
    suspend fun loadEntityByCard(card: String): List<UserEntity>

    @Query("SELECT * FROM UserEntity WHERE login like :login")
    suspend fun loadEntityByLogin(login: String): List<UserEntity>

    @Query("SELECT * FROM UserEntity WHERE pin like :pin")
    suspend fun loadEntityByPIN(pin: String): List<UserEntity>

    @Query("SELECT count(*) FROM UserEntity")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOne(entity: UserEntity)

    @Delete
    suspend fun delete(userEntity: UserEntity)

    @Query("DELETE FROM UserEntity")
    suspend fun deleteAll()

    @Update
    suspend fun updateEntity(userEntities: List<UserEntity>): Int

    @Query("SELECT * FROM UserEntity LIMIT :pageNo, 50 ")
    suspend fun getPageAsList(pageNo: Int): List<UserEntity>
}