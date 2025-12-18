package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity
class AbsenceDeputyEntity(
    @PrimaryKey @ColumnInfo(name="user_id") val userId: Long,
    @ColumnInfo(name="deputies") val deputies: JSONArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbsenceDeputyEntity) return false

        if (userId != other.userId) return false
        if (deputies.toString() != other.deputies.toString()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + deputies.toString().hashCode()
        return result
    }

    override fun toString(): String {
        return "AbsenceDeputyEntity(userId=$userId, deputies=$deputies)"
    }

    companion object {
        fun parseFromJson(json: JSONObject): List<AbsenceDeputyEntity> {
            val entities = mutableListOf<AbsenceDeputyEntity>()
            val array = json.getJSONArray("absenceDeputies")
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val userId = obj.getLong("userId")
                val deputies = obj.getJSONArray("deputies")
                val entity = AbsenceDeputyEntity(userId, deputies)
                entities.add(entity)
            }
            return entities
        }
    }
}