package com.timo.timoterminal.entityClasses

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
class AbsenceEntryEntity(
    @ColumnInfo(name = "user_id") var userId: String,
    @ColumnInfo(name = "absence_type_id") var absenceTypeId: String,
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "date_to") var dateTo: String,
    @ColumnInfo(name = "from") var from: String,
    @ColumnInfo(name = "to") var to: String,
    @ColumnInfo(name = "hours") var hours: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "deputy") var deputy: String,
    @ColumnInfo(name = "matrix") var matrix: String,
    @ColumnInfo(name = "half_day") var halfDay: String,
    @ColumnInfo(name = "full_day") var fullDay: String,
    @ColumnInfo(name = "editor_id") var editorId: String,
    @ColumnInfo(name = "is_send", defaultValue = "0") var isSend: Boolean,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") var createdTime: String? = null,
    @ColumnInfo(name = "is_visible", defaultValue = "1") var isVisible: Boolean = true,
    @ColumnInfo(name = "wt_id") var wtId: Long?,
    @ColumnInfo(name = "message") var message : String = ""
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other !is AbsenceEntryEntity) return false

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (absenceTypeId != other.absenceTypeId) return false
        if (date != other.date) return false
        if (dateTo != other.dateTo) return false
        if (from != other.from) return false
        if (to != other.to) return false
        if (hours != other.hours) return false
        if (description != other.description) return false
        if (deputy != other.deputy) return false
        if (matrix != other.matrix) return false
        if (halfDay != other.halfDay) return false
        if (fullDay != other.fullDay) return false
        if (editorId != other.editorId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + userId.hashCode()
        result = 31 * result + absenceTypeId.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + dateTo.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + hours.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + deputy.hashCode()
        result = 31 * result + matrix.hashCode()
        result = 31 * result + halfDay.hashCode()
        result = 31 * result + fullDay.hashCode()
        result = 31 * result + editorId.hashCode()
        return result
    }

    override fun toString(): String {
        val builder: StringBuilder = StringBuilder()
        builder.append("AbsenceEntryEntity { ")
        builder.append("id: ").append(id).append(", ")
        builder.append("userId: ").append(userId).append(", ")
        builder.append("absenceTypeId: ").append(absenceTypeId).append(", ")
        builder.append("date: ").append(date).append(", ")
        builder.append("dateTo: ").append(dateTo).append(", ")
        builder.append("from: ").append(from).append(", ")
        builder.append("to: ").append(to).append(", ")
        builder.append("hours: ").append(hours).append(", ")
        builder.append("description: ").append(description).append(", ")
        builder.append("deputy: ").append(deputy).append(", ")
        builder.append("matrix: ").append(matrix).append(", ")
        builder.append("halfDay: ").append(halfDay).append(", ")
        builder.append("fullDay: ").append(fullDay).append(", ")
        builder.append("editorId: ").append(editorId).append(", ")
        builder.append("isSend: ").append(isSend).append(", ")
        builder.append("createdTime: ").append(createdTime).append(", ")
        builder.append("isVisible: ").append(isVisible).append(", ")
        builder.append("wtId: ").append(wtId).append(", ")
        builder.append("message: ").append(message)
        builder.append(" }")
        return builder.toString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(id)
        dest.writeString(userId)
        dest.writeString(absenceTypeId)
        dest.writeString(date)
        dest.writeString(dateTo)
        dest.writeString(from)
        dest.writeString(to)
        dest.writeString(hours)
        dest.writeString(description)
        dest.writeString(deputy)
        dest.writeString(matrix)
        dest.writeString(halfDay)
        dest.writeString(fullDay)
        dest.writeString(editorId)
        dest.writeByte(if (isSend) 1 else 0)
        dest.writeString(createdTime)
        dest.writeByte(if (isVisible) 1 else 0)
        dest.writeValue(wtId)
        dest.writeString(message)
    }

    override fun describeContents(): Int = 0

    fun toMap(): Map<String, String?> {
        val map = mutableMapOf<String, String?>()
        map["id"] = id?.toString()
        map["userId"] = userId
        map["absenceTypeId"] = absenceTypeId
        map["date"] = date
        map["dateTo"] = dateTo
        map["from"] = from
        map["to"] = to
        map["hours"] = hours
        map["description"] = description
        map["deputy"] = deputy
        map["matrix"] = matrix
        map["halfDay"] = (halfDay == "1").toString()
        map["fullDay"] = (fullDay == "1").toString()
        map["editorId"] = editorId
        map["isSend"] = isSend.toString()
        map["createdTime"] = createdTime
        map["isVisible"] = isVisible.toString()
        map["wtId"] = wtId?.toString()
        map["message"] = message
        return map
    }

    fun toJsonObject(): JSONObject {
        val jObj = JSONObject()
        jObj.put("id", id)
        jObj.put("userId", userId)
        jObj.put("absenceTypeId", absenceTypeId)
        jObj.put("date", date)
        jObj.put("dateTo", dateTo)
        jObj.put("from", from)
        jObj.put("to", to)
        jObj.put("hours", hours)
        jObj.put("description", description)
        jObj.put("deputy", deputy)
        jObj.put("matrix", matrix)
        jObj.put("halfDay", halfDay)
        jObj.put("fullDay", fullDay)
        jObj.put("editorId", editorId)
        jObj.put("isSend", isSend)
        jObj.put("createdTime", createdTime)
        jObj.put("isVisible", isVisible)
        jObj.put("wtId", wtId)
        jObj.put("message", message)
        return jObj
    }

    companion object CREATOR : Parcelable.Creator<AbsenceEntryEntity> {
        override fun createFromParcel(parcel: Parcel): AbsenceEntryEntity {
            val id = parcel.readValue(Long::class.java.classLoader) as? Long
            val userId = parcel.readString()!!
            val absenceTypeId = parcel.readString()!!
            val date = parcel.readString()!!
            val dateTo = parcel.readString()!!
            val from = parcel.readString()!!
            val to = parcel.readString()!!
            val hours = parcel.readString()!!
            val description = parcel.readString()!!
            val deputy = parcel.readString()!!
            val matrix = parcel.readString()!!
            val halfDay = parcel.readString()!!
            val fullDay = parcel.readString()!!
            val editorId = parcel.readString()!!
            val isSend = parcel.readByte() != 0.toByte()
            val createdTime = parcel.readString()
            val isVisible = parcel.readByte() != 0.toByte()
            val wtId = parcel.readValue(Long::class.java.classLoader) as? Long?
            val message = parcel.readString() ?: ""

            return AbsenceEntryEntity(
                userId,
                absenceTypeId,
                date,
                dateTo,
                from,
                to,
                hours,
                description,
                deputy,
                matrix,
                halfDay,
                fullDay,
                editorId,
                isSend,
                createdTime,
                isVisible,
                wtId,
                message
            ).apply {
                this.id = id
            }
        }

        override fun newArray(size: Int): Array<AbsenceEntryEntity?> = arrayOfNulls(size)

        fun parseFromMap(map: Map<String, String?>): AbsenceEntryEntity {
            return AbsenceEntryEntity(
                map["userId"] ?: "",
                map["absenceTypeId"] ?: "",
                map["date"] ?: "",
                map["dateTo"] ?: "",
                map["from"] ?: "",
                map["to"] ?: "",
                map["hours"] ?: "",
                map["description"] ?: "",
                map["deputy"] ?: "",
                map["matrix"] ?: "",
                map["halfDay"] ?: "",
                map["fullDay"] ?: "",
                map["editorId"] ?: "",
                map["isSend"]?.toBoolean() ?: false,
                null,
                map["isVisible"]?.toBoolean() ?: true,
                map["wtId"]?.toLongOrNull(),
                map["message"] ?: ""
            )
        }
    }
}