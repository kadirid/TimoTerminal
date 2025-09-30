package com.timo.timoterminal.entityClasses

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ProjectTimeEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long?,
    @ColumnInfo(name = "userId") var userId: String,
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "dateTo") var dateTo: String,
    @ColumnInfo(name = "from") var from: String,
    @ColumnInfo(name = "to") var to: String,
    @ColumnInfo(name = "hours") var hours: String,
    @ColumnInfo(name = "manDays") var manDays: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "customerId") var customerId: String,
    @ColumnInfo(name = "ticketId") var ticketId: String,
    @ColumnInfo(name = "projectId") var projectId: String,
    @ColumnInfo(name = "taskId") var taskId: String,
    @ColumnInfo(name = "orderNo") var orderNo: String,
    @ColumnInfo(name = "activityType") var activityType: String,
    @ColumnInfo(name = "activityTypeMatrix") var activityTypeMatrix: String,
    @ColumnInfo(name = "skillLevel") var skillLevel: String,
    @ColumnInfo(name = "performanceLocation") var performanceLocation: String,
    @ColumnInfo(name = "teamId") var teamId: String,
    @ColumnInfo(name = "journeyId") var journeyId: String,
    @ColumnInfo(name = "travelTime") var travelTime: String,
    @ColumnInfo(name = "drivenKm") var drivenKm: String,
    @ColumnInfo(name = "kmFlatRate") var kmFlatRate: String,
    @ColumnInfo(name = "billable") var billable: String,
    @ColumnInfo(name = "premium") var premium: String,
    @ColumnInfo(name = "units") var units: String,
    @ColumnInfo(name = "evaluation") var evaluation: String,
    @ColumnInfo(name = "isSend", defaultValue = "false") var isSend: Boolean,
    @ColumnInfo(name = "timeEntryType") var timeEntryType: String,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") val createdTime: String
) :Parcelable {

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as ProjectTimeEntity

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (date != other.date) return false
        if (dateTo != other.dateTo) return false
        if (from != other.from) return false
        if (to != other.to) return false
        if (hours != other.hours) return false
        if (manDays != other.manDays) return false
        if (description != other.description) return false
        if (customerId != other.customerId) return false
        if (ticketId != other.ticketId) return false
        if (projectId != other.projectId) return false
        if (taskId != other.taskId) return false
        if (orderNo != other.orderNo) return false
        if (activityType != other.activityType) return false
        if (activityTypeMatrix != other.activityTypeMatrix) return false
        if (skillLevel != other.skillLevel) return false
        if (performanceLocation != other.performanceLocation) return false
        if (teamId != other.teamId) return false
        if (journeyId != other.journeyId) return false
        if (travelTime != other.travelTime) return false
        if (drivenKm != other.drivenKm) return false
        if (kmFlatRate != other.kmFlatRate) return false
        if (billable != other.billable) return false
        if (premium != other.premium) return false
        if (units != other.units) return false
        if (evaluation != other.evaluation) return false
        if (isSend != other.isSend) return false
        if (timeEntryType != other.timeEntryType) return false
        if (createdTime != other.createdTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + userId.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + dateTo.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + hours.hashCode()
        result = 31 * result + manDays.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + customerId.hashCode()
        result = 31 * result + ticketId.hashCode()
        result = 31 * result + projectId.hashCode()
        result = 31 * result + taskId.hashCode()
        result = 31 * result + orderNo.hashCode()
        result = 31 * result + activityType.hashCode()
        result = 31 * result + activityTypeMatrix.hashCode()
        result = 31 * result + skillLevel.hashCode()
        result = 31 * result + performanceLocation.hashCode()
        result = 31 * result + teamId.hashCode()
        result = 31 * result + journeyId.hashCode()
        result = 31 * result + travelTime.hashCode()
        result = 31 * result + drivenKm.hashCode()
        result = 31 * result + kmFlatRate.hashCode()
        result = 31 * result + billable.hashCode()
        result = 31 * result + premium.hashCode()
        result = 31 * result + units.hashCode()
        result = 31 * result + evaluation.hashCode()
        result = 31 * result + isSend.hashCode()
        result = 31 * result + timeEntryType.hashCode()
        result = 31 * result + createdTime.hashCode()
        return result
    }

    override fun toString(): String {
        val builder : StringBuilder = StringBuilder()
        builder.append("ProjectTimeEntity(")
        builder.append("id=$id, ")
        builder.append("userId=$userId, ")
        builder.append("date='$date', ")
        builder.append("dateTo='$dateTo', ")
        builder.append("from='$from', ")
        builder.append("to='$to', ")
        builder.append("hours='$hours', ")
        builder.append("manDays='$manDays', ")
        builder.append("description='$description', ")
        builder.append("customerId='$customerId', ")
        builder.append("ticketId='$ticketId', ")
        builder.append("projectId='$projectId', ")
        builder.append("taskId='$taskId', ")
        builder.append("orderNo='$orderNo', ")
        builder.append("activityType='$activityType', ")
        builder.append("activityTypeMatrix='$activityTypeMatrix', ")
        builder.append("skillLevel='$skillLevel', ")
        builder.append("performanceLocation='$performanceLocation', ")
        builder.append("teamId='$teamId', ")
        builder.append("journeyId='$journeyId', ")
        builder.append("travelTime='$travelTime', ")
        builder.append("drivenKm='$drivenKm', ")
        builder.append("kmFlatRate='$kmFlatRate', ")
        builder.append("billable='$billable', ")
        builder.append("premium='$premium', ")
        builder.append("units='$units', ")
        builder.append("evaluation='$evaluation', ")
        builder.append("isSend=$isSend, ")
        builder.append("timeEntryType='$timeEntryType', ")
        builder.append("createdTime='$createdTime')")
        return builder.toString()
    }

    fun toMap(): Map<String, String> {
        return mapOf(
            "userId" to userId,
            "date" to date,
            "dateTo" to dateTo,
            "from" to from,
            "to" to to,
            "hours" to hours,
            "manDays" to manDays,
            "description" to description,
            "customerId" to customerId,
            "ticketId" to ticketId,
            "projectId" to projectId,
            "taskId" to taskId,
            "orderNo" to orderNo,
            "activityType" to activityType,
            "activityTypeMatrix" to activityTypeMatrix,
            "skillLevel" to skillLevel,
            "performanceLocation" to performanceLocation,
            "teamId" to teamId,
            "journeyId" to journeyId,
            "travelTime" to travelTime,
            "drivenKm" to drivenKm,
            "kmFlatRate" to kmFlatRate,
            "billable" to billable,
            "premium" to premium,
            "units" to units,
            "timeEntryType" to timeEntryType,
            "evaluation" to evaluation
        )
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(userId)
        parcel.writeString(date)
        parcel.writeString(dateTo)
        parcel.writeString(from)
        parcel.writeString(to)
        parcel.writeString(hours)
        parcel.writeString(manDays)
        parcel.writeString(description)
        parcel.writeString(customerId)
        parcel.writeString(ticketId)
        parcel.writeString(projectId)
        parcel.writeString(taskId)
        parcel.writeString(orderNo)
        parcel.writeString(activityType)
        parcel.writeString(activityTypeMatrix)
        parcel.writeString(skillLevel)
        parcel.writeString(performanceLocation)
        parcel.writeString(teamId)
        parcel.writeString(journeyId)
        parcel.writeString(travelTime)
        parcel.writeString(drivenKm)
        parcel.writeString(kmFlatRate)
        parcel.writeString(billable)
        parcel.writeString(premium)
        parcel.writeString(units)
        parcel.writeString(evaluation)
        parcel.writeByte(if (isSend) 1 else 0)
        parcel.writeString(timeEntryType)
        parcel.writeString(createdTime)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ProjectTimeEntity> {
        override fun createFromParcel(parcel: Parcel): ProjectTimeEntity {
            return ProjectTimeEntity(
                id = parcel.readValue(Long::class.java.classLoader) as? Long,
                userId = parcel.readString() ?: "",
                date = parcel.readString() ?: "",
                dateTo = parcel.readString() ?: "",
                from = parcel.readString() ?: "",
                to = parcel.readString() ?: "",
                hours = parcel.readString() ?: "",
                manDays = parcel.readString() ?: "",
                description = parcel.readString() ?: "",
                customerId = parcel.readString() ?: "",
                ticketId = parcel.readString() ?: "",
                projectId = parcel.readString() ?: "",
                taskId = parcel.readString() ?: "",
                orderNo = parcel.readString() ?: "",
                activityType = parcel.readString() ?: "",
                activityTypeMatrix = parcel.readString() ?: "",
                skillLevel = parcel.readString() ?: "",
                performanceLocation = parcel.readString() ?: "",
                teamId = parcel.readString() ?: "",
                journeyId = parcel.readString() ?: "",
                travelTime = parcel.readString() ?: "",
                drivenKm = parcel.readString() ?: "",
                kmFlatRate = parcel.readString() ?: "",
                billable = parcel.readString() ?: "",
                premium = parcel.readString() ?: "",
                units = parcel.readString() ?: "",
                evaluation = parcel.readString() ?: "",
                isSend = parcel.readByte() != 0.toByte(),
                timeEntryType = parcel.readString() ?: "",
                createdTime = parcel.readString() ?: ""
            )
        }

        override fun newArray(size: Int): Array<ProjectTimeEntity?> = arrayOfNulls(size)

        fun parseFromMap(map: Map<String, String?>): ProjectTimeEntity {
            return ProjectTimeEntity(
                id = map["id"]?.toLongOrNull(),
                userId = map["userId"] ?: "",
                date = map["date"] ?: "",
                dateTo = map["dateTo"] ?: "",
                from = map["from"] ?: "",
                to = map["to"] ?: "",
                hours = map["hours"] ?: "",
                manDays = map["manDays"] ?: "",
                description = map["description"] ?: "",
                customerId = map["customerId"] ?: "",
                ticketId = map["ticketId"] ?: "",
                projectId = map["projectId"] ?: "",
                taskId = map["taskId"] ?: "",
                orderNo = map["orderNo"] ?: "",
                activityType = map["activityType"] ?: "",
                activityTypeMatrix = map["activityTypeMatrix"] ?: "",
                skillLevel = map["skillLevel"] ?: "",
                performanceLocation = map["performanceLocation"] ?: "",
                teamId = map["teamId"] ?: "",
                journeyId = map["journeyId"] ?: "",
                travelTime = map["travelTime"] ?: "",
                drivenKm = map["drivenKm"] ?: "",
                kmFlatRate = map["kmFlatRate"] ?: "",
                billable = map["billable"] ?: "",
                premium = map["premium"] ?: "",
                units = map["units"] ?: "",
                evaluation = map["evaluation"] ?: "",
                isSend = false,
                timeEntryType = map["timeEntryType"] ?: "",
                createdTime = map["createdTime"] ?: ""
            )
        }

        fun parseFromJson(json: org.json.JSONObject): ProjectTimeEntity {
            return ProjectTimeEntity(
                id = null,
                userId = json.optString("userId", ""),
                date = json.optString("date", ""),
                dateTo = json.optString("dateTo", ""),
                from = json.optString("from", ""),
                to = json.optString("to", ""),
                hours = json.optString("hours", ""),
                manDays = json.optString("manDays", ""),
                description = json.optString("description", ""),
                customerId = json.optString("customerId", ""),
                ticketId = json.optString("ticketId", ""),
                projectId = json.optString("projectId", ""),
                taskId = json.optString("taskId", ""),
                orderNo = json.optString("orderNo", ""),
                activityType = json.optString("activityTypeId", ""),
                activityTypeMatrix = json.optString("activityTypeMatrix", ""),
                skillLevel = json.optString("skillLevel", ""),
                performanceLocation = json.optString("performanceLocation", ""),
                teamId = json.optString("teamId", ""),
                journeyId = json.optString("journeyId", ""),
                travelTime = json.optString("travelTime", ""),
                drivenKm = json.optString("drivenKm", ""),
                kmFlatRate = json.optString("kmFlatRate", ""),
                billable = json.optString("billable", ""),
                premium = json.optString("premium", ""),
                units = json.optString("units", ""),
                evaluation = json.optString("evaluation", ""),
                isSend = false,
                timeEntryType = json.optString("timeEntryType", ""),
                createdTime = ""
            )
        }
    }

}