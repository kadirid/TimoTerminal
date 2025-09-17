package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Customer2TaskEntity(
    @PrimaryKey @ColumnInfo(name = "customer2task_id") var id: Long,
    @ColumnInfo(name = "customer_id") var customerId: Long,
    @ColumnInfo(name = "task_id") var taskId: Long,
    @ColumnInfo(name = "group_id") var groupId: Long) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as Customer2TaskEntity

        if (id != other.id) return false
        if (customerId != other.customerId) return false
        if (taskId != other.taskId) return false
        if (groupId != other.groupId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + customerId.hashCode()
        result = 31 * result + taskId.hashCode()
        result = 31 * result + groupId.hashCode()
        return result
    }

    override fun toString(): String {
        return "Customer2TaskEntity(id=$id, customerId=$customerId, taskId=$taskId, groupId=$groupId)"
    }

    companion object {
        fun parseFromJson(obj: org.json.JSONObject): List<Customer2TaskEntity> {
            val customer2Tasks = mutableListOf<Customer2TaskEntity>()
            val customer2TaskArray = obj.getJSONArray("customer2Task")
            for (i in 0 until customer2TaskArray.length()) {
                val c2tObj = customer2TaskArray.getJSONObject(i)
                val id = c2tObj.getLong("id")
                val customerId = c2tObj.getLong("customerId")
                val taskId = c2tObj.getLong("taskId")
                val groupId = c2tObj.getLong("groupId")
                customer2Tasks.add(Customer2TaskEntity(id, customerId, taskId, groupId))
            }
            return customer2Tasks
        }
    }
}