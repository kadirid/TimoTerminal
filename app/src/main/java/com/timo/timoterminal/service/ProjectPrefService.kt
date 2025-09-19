package com.timo.timoterminal.service

import android.content.Context
import android.content.SharedPreferences
import com.timo.timoterminal.entityClasses.ProjectTimeTrackSetting
import com.timo.timoterminal.enums.ProjectPreferenceKeys
import com.timo.timoterminal.utils.Constants
import kotlinx.coroutines.CoroutineScope
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// This is a preference service for managing project-related preferences.
class ProjectPrefService(
    private val settingsService: SettingsService,
    context: Context) : KoinComponent {

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)

    private fun getEditor(): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    fun getInt(p0: ProjectPreferenceKeys, p1: Int): Int {
        return sharedPreferences.getInt(p0.name, p1)
    }

    fun getBoolean(p0: ProjectPreferenceKeys, p1: Boolean): Boolean {
        return sharedPreferences.getBoolean(p0.name, p1)
    }

    fun isStopwatchMode(): Boolean {
        return getInt(ProjectPreferenceKeys.PROJECT_ENTRY_TYPE, 1) == 1
    }

    companion object {
        const val TAG = "ProjectPrefService"
    }

    fun saveProjectTimeTrackSetting(obj: JSONObject) : ProjectTimeTrackSetting{
        val entryType = obj.getInt("entry_type")
        val activityType = obj.getBoolean("activity_type")
        val activityTypeMatrix = obj.getBoolean("activity_type_matrix")
        val billable = obj.getBoolean("billable")
        val customer = obj.getBoolean("customer")
        val description = obj.getBoolean("description")
        val drivenKm = obj.getBoolean("driven_km")
        val evaluation = obj.getBoolean("evaluation")
        val journey = obj.getBoolean("journey")
        val kmFlatRate = obj.getBoolean("km_flat_rate")
        val orderNo = obj.getBoolean("order_no")
        val performanceLocation = obj.getBoolean("performance_location")
        val premiumable = obj.getBoolean("premiumable")
        val skillLevel = obj.getBoolean("skill_level")
        val team = obj.getBoolean("team")
        val ticket = obj.getBoolean("ticket")
        val travelTime = obj.getBoolean("travel_time")
        val unit = obj.getBoolean("unit")

        val editor = getEditor()
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_ACTIVITY_TYPE.name, activityType)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_ACTIVITY_TYPE_MATRIX.name, activityTypeMatrix)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_BILLABLE.name, billable)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_CUSTOMER.name, customer)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_DESCRIPTION.name, description)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_DRIVEN_KM.name, drivenKm)
        editor.putInt(ProjectPreferenceKeys.PROJECT_ENTRY_TYPE.name, entryType)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_EVALUATION.name, evaluation)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_JOURNEY.name, journey)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_KM_FLAT_RATE.name, kmFlatRate)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_ORDER_NO.name, orderNo)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_PERFORMANCE_LOCATION.name, performanceLocation)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_PREMIUNMABLE.name, premiumable)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_SKILL_LEVEL.name, skillLevel)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_TEAM.name, team)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_TICKET.name, ticket)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_TRAVEL_TIME.name, travelTime)
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_UNIT.name, unit)
        editor.apply()

        return ProjectTimeTrackSetting(
            entryType,
            activityType,
            activityTypeMatrix,
            billable,
            customer,
            description,
            drivenKm,
            evaluation,
            journey,
            kmFlatRate,
            orderNo,
            performanceLocation,
            premiumable,
            skillLevel,
            team,
            ticket,
            travelTime,
            unit
        )
    }

    fun getProjectTimeTrackSetting(force: Boolean?): ProjectTimeTrackSetting {
        if (!sharedPreferences.contains(ProjectPreferenceKeys.PROJECT_ENTRY_TYPE.name) || force?:false) {
            var result: ProjectTimeTrackSetting? = null
            val latch = java.util.concurrent.CountDownLatch(1)
            settingsService.getSettingForProjectTimeTrack { success, obj ->
                if (success && obj != null) {
                    result = saveProjectTimeTrackSetting(obj)
                }
                latch.countDown()
            }
            latch.await()
            return result ?: ProjectTimeTrackSetting(
                2, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true
            )
        }

        return ProjectTimeTrackSetting(
            getInt(ProjectPreferenceKeys.PROJECT_ENTRY_TYPE, 2),
            getBoolean(ProjectPreferenceKeys.PROJECT_ACTIVITY_TYPE, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_ACTIVITY_TYPE_MATRIX, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_BILLABLE, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_CUSTOMER, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_DESCRIPTION, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_DRIVEN_KM, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_EVALUATION, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_JOURNEY, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_KM_FLAT_RATE, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_ORDER_NO, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_PERFORMANCE_LOCATION, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_PREMIUNMABLE, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_SKILL_LEVEL, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_TEAM, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_TICKET, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_TRAVEL_TIME, true),
            getBoolean(ProjectPreferenceKeys.PROJECT_UNIT, true)
        )
    }
}