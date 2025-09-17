package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
class User2TaskEntity (
    @PrimaryKey @ColumnInfo(name = "user2task_id") var id: Long,
    @ColumnInfo(name = "user2task_user_id") var userId: Long,
    @ColumnInfo(name = "user2task_task_id") var taskId: Long,
    @ColumnInfo(name = "user2task_project_id") var projectId: Long,
    @ColumnInfo(name = "user2task_activityType") var activityType: Long?
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as User2TaskEntity

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (taskId != other.taskId) return false
        if (projectId != other.projectId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + taskId.hashCode()
        result = 31 * result + projectId.hashCode()
        return result
    }

    override fun toString(): String {
        return "User2TaskEntity(id=$id, userId=$userId, taskId=$taskId, projectId=$projectId)"
    }

    companion object {
        fun parseFromJson(obj: JSONObject): List<User2TaskEntity> {
            val user2Tasks = mutableListOf<User2TaskEntity>()
            val user2TaskArray = obj.getJSONArray("user2Task")
            for (i in 0 until user2TaskArray.length()) {
                val user2TaskObj = user2TaskArray.getJSONObject(i)
                val id = user2TaskObj.getLong("id")
                val userId = user2TaskObj.getLong("userId")
                val taskId = user2TaskObj.getLong("taskId")
                val projectId = user2TaskObj.getLong("projectId")
                val activityType = user2TaskObj.optLong("activityType")
                user2Tasks.add(User2TaskEntity(id, userId, taskId, projectId, activityType))
            }
            return user2Tasks
        }
    }
}