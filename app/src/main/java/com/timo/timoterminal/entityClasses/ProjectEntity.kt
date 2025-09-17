package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
class ProjectEntity (
    @PrimaryKey @ColumnInfo(name = "project_id") var projectId: Long,
    @ColumnInfo(name = "project_name") var projectName: String
) {

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as ProjectEntity

        if (projectId != other.projectId) return false
        if (projectName != other.projectName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = projectId.hashCode()
        result = 31 * result + projectName.hashCode()
        return result
    }

    override fun toString(): String {
        return projectName
    }

    companion object {
        fun parseFromJson(obj: JSONObject): List<ProjectEntity> {
            val projects = mutableListOf<ProjectEntity>()
            val projectArray = obj.getJSONArray("projects")
            for (i in 0 until projectArray.length()) {
                val projectObj = projectArray.getJSONObject(i)
                val projectId = projectObj.getLong("id")
                val projectName = projectObj.getString("name")
                projects.add(ProjectEntity(projectId, projectName))
            }
            return projects
        }
    }
}