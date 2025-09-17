package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.JourneyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyDAO {
    @Query("SELECT * FROM JourneyEntity")
    fun getAllJourneysFlow(): Flow<List<JourneyEntity>>

    @Query("SELECT * FROM JourneyEntity")
    suspend fun getAllJourneys(): List<JourneyEntity>

    @Query("SELECT * FROM JourneyEntity WHERE journey_id = :id")
    suspend fun getJourneyById(id: Long): JourneyEntity?

    @Query("SELECT * FROM JourneyEntity WHERE journey_start_date >= :startDate AND journey_end_date <= :endDate AND journey_user_id = :userId")
    suspend fun getJourneysByDateRange(
        startDate: Long,
        endDate: Long,
        userId: Int
    ): List<JourneyEntity>

    @Query("SELECT COUNT(*) FROM JourneyEntity")
    suspend fun countJourneys(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: JourneyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllJourneys(journeys: List<JourneyEntity>)

    @Update
    suspend fun updateJourney(journey: JourneyEntity)

    @Delete
    suspend fun deleteJourney(journey: JourneyEntity)

    @Query("DELETE FROM JourneyEntity")
    suspend fun deleteAllJourneys()
}