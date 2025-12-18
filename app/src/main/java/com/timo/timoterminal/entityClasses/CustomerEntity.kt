package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
class CustomerEntity (
    @PrimaryKey @ColumnInfo(name = "customer_id") var customerId: Long,
    @ColumnInfo(name = "customer_name") var customerName: String,
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as CustomerEntity

        if (customerId != other.customerId) return false
        if (customerName != other.customerName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = customerId.hashCode()
        result = 31 * result + customerName.hashCode()
        return result
    }

    override fun toString(): String {
        return "CustomerEntity(customerId=$customerId, customerName='$customerName')"
    }

    companion object {
        fun parseFromJson(obj: JSONObject): List<CustomerEntity> {
            val customers = mutableListOf<CustomerEntity>()
            val customerArray = obj.getJSONArray("customers")
            for (i in 0 until customerArray.length()) {
                val customerObj = customerArray.getJSONObject(i)
                val customerId = customerObj.getLong("id")
                val customerName = customerObj.getString("name")
                customers.add(CustomerEntity(customerId, customerName))
            }
            return customers
        }
    }
}