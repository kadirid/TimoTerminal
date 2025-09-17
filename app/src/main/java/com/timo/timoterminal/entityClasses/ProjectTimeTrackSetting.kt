package com.timo.timoterminal.entityClasses

class ProjectTimeTrackSetting(
    var entryType: Int,
    var activityType: Boolean,
    var activityTypeMatrix: Boolean,
    var billable: Boolean,
    var customer: Boolean,
    var description: Boolean,
    var drivenKm: Boolean,
    var evaluation: Boolean,
    var journey: Boolean,
    var kmFlatRate: Boolean,
    var orderNo: Boolean,
    var performanceLocation: Boolean,
    var premiumable: Boolean,
    var skillLevel: Boolean,
    var team: Boolean,
    var ticket: Boolean,
    var travelTime: Boolean,
    var unit: Boolean
) {
    override fun toString(): String {
        return "ProjectTimeTrackSetting(entryType=$entryType, activityType=$activityType, activityTypeMatrix=$activityTypeMatrix, billable=$billable, customer=$customer, description=$description, drivenKm=$drivenKm, evaluation=$evaluation, journey=$journey, kmFlatRate=$kmFlatRate, orderNo=$orderNo, performanceLocation=$performanceLocation, premiumable=$premiumable, skillLevel=$skillLevel, team=$team, ticket=$ticket, travelTime=$travelTime, unit=$unit)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjectTimeTrackSetting

        if (entryType != other.entryType) return false
        if (activityType != other.activityType) return false
        if (activityTypeMatrix != other.activityTypeMatrix) return false
        if (billable != other.billable) return false
        if (customer != other.customer) return false
        if (description != other.description) return false
        if (drivenKm != other.drivenKm) return false
        if (evaluation != other.evaluation) return false
        if (journey != other.journey) return false
        if (kmFlatRate != other.kmFlatRate) return false
        if (orderNo != other.orderNo) return false
        if (performanceLocation != other.performanceLocation) return false
        if (premiumable != other.premiumable) return false
        if (skillLevel != other.skillLevel) return false
        if (team != other.team) return false
        if (ticket != other.ticket) return false
        if (travelTime != other.travelTime) return false
        if (unit != other.unit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entryType
        result = 31 * result + activityType.hashCode()
        result = 31 * result + activityTypeMatrix.hashCode()
        result = 31 * result + billable.hashCode()
        result = 31 * result + customer.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + drivenKm.hashCode()
        result = 31 * result + evaluation.hashCode()
        result = 31 * result + journey.hashCode()
        result = 31 * result + kmFlatRate.hashCode()
        result = 31 * result + orderNo.hashCode()
        result = 31 * result + performanceLocation.hashCode()
        result = 31 * result + premiumable.hashCode()
        result = 31 * result + skillLevel.hashCode()
        result = 31 * result + team.hashCode()
        result = 31 * result + ticket.hashCode()
        result = 31 * result + travelTime.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }
}