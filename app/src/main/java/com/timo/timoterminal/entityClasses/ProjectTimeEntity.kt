package com.timo.timoterminal.entityClasses

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
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") val createdTime: String
) {

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
        result = 31 * result + createdTime.hashCode()
        return result
    }

    override fun toString(): String {
        return "ProjectTimeEntity(id=$id, userId=$userId, date='$date', dateTo='$dateTo', from='$from', to='$to', hours='$hours', manDays='$manDays', description='$description', customerId='$customerId', ticketId='$ticketId', projectId='$projectId', taskId='$taskId', orderNo='$orderNo', activityType='$activityType', activityTypeMatrix='$activityTypeMatrix', skillLevel='$skillLevel', performanceLocation='$performanceLocation', teamId='$teamId', journeyId='$journeyId', travelTime='$travelTime', drivenKm='$drivenKm', kmFlatRate='$kmFlatRate', billable='$billable', premium='$premium', units='$units', evaluation='$evaluation', isSend=$isSend, createdTime='$createdTime')"
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
            "evaluation" to evaluation
        )
    }

    companion object {
        fun parseFromMap(map: Map<String, String>): ProjectTimeEntity {
            return ProjectTimeEntity(
                id = null,
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
                createdTime = ""
            )
        }
    }
}