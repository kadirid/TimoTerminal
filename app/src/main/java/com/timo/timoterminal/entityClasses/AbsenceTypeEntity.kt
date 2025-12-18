package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
class AbsenceTypeEntity (
    @PrimaryKey @ColumnInfo(name = "absence_type_id") var id: Long,
    @ColumnInfo(name = "absence_type_name") var name: String,
    @ColumnInfo(name = "absence_type_color") var color: String,
    @ColumnInfo(name = "absence_type_subject_to_approval") var subjectToApproval: Boolean,
    @ColumnInfo(name = "absence_type_hours") var hours: Boolean,
    @ColumnInfo(name = "absence_type_from_to") var fromTo: Boolean,
    @ColumnInfo(name = "absence_type_start_stop") var startStop: Boolean
) {

    override fun equals(other: Any?): Boolean {
        if(other == null) return false
        if(this === other) return true
        if(javaClass != other.javaClass) return false

        other as AbsenceTypeEntity

        if(id != other.id) return false
        if(name != other.name) return false
        if(color != other.color) return false
        if(subjectToApproval != other.subjectToApproval) return false
        if(hours != other.hours) return false
        if(fromTo != other.fromTo) return false
        if(startStop != other.startStop) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + subjectToApproval.hashCode()
        result = 31 * result + hours.hashCode()
        result = 31 * result + fromTo.hashCode()
        result = 31 * result + startStop.hashCode()
        return result
    }

    override fun toString(): String {
        return name
    }

    companion object {
        fun parseFromJson(obj: JSONObject): List<AbsenceTypeEntity> {
            val absenceTypes = mutableListOf<AbsenceTypeEntity>()
            val absenceTypeArray = obj.getJSONArray("absenceTypes")
            for (i in 0 until absenceTypeArray.length()) {
                val absenceTypeObj = absenceTypeArray.getJSONObject(i)
                val id = absenceTypeObj.getLong("id")
                val name = absenceTypeObj.getString("name")
                val color = absenceTypeObj.getString("color")
                val subjectToApproval = absenceTypeObj.getBoolean("subjectToApproval")
                val hours = absenceTypeObj.getBoolean("hours")
                val fromTo = absenceTypeObj.getBoolean("fromTo")
                val startStop = absenceTypeObj.getBoolean("startStop")
                absenceTypes.add(
                    AbsenceTypeEntity(
                        id,
                        name,
                        color,
                        subjectToApproval,
                        hours,
                        fromTo,
                        startStop
                    )
                )
            }
            return absenceTypes
        }
    }
}