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
    var unit: Boolean,
    var fieldOrder: String
) {

    companion object {
        const val DEFAULT_FIELD_ORDER =
            "customer,ticket,team,activityType,activityTypeMatrix,skillLevel,unit,orderNo,billable,premiumable,journey,travelTime,drivenKm,kmFlatRate,description,performanceLocation,evaluation"
    }

    // --- Datenklassen für die UI-Anordnung ---
    /** Container-IDs auf UI-Ebene (zweite Seite). */
    enum class ContainerId { ASSIGNMENT, ACTIVITY, BILLABLE, JOURNEY, DESCRIPTION, LOCATION_EVALUATION }

    /** Ergebnisstruktur für die UI-Anordnung. */
    data class UiArrangementStopWatch(
        // Reihenfolge der drei Felder oben links (erste Seite)
        val firstPageOrder: List<String>, // keys: project, task
        // Reihenfolge der Container auf der zweiten Seite
        val secondPageContainerOrder: List<ContainerId>
    )

    /** Bekannte Feldschlüssel. */
    object Keys {
        const val PROJECT = "project"
        const val TASK = "task"
        const val CUSTOMER = "customer"
        const val TICKET = "ticket"
        const val TEAM = "team"
        const val ACTIVITY_TYPE = "activityType"
        const val ACTIVITY_TYPE_MATRIX = "activityTypeMatrix"
        const val SKILL_LEVEL = "skillLevel"
        const val UNIT = "unit"
        const val ORDER_NO = "orderNo"
        const val BILLABLE = "billable"
        const val PREMIUMABLE = "premiumable"
        const val JOURNEY = "journey"
        const val TRAVEL_TIME = "travelTime"
        const val DRIVEN_KM = "drivenKm"
        const val KM_FLAT_RATE = "kmFlatRate"
        const val DESCRIPTION = "description"
        const val PERFORMANCE_LOCATION = "performanceLocation"
        const val EVALUATION = "evaluation"
    }


    /**
     * Vereinfachte und performante Ranking-Methode für die UI-Anordnung.
     * - Erste Seite (max. 3 Felder):
     *   isCustomerTimeTrack = true  -> [customer, project, task] (sichtbar, danach restliche sichtbare Felder aus field_order)
     *   isCustomerTimeTrack = false -> [project, task] (sichtbar, danach restliche sichtbare Felder aus field_order)
     * - Zweite Seite (Containerreihenfolge):
     *   Für jeden Container zählt nur der zuerst in field_order erwähnte sichtbare Key. Nach dessen Index wird sortiert.
     *   Container ohne Treffer behalten eine stabile Default-Reihenfolge.
     */
    fun arrangeInputsBasedOnSettingsForStopwatch(isCustomerTimeTrack: Boolean): UiArrangementStopWatch {
        // 1) Reihenfolge-Quelle aufbereiten
        val order = (fieldOrder.takeIf { it.isNotBlank() } ?: DEFAULT_FIELD_ORDER)
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // 2) Sichtbarkeiten per Settings berücksichtigen
        fun visible(key: String): Boolean = when (key) {
            Keys.CUSTOMER -> customer
            Keys.TICKET -> ticket
            Keys.TEAM -> team
            Keys.ACTIVITY_TYPE -> activityType
            Keys.ACTIVITY_TYPE_MATRIX -> activityTypeMatrix && activityType
            Keys.SKILL_LEVEL -> skillLevel
            Keys.UNIT -> unit
            Keys.ORDER_NO -> orderNo
            Keys.BILLABLE -> billable
            Keys.PREMIUMABLE -> premiumable
            Keys.JOURNEY -> journey
            Keys.TRAVEL_TIME -> travelTime
            Keys.DRIVEN_KM -> drivenKm
            Keys.KM_FLAT_RATE -> kmFlatRate
            Keys.DESCRIPTION -> description
            Keys.PERFORMANCE_LOCATION -> performanceLocation
            Keys.EVALUATION -> evaluation
            Keys.PROJECT, Keys.TASK -> true
            else -> true
        }

        // 3) Erste Seite: wie gehabt
        val firstPageOrder = buildList {
            val base = listOf(Keys.TASK, Keys.PROJECT)
            fun rankOf(key: String): Int {
                val idx = order.indexOf(key)
                return if (idx >= 0) idx else Int.MAX_VALUE - base.indexOf(key)
            }
            val sortedHead = if (isCustomerTimeTrack) {
                val pt = listOf(Keys.TASK, Keys.PROJECT).sortedBy { rankOf(it) }
                listOf(Keys.CUSTOMER) + pt
            } else {
                listOf(Keys.TASK, Keys.PROJECT).sortedBy { rankOf(it) }
            }
            val visibleHead = sortedHead.filter { visible(it) }
            val headSet = (if (isCustomerTimeTrack) listOf(Keys.CUSTOMER, Keys.PROJECT, Keys.TASK) else listOf(Keys.PROJECT, Keys.TASK)).toSet()
            val remaining = order.filter { it !in headSet && visible(it) }
            addAll(visibleHead)
            addAll(remaining)
        }.take(3)

        // 4) Container Mapping
        val containerOfKey = mapOf(
            Keys.TICKET to ContainerId.ASSIGNMENT,
            Keys.TEAM to ContainerId.ASSIGNMENT,
            Keys.ACTIVITY_TYPE to ContainerId.ACTIVITY,
            Keys.ACTIVITY_TYPE_MATRIX to ContainerId.ACTIVITY,
            Keys.SKILL_LEVEL to ContainerId.ACTIVITY,
            Keys.UNIT to ContainerId.ACTIVITY,
            Keys.ORDER_NO to ContainerId.BILLABLE,
            Keys.BILLABLE to ContainerId.BILLABLE,
            Keys.PREMIUMABLE to ContainerId.BILLABLE,
            Keys.JOURNEY to ContainerId.JOURNEY,
            Keys.TRAVEL_TIME to ContainerId.JOURNEY,
            Keys.DRIVEN_KM to ContainerId.JOURNEY,
            Keys.KM_FLAT_RATE to ContainerId.JOURNEY,
            Keys.DESCRIPTION to ContainerId.DESCRIPTION,
            Keys.PERFORMANCE_LOCATION to ContainerId.LOCATION_EVALUATION,
            Keys.EVALUATION to ContainerId.LOCATION_EVALUATION
        )

        // 5) Für jeden Container den zuerst in field_order auftauchenden sichtbaren Key finden
        val firstIndexPerContainer = mutableMapOf<ContainerId, Int>()
        for ((idx, key) in order.withIndex()) {
            if (!visible(key)) continue
            val container = containerOfKey[key] ?: continue
            if (firstIndexPerContainer.containsKey(container)) continue
            firstIndexPerContainer[container] = idx
        }

        // 6) Endgültige Reihenfolge bestimmen
        val allContainers = listOf(
            ContainerId.ASSIGNMENT,
            ContainerId.ACTIVITY,
            ContainerId.BILLABLE,
            ContainerId.JOURNEY,
            ContainerId.DESCRIPTION,
            ContainerId.LOCATION_EVALUATION
        )
        val secondPageContainerOrder = allContainers
            .sortedWith(compareBy({ firstIndexPerContainer[it] ?: Int.MAX_VALUE }, { defaultContainerIndex(it) }))

        return UiArrangementStopWatch(
            firstPageOrder = firstPageOrder,
            secondPageContainerOrder = secondPageContainerOrder
        )
    }

    // Fallback-Index, falls ein Container keine Felder hat (oder alle unsichtbar sind)
    private fun defaultContainerIndex(id: ContainerId): Int = when (id) {
        ContainerId.ASSIGNMENT -> 0
        ContainerId.ACTIVITY -> 1
        ContainerId.BILLABLE -> 2
        ContainerId.JOURNEY -> 3
        ContainerId.DESCRIPTION -> 4
        ContainerId.LOCATION_EVALUATION -> 5
    }

    override fun toString(): String {
        return "ProjectTimeTrackSetting(entryType=$entryType, activityType=$activityType, activityTypeMatrix=$activityTypeMatrix, billable=$billable, customer=$customer, description=$description, drivenKm=$drivenKm, evaluation=$evaluation, journey=$journey, kmFlatRate=$kmFlatRate, orderNo=$orderNo, performanceLocation=$performanceLocation, premiumable=$premiumable, skillLevel=$skillLevel, team=$team, ticket=$ticket, travelTime=$travelTime, unit=$unit, field_order='$fieldOrder')"
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
        if (fieldOrder != other.fieldOrder) return false

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
        result = 31 * result + fieldOrder.hashCode()
        return result
    }
}