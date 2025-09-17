package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.ActivityTypeMatrixEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityTypeMatrixDAO {
    @Query("SELECT * FROM ActivityTypeMatrixEntity")
    fun getAllActivityTypeMatricesFlow(): Flow<List<ActivityTypeMatrixEntity>>

    @Query("SELECT * FROM ActivityTypeMatrixEntity")
    suspend fun getAllActivityTypeMatrices(): List<ActivityTypeMatrixEntity>

    @Query("SELECT * FROM ActivityTypeMatrixEntity WHERE activity_type_matrix_id = :id")
    suspend fun getActivityTypeMatrixById(id: Long): ActivityTypeMatrixEntity?

    @Query("SELECT COUNT(*) FROM ActivityTypeMatrixEntity")
    suspend fun countActivityTypeMatrices(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityTypeMatrix(activityTypeMatrix: ActivityTypeMatrixEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllActivityTypeMatrices(activityTypeMatrices: List<ActivityTypeMatrixEntity>)

    @Update
    suspend fun updateActivityTypeMatrix(activityTypeMatrix: ActivityTypeMatrixEntity)

    @Delete
    suspend fun deleteActivityTypeMatrix(activityTypeMatrix: ActivityTypeMatrixEntity)

    @Query("DELETE FROM ActivityTypeMatrixEntity")
    suspend fun deleteAllActivityTypeMatrices()
}