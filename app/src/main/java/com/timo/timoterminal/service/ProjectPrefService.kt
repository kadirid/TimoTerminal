package com.timo.timoterminal.service

import android.content.Context
import android.content.SharedPreferences
import com.timo.timoterminal.entityClasses.ProjectTimeTrackSetting
import com.timo.timoterminal.enums.ProjectPreferenceKeys
import com.timo.timoterminal.utils.Constants
import org.json.JSONObject
import org.koin.core.component.KoinComponent

// This is a preference service for managing project-related preferences.
class ProjectPrefService(context: Context) : KoinComponent {
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

    fun saveProjectTimeTrackSetting(obj: JSONObject) : ProjectTimeTrackSetting{
        val editor = getEditor()
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_ACTIVITY_TYPE.name, obj.getBoolean("activity_type"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_ACTIVITY_TYPE_MATRIX.name, obj.getBoolean("activity_type_matrix"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_BILLABLE.name, obj.getBoolean("billable"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_CUSTOMER.name, obj.getBoolean("customer"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_DESCRIPTION.name, obj.getBoolean("description"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_DRIVEN_KM.name, obj.getBoolean("driven_km"))
        editor.putInt(ProjectPreferenceKeys.PROJECT_ENTRY_TYPE.name, obj.getInt("entry_type"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_EVALUATION.name, obj.getBoolean("evaluation"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_JOURNEY.name, obj.getBoolean("journey"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_KM_FLAT_RATE.name, obj.getBoolean("km_flat_rate"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_ORDER_NO.name, obj.getBoolean("order_no"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_PERFORMANCE_LOCATION.name, obj.getBoolean("performance_location"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_PREMIUNMABLE.name, obj.getBoolean("premiumable"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_SKILL_LEVEL.name, obj.getBoolean("skill_level"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_TEAM.name, obj.getBoolean("team"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_TICKET.name, obj.getBoolean("ticket"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_TRAVEL_TIME.name, obj.getBoolean("travel_time"))
        editor.putBoolean(ProjectPreferenceKeys.PROJECT_UNIT.name, obj.getBoolean("unit"))
        editor.apply()

        return ProjectTimeTrackSetting(
            obj.getInt("entry_type"),
            obj.getBoolean("activity_type"),
            obj.getBoolean("activity_type_matrix"),
            obj.getBoolean("billable"),
            obj.getBoolean("customer"),
            obj.getBoolean("description"),
            obj.getBoolean("driven_km"),
            obj.getBoolean("evaluation"),
            obj.getBoolean("journey"),
            obj.getBoolean("km_flat_rate"),
            obj.getBoolean("order_no"),
            obj.getBoolean("performance_location"),
            obj.getBoolean("premiumable"),
            obj.getBoolean("skill_level"),
            obj.getBoolean("team"),
            obj.getBoolean("ticket"),
            obj.getBoolean("travel_time"),
            obj.getBoolean("unit")
        )
    }

    fun getProjectTimeTrackSetting(): ProjectTimeTrackSetting {
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