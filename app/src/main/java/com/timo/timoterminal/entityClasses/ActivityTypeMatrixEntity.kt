package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ActivityTypeMatrixEntity(
    @PrimaryKey @ColumnInfo(name = "activity_type_matrix_id") var activityTypeMatrixId: Long,
    @ColumnInfo(name = "activity_type_matrix_name") var activityTypeMatrixName: String,
    @ColumnInfo(name = "activity_type_matrix_activity_type_id") var activityTypeMatrixActivityTypeId: Long
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as ActivityTypeMatrixEntity

        if (activityTypeMatrixId != other.activityTypeMatrixId) return false
        if (activityTypeMatrixName != other.activityTypeMatrixName) return false
        if (activityTypeMatrixActivityTypeId != other.activityTypeMatrixActivityTypeId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = activityTypeMatrixId.hashCode()
        result = 31 * result + activityTypeMatrixName.hashCode()
        result = 31 * result + activityTypeMatrixActivityTypeId.hashCode()
        return result
    }

    override fun toString(): String {
        return activityTypeMatrixName
    }

    companion object {
        fun parseFromJson(obj: org.json.JSONObject): List<ActivityTypeMatrixEntity> {
            val activityTypeMatrices = mutableListOf<ActivityTypeMatrixEntity>()
            val activityTypeMatrixArray = obj.getJSONArray("activityTypeMatrices")
            for (i in 0 until activityTypeMatrixArray.length()) {
                val activityTypeMatrixObj = activityTypeMatrixArray.getJSONObject(i)
                val activityTypeMatrixId = activityTypeMatrixObj.getLong("id")
                val activityTypeMatrixName = activityTypeMatrixObj.getString("name")
                val activityTypeMatrixActivityTypeId =
                    activityTypeMatrixObj.getLong("activityTypeId")
                activityTypeMatrices.add(
                    ActivityTypeMatrixEntity(
                        activityTypeMatrixId,
                        activityTypeMatrixName,
                        activityTypeMatrixActivityTypeId
                    )
                )
            }
            return activityTypeMatrices
        }
    }
}