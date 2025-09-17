package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDAO {

    @Query("SELECT * FROM CustomerEntity")
    fun getAllCustomersFlow(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM CustomerEntity")
    suspend fun getAllCustomers(): List<CustomerEntity>

    @Query("SELECT * FROM CustomerEntity WHERE customer_id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT COUNT(*) FROM CustomerEntity")
    suspend fun countCustomers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM CustomerEntity")
    suspend fun deleteAllCustomers()
}