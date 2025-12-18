package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
class AbsenceTypeMatrixEntity (
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "absence_type_id") val absenceTypeId: Int,
    @ColumnInfo(name = "hours") val hours: String,
    @ColumnInfo(name = "days") val days: String
) {

    override fun equals(other: Any?): Boolean {
        if(other == null) return false
        if(this === other) return true
        if(javaClass != other.javaClass) return false

        other as AbsenceTypeMatrixEntity

        if(id != other.id) return false
        if(name != other.name) return false
        if(absenceTypeId != other.absenceTypeId) return false
        if(hours != other.hours) return false
        if(days != other.days) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + absenceTypeId.hashCode()
        result = 31 * result + hours.hashCode()
        result = 31 * result + days.hashCode()
        return result
    }

    override fun toString(): String {
        return name
    }

    companion object {
        fun parseFromJson(obj: JSONObject): List<AbsenceTypeMatrixEntity> {
            val absenceTypeMatrices = mutableListOf<AbsenceTypeMatrixEntity>()
            val absenceTypeMatrixArray = obj.getJSONArray("absenceTypeMatrices")
            for (i in 0 until absenceTypeMatrixArray.length()) {
                val absenceTypeMatrixObj = absenceTypeMatrixArray.getJSONObject(i)
                val id = absenceTypeMatrixObj.getInt("id")
                val name = absenceTypeMatrixObj.getString("name")
                val absenceTypeId = absenceTypeMatrixObj.getInt("absenceTypeId")
                val hours = absenceTypeMatrixObj.getString("hours")
                val days = absenceTypeMatrixObj.getString("days")
                absenceTypeMatrices.add(
                    AbsenceTypeMatrixEntity(
                        id,
                        name,
                        absenceTypeId,
                        hours,
                        days
                    )
                )
            }
            return absenceTypeMatrices
        }
    }
}