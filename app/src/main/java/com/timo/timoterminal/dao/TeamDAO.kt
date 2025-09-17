package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.TeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDAO {

    @Query("SELECT * FROM TeamEntity")
    fun getAllTeamsFlow(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM TeamEntity")
    suspend fun getAllTeams(): List<TeamEntity>

    @Query("SELECT * FROM TeamEntity WHERE team_id = :id")
    suspend fun getTeamById(id: Long): TeamEntity?

    @Query("SELECT COUNT(*) FROM TeamEntity")
    suspend fun countTeams(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTeams(teams: List<TeamEntity>)

    @Update
    suspend fun updateTeam(team: TeamEntity)

    @Delete
    suspend fun deleteTeam(team: TeamEntity)

    @Query("DELETE FROM TeamEntity")
    suspend fun deleteAllTeams()
}