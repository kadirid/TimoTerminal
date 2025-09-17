package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.TeamDAO
import com.timo.timoterminal.entityClasses.TeamEntity

class TeamRepository(private val teamDAO: TeamDAO) {

    fun getAllTeamsFlow() = teamDAO.getAllTeamsFlow()

    suspend fun getAllTeams() = teamDAO.getAllTeams()

    suspend fun getTeamById(id: Long) = teamDAO.getTeamById(id)

    suspend fun countTeams() = teamDAO.countTeams()

    @WorkerThread
    suspend fun insertTeam(team: TeamEntity): Long {
        return teamDAO.insertTeam(team)
    }

    @WorkerThread
    suspend fun insertAllTeams(teams: List<TeamEntity>) {
        teamDAO.insertAllTeams(teams)
    }

    suspend fun updateTeam(team: TeamEntity) {
        teamDAO.updateTeam(team)
    }

    suspend fun deleteTeam(team: TeamEntity) {
        teamDAO.deleteTeam(team)
    }

    suspend fun deleteAllTeams() {
        teamDAO.deleteAllTeams()
    }
}