package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.CustomerGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerGroupDAO {

    @Query("SELECT * FROM CustomerGroupEntity")
    fun getAllCustomerGroupsFlow(): Flow<List<CustomerGroupEntity>>

    @Query("SELECT * FROM CustomerGroupEntity")
    suspend fun getAllCustomerGroups(): List<CustomerGroupEntity>

    @Query("SELECT * FROM CustomerGroupEntity WHERE customer_group_id = :id")
    suspend fun getCustomerGroupById(id: Long): CustomerGroupEntity?

    @Query("SELECT COUNT(*) FROM CustomerGroupEntity")
    suspend fun countCustomerGroups(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerGroup(customerGroup: CustomerGroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCustomerGroups(customerGroups: List<CustomerGroupEntity>)

    @Update
    suspend fun updateCustomerGroup(customerGroup: CustomerGroupEntity)

    @Delete
    suspend fun deleteCustomerGroup(customerGroup: CustomerGroupEntity)

    @Query("DELETE FROM CustomerGroupEntity")
    suspend fun deleteAllCustomerGroups()
}