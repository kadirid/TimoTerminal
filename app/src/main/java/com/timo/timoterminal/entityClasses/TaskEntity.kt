package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
class TaskEntity(
    @PrimaryKey @ColumnInfo(name = "task_id") var taskId: Long,
    @ColumnInfo(name = "task_name") var taskName: String,
    @ColumnInfo(name = "task_project_id") var projectId: Long,
    @ColumnInfo(name = "task_activity_type") var activityType: Long,
    @ColumnInfo(name = "task_activity_type_must") var activityTypeMust: Boolean,
    @ColumnInfo(name = "task_description_must") var descriptionMust: Boolean,
    @ColumnInfo(name = "task_abschaetzung_must") var abschaetzungMust: Boolean,
    @ColumnInfo(name = "task_abrechenbar") var abrechenbar: Boolean,
    @ColumnInfo(name = "task_abrechenbar_disabled") var abrechenbarDisabled: Boolean,
    @ColumnInfo(name = "task_abrechenbar_pbaum") var abrechenbarPBaum: Long,
    @ColumnInfo(name = "task_order_no_must") var orderNoMust: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as TaskEntity

        if (taskId != other.taskId) return false
        if (taskName != other.taskName) return false
        if (projectId != other.projectId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = taskId.hashCode()
        result = 31 * result + taskName.hashCode()
        result = 31 * result + projectId.hashCode()
        return result
    }

    override fun toString(): String {
        return taskName
    }

    companion object {
        fun parseFromJson(obj: JSONObject): List<TaskEntity> {
            val tasks = mutableListOf<TaskEntity>()
            val taskArray = obj.getJSONArray("tasks")
            for (i in 0 until taskArray.length()) {
                val taskObj = taskArray.getJSONObject(i)
                val taskId = taskObj.getLong("id")
                val taskName = taskObj.getString("name")
                val projectId = taskObj.getLong("projectId")
                val activityType = taskObj.getLong("activityType")
                val activityTypeMust = taskObj.getBoolean("activityTypeMust")
                val descriptionMust = taskObj.getBoolean("descriptionMust")
                val abschaetzungMust = taskObj.getBoolean("abschaetzungMust")
                val abrechenbar = taskObj.getBoolean("abrechenbar")
                val abrechenbarDisabled = taskObj.getBoolean("abrechenbarDisabled")
                val abrechenbarPBaum = taskObj.getLong("abrechenbarPBaum")
                val orderNoMust = taskObj.getBoolean("orderNoMust")
                tasks.add(
                    TaskEntity(
                        taskId,
                        taskName,
                        projectId,
                        activityType,
                        activityTypeMust,
                        descriptionMust,
                        abschaetzungMust,
                        abrechenbar,
                        abrechenbarDisabled,
                        abrechenbarPBaum,
                        orderNoMust
                    )
                )
            }
            return tasks
        }
    }
}