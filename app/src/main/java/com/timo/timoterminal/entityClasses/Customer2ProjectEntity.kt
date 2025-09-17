package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
class Customer2ProjectEntity(
    @PrimaryKey @ColumnInfo(name = "customer2project_id") var id: Long,
    @ColumnInfo(name = "customer_id") var customerId: Long,
    @ColumnInfo(name = "project_id") var projectId: Long,
    @ColumnInfo(name = "group_id") var groupId: Long
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as Customer2ProjectEntity

        if (id != other.id) return false
        if (customerId != other.customerId) return false
        if (projectId != other.projectId) return false
        if (groupId != other.groupId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + customerId.hashCode()
        result = 31 * result + projectId.hashCode()
        result = 31 * result + groupId.hashCode()
        return result
    }

    override fun toString(): String {
        return "Customer2ProjectEntity(id=$id, customerId=$customerId, projectId=$projectId, groupId=$groupId)"
    }

    companion object {
        fun parseFromJson(obj: JSONObject): List<Customer2ProjectEntity> {
            val customer2Projects = mutableListOf<Customer2ProjectEntity>()
            val customer2ProjectArray = obj.getJSONArray("customer2Project")
            for (i in 0 until customer2ProjectArray.length()) {
                val c2pObj = customer2ProjectArray.getJSONObject(i)
                val id = c2pObj.getLong("id")
                val customerId = c2pObj.getLong("customerId")
                val projectId = c2pObj.getLong("projectId")
                val groupId = c2pObj.getLong("groupId")
                customer2Projects.add(Customer2ProjectEntity(id, customerId, projectId, groupId))
            }
            return customer2Projects
        }
    }
}