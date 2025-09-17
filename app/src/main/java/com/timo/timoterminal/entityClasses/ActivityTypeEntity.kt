package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ActivityTypeEntity (
    @PrimaryKey @ColumnInfo(name = "activity_type_id") var activityTypeId: Long,
    @ColumnInfo(name = "activity_type_name") var activityTypeName: String
){
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as ActivityTypeEntity

        if (activityTypeId != other.activityTypeId) return false
        if (activityTypeName != other.activityTypeName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = activityTypeId.hashCode()
        result = 31 * result + activityTypeName.hashCode()
        return result
    }

    override fun toString(): String {
        return activityTypeName
    }

    companion object {
        fun parseFromJson(obj: org.json.JSONObject): List<ActivityTypeEntity> {
            val activityTypes = mutableListOf<ActivityTypeEntity>()
            val activityTypeArray = obj.getJSONArray("activityTypes")
            for (i in 0 until activityTypeArray.length()) {
                val activityTypeObj = activityTypeArray.getJSONObject(i)
                val activityTypeId = activityTypeObj.getLong("id")
                val activityTypeName = activityTypeObj.getString("name")
                activityTypes.add(ActivityTypeEntity(activityTypeId, activityTypeName))
            }
            return activityTypes
        }
    }
}