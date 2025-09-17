package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
class CustomerGroupEntity (
    @PrimaryKey @ColumnInfo(name = "customer_group_id") var customerGroupId: Long,
    @ColumnInfo(name = "customer_group_name") var customerGroupName: String
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as CustomerGroupEntity

        if (customerGroupId != other.customerGroupId) return false
        if (customerGroupName != other.customerGroupName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = customerGroupId.hashCode()
        result = 31 * result + customerGroupName.hashCode()
        return result
    }

    override fun toString(): String {
        return customerGroupName
    }

    companion object {
        fun parseFromJson(obj: JSONObject): List<CustomerGroupEntity> {
            val customerGroups = mutableListOf<CustomerGroupEntity>()
            val customerGroupArray = obj.getJSONArray("customerGroups")
            for (i in 0 until customerGroupArray.length()) {
                val customerGroupObj = customerGroupArray.getJSONObject(i)
                val customerGroupId = customerGroupObj.getLong("id")
                val customerGroupName = customerGroupObj.getString("name")
                customerGroups.add(CustomerGroupEntity(customerGroupId, customerGroupName))
            }
            return customerGroups
        }
    }
}