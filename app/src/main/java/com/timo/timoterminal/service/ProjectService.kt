package com.timo.timoterminal.service

import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.enums.SharedPreferenceKeys
import org.json.JSONObject
import org.koin.core.component.KoinComponent

class ProjectService(
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService
) :KoinComponent {

    companion object {
        const val TAG = "ProjectService"
    }

    fun getValuesForProjectTimeTrack(onResult: (Boolean, JSONObject?) -> Unit) {
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
        val tId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
        val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
        val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
        if (!company.isNullOrEmpty() && !token.isNullOrEmpty()) {
            val params = HashMap<String, String>()
            params["company"] = company
            params["terminalSN"] = MainApplication.lcdk.getSerialNumber()
            params["terminalId"] = tId.toString()
            params["token"] = token
            httpService.get(
                "${url}services/rest/zktecoTerminal/getValuesForProjectTimeTrack",
                params,
                null,
                { obj, _, _ ->
                    onResult(true, obj)
                },
                { _, _, _, _ ->
                    onResult(false, null)
                }
            )
        }
    }

    fun getJourneysForProjectTimeTrack(userId: Int, date: String, onResult: (Boolean, JSONObject?) -> Unit) {
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
        val tId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
        val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
        val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
        if (!company.isNullOrEmpty() && !token.isNullOrEmpty()) {
            val params = HashMap<String, String>()
            params["company"] = company
            params["terminalSN"] = MainApplication.lcdk.getSerialNumber()
            params["terminalId"] = tId.toString()
            params["token"] = token
            params["userId"] = userId.toString()
            params["date"] = date
            httpService.get(
                "${url}services/rest/zktecoTerminal/getJourneysForProjectTimeTrack",
                params,
                null,
                { obj, _, _ ->
                    onResult(true, obj)
                },
                { _, _, _, _ ->
                    onResult(false, null)
                }
            )
        }
    }
}