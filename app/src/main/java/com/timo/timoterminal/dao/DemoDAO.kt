package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entities.DemoEntity
import kotlinx.coroutines.flow.Flow


/*
https://developer.android.com/training/data-storage/room/accessing-data
for further notice on how to use it
 */

@Dao
interface DemoDAO {

    @Query("SELECT * FROM DemoEntity")
    fun getAll() : Flow<List<DemoEntity>>

    @Query("SELECT * FROM DemoEntity WHERE id = :id")
    suspend fun loadEntityById(id: Long) : List<DemoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg entities : DemoEntity)

    @Delete
    suspend fun delete(demoEntity: DemoEntity)

    @Update
    suspend fun updateEntity(demoEntities: List<DemoEntity>) : Int

}