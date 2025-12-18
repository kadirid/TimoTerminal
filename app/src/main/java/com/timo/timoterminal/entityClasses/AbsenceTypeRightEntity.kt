package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class AbsenceTypeRightEntity(
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "absence_type_id") val absenceTypeId: Long,
    @ColumnInfo(name = "deputy_required", defaultValue = "0") var deputyRequired: Boolean = false
) {
    @PrimaryKey(autoGenerate = true)
    var id : Long? = null

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as AbsenceTypeRightEntity

        if (userId != other.userId) return false
        if (absenceTypeId != other.absenceTypeId) return false
        if (deputyRequired != other.deputyRequired) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + absenceTypeId.hashCode()
        result = 31 * result + deputyRequired.hashCode()
        return result
    }

    override fun toString(): String {
        return "AbsenceTypeRightEntity(userId=$userId, absenceTypeId=$absenceTypeId)"
    }

    companion object {
        fun parseFromJson(obj: org.json.JSONObject): List<AbsenceTypeRightEntity> {
            val absenceTypeRights = mutableListOf<AbsenceTypeRightEntity>()
            val rightsArray = obj.getJSONArray("absenceTypeRights")
            for (i in 0 until rightsArray.length()) {
                val rightObj = rightsArray.getJSONObject(i)
                val userId = rightObj.getLong("userId")
                val absenceTypeId = rightObj.getLong("absenceTypeId")
                val deputyRequired = rightObj.optBoolean("deputyRequired", false)
                absenceTypeRights.add(AbsenceTypeRightEntity(userId, absenceTypeId, deputyRequired))
            }
            return absenceTypeRights
        }
    }
}