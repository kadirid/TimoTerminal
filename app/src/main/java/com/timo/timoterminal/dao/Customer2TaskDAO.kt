package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.Customer2TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface Customer2TaskDAO {

    @Query("SELECT * FROM Customer2TaskEntity")
    fun getAllCustomer2TasksFlow(): Flow<List<Customer2TaskEntity>>

    @Query("SELECT * FROM Customer2TaskEntity")
    suspend fun getAllCustomer2Tasks(): List<Customer2TaskEntity>

    @Query("SELECT * FROM Customer2TaskEntity WHERE customer2task_id = :id")
    suspend fun getCustomer2TaskById(id: Long): Customer2TaskEntity?

    @Query("SELECT COUNT(*) FROM Customer2TaskEntity")
    suspend fun countCustomer2Tasks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer2Task(customer2Task: Customer2TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCustomer2Tasks(customer2Tasks: List<Customer2TaskEntity>)

    @Update
    suspend fun updateCustomer2Task(customer2Task: Customer2TaskEntity)

    @Delete
    suspend fun deleteCustomer2Task(customer2Task: Customer2TaskEntity)

    @Query("DELETE FROM Customer2TaskEntity")
    suspend fun deleteAllCustomer2Tasks()
}