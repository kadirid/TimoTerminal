package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.ActivityTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityTypeDAO {

    @Query("SELECT * FROM ActivityTypeEntity")
    fun getAllActivityTypesFlow(): Flow<List<ActivityTypeEntity>>

    @Query("SELECT * FROM ActivityTypeEntity")
    suspend fun getAllActivityTypes(): List<ActivityTypeEntity>

    @Query("SELECT * FROM ActivityTypeEntity WHERE activity_type_id = :id")
    suspend fun getActivityTypeById(id: Long): ActivityTypeEntity?

    @Query("SELECT COUNT(*) FROM ActivityTypeEntity")
    suspend fun countActivityTypes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityType(activityType: ActivityTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllActivityTypes(activityTypes: List<ActivityTypeEntity>)

    @Update
    suspend fun updateActivityType(activityType: ActivityTypeEntity)

    @Delete
    suspend fun deleteActivityType(activityType: ActivityTypeEntity)

    @Query("DELETE FROM ActivityTypeEntity")
    suspend fun deleteAllActivityTypes()
}