package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class JourneyEntity(
    @PrimaryKey @ColumnInfo(name = "journey_id") var journeyId: Long,
    @ColumnInfo(name = "journey_name") var journeyName: String,
    @ColumnInfo(name = "journey_location") var journeyLocation: String,
    @ColumnInfo(name = "journey_time") var journeyTime: String,
    @ColumnInfo(name = "journey_km") var journeyKm: String,
    @ColumnInfo(name = "journey_vehicle") var journeyVehicle: Boolean,
    @ColumnInfo(name = "journey_start_date") var journeyStartDate: Long,
    @ColumnInfo(name = "journey_end_date") var journeyEndDate: Long,
    @ColumnInfo(name = "journey_user_id") var journeyUserId: Long
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as JourneyEntity

        if (journeyId != other.journeyId) return false
        if (journeyName != other.journeyName) return false
        if (journeyLocation != other.journeyLocation) return false
        if (journeyTime != other.journeyTime) return false
        if (journeyKm != other.journeyKm) return false
        if (journeyVehicle != other.journeyVehicle) return false
        if (journeyStartDate != other.journeyStartDate) return false
        if (journeyEndDate != other.journeyEndDate) return false
        if (journeyUserId != other.journeyUserId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = journeyId.hashCode()
        result = 31 * result + journeyName.hashCode()
        result = 31 * result + journeyLocation.hashCode()
        result = 31 * result + journeyTime.hashCode()
        result = 31 * result + journeyKm.hashCode()
        result = 31 * result + journeyVehicle.hashCode()
        result = 31 * result + journeyStartDate.hashCode()
        result = 31 * result + journeyEndDate.hashCode()
        result = 31 * result + journeyUserId.hashCode()
        return result
    }

    override fun toString(): String {
        return journeyName
    }

    companion object {
        fun parseFromJson(obj: org.json.JSONObject): List<JourneyEntity> {
            val journeys = mutableListOf<JourneyEntity>()
            val journeyArray = obj.getJSONArray("journeys")
            for (i in 0 until journeyArray.length()) {
                val journeyObj = journeyArray.getJSONObject(i)
                val journeyId = journeyObj.getLong("id")
                val journeyName = journeyObj.getString("name")
                val journeyLocation = journeyObj.getString("location")
                val journeyTime = journeyObj.getString("time")
                val journeyKm = journeyObj.getString("km")
                val journeyVehicle = journeyObj.getBoolean("vehicle")
                val journeyStartDate = journeyObj.getLong("startDate")
                val journeyEndDate = journeyObj.getLong("endDate")
                val journeyUserId = journeyObj.getLong("userId")
                journeys.add(
                    JourneyEntity(
                        journeyId,
                        journeyName,
                        journeyLocation,
                        journeyTime,
                        journeyKm,
                        journeyVehicle,
                        journeyStartDate,
                        journeyEndDate,
                        journeyUserId
                    )
                )
            }
            return journeys
        }
    }

}