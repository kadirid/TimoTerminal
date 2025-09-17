package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.User2TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface User2TaskDAO {

    @Query("SELECT * FROM User2TaskEntity")
    fun getAllUser2TasksFlow(): Flow<List<User2TaskEntity>>

    @Query("SELECT * FROM User2TaskEntity")
    suspend fun getAllUser2TAsks(): List<User2TaskEntity>

    @Query("SELECT * FROM User2TaskEntity WHERE user2task_id = :id")
    suspend fun getUser2TaskById(id: Long): User2TaskEntity?

    @Query("SELECT COUNT(*) FROM User2TaskEntity")
    suspend fun countUser2Tasks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser2Task(user2Task: User2TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUser2Tasks(user2Tasks: List<User2TaskEntity>)

    @Update
    suspend fun updateUser2Task(user2Task: User2TaskEntity)

    @Delete
    suspend fun deleteUser2Task(user2Task: User2TaskEntity)

    @Query("DELETE FROM User2TaskEntity")
    suspend fun deleteAllUser2Tasks()
}