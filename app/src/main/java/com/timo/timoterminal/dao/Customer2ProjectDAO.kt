package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.Customer2ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface Customer2ProjectDAO {

    @Query("SELECT * FROM Customer2ProjectEntity")
    fun getAllCustomer2ProjectsFlow(): Flow<List<Customer2ProjectEntity>>

    @Query("SELECT * FROM Customer2ProjectEntity")
    suspend fun getAllCustomer2Projects(): List<Customer2ProjectEntity>

    @Query("SELECT * FROM Customer2ProjectEntity WHERE customer2project_id = :id")
    suspend fun getCustomer2ProjectById(id: Long): Customer2ProjectEntity?

    @Query("SELECT COUNT(*) FROM Customer2ProjectEntity")
    suspend fun countCustomer2Projects(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer2Project(customer2Project: Customer2ProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCustomer2Projects(customer2Projects: List<Customer2ProjectEntity>)

    @Update
    suspend fun updateCustomer2Project(customer2Project: Customer2ProjectEntity)

    @Delete
    suspend fun deleteCustomer2Project(customer2Project: Customer2ProjectEntity)

    @Query("DELETE FROM Customer2ProjectEntity")
    suspend fun deleteAllCustomer2Projects()
}