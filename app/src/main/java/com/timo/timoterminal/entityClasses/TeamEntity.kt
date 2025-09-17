package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class TeamEntity (
    @PrimaryKey @ColumnInfo(name = "team_id") var teamId: Long,
    @ColumnInfo(name = "team_name") var teamName: String
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as TeamEntity

        if (teamId != other.teamId) return false
        if (teamName != other.teamName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = teamId.hashCode()
        result = 31 * result + teamName.hashCode()
        return result
    }

    override fun toString(): String {
        return teamName
    }

    companion object {
        fun parseFromJson(obj: org.json.JSONObject): List<TeamEntity> {
            val teams = mutableListOf<TeamEntity>()
            val teamArray = obj.getJSONArray("teams")
            for (i in 0 until teamArray.length()) {
                val teamObj = teamArray.getJSONObject(i)
                val teamId = teamObj.getLong("id")
                val teamName = teamObj.getString("name")
                teams.add(TeamEntity(teamId, teamName))
            }
            return teams
        }
    }
}