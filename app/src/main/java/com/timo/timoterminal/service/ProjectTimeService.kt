package com.timo.timoterminal.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.entityClasses.ProjectTimeEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.ProjectTimeRepository
import com.timo.timoterminal.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent

class ProjectTimeService (
    private val projectTimeRepository: ProjectTimeRepository,
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService
) : KoinComponent {
    private fun getCompany(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
    }

    private fun getURl(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
    }

    private fun getTerminalId(): Int {
        return sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID,-1)
    }

    private fun getToken(): String {
        return sharedPrefService.getString(SharedPreferenceKeys.TOKEN, "") ?: ""
    }

    fun saveProjectTime(
        data: HashMap<String, String>,
        context: Context,
        viewModelScope: CoroutineScope,
        liveHideMask: MutableLiveData<Boolean>,
        liveMessage: MutableLiveData<String>
    ) {
        viewModelScope.launch {
            val company = getCompany() ?: ""
            val url = getURl() ?: ""
            val tId = getTerminalId()
            val token = getToken()

            if (data["date"]?.isNotEmpty() == true)
                data["date"] = Utils.getDateForTransfer(Utils.parseGCFromDate(data["date"] ?: ""))
            if (data["dateFrom"]?.isNotEmpty() == true)
                data["dateTo"] =
                    Utils.getDateForTransfer(Utils.parseGCFromDate(data["dateTo"] ?: ""))
            data["company"] = company
            data["token"] = token
            data["terminalSN"] = MainApplication.lcdk.getSerialNumber()
            data["terminalId"] = tId.toString()

            httpService.post(
                "${url}services/rest/zktecoTerminal/saveWorkingTime",
                data,
                context,
                { obj, arr, resp ->
                    Log.d("ProjectFragmentViewModel", "Response: $resp, Object: $obj, Array: $arr")
                    liveHideMask.postValue(true)
                    if (obj?.getBoolean("success") == true) {
                        liveMessage.postValue(obj.optString("message", ""))
                    } else {
                        if (obj?.has("message") == true) {
                            liveMessage.postValue("e${obj.getString("message")}")
                        }
                    }
                },
                { e, response, _, output ->
                    Log.d(
                        "ProjectFragmentViewModel",
                        "Error: $e, Response: $response, Output: $output"
                    )
                    val pte = ProjectTimeEntity.parseFromMap(data)
                    viewModelScope.launch {
                        projectTimeRepository.insert(pte)
                    }
                    liveHideMask.postValue(true)
                }
            )
        }
    }

    suspend fun sendSavedProjectTimes(scope: CoroutineScope) {
        projectTimeRepository.deleteOldProjectTimes()
        if (projectTimeRepository.count() > 0) {
            val company = getCompany() ?: ""
            val url = getURl() ?: ""
            val tId = getTerminalId()
            val token = getToken()

            if (company.isNotEmpty() && token.isNotEmpty()) {
                val projectTimes = projectTimeRepository.getAllAsList()
                val params = JSONObject()
                params.put("terminalSN" , MainApplication.lcdk.getSerialNumber())
                params.put("terminalId" , "$tId")
                params.put("token" , token)
                params.put("firma" , company)
                val arr = JSONArray()
                for (pt in projectTimes) {
                    val map = pt.toMap()
                    val obj = JSONObject()
                    for ((key, value) in map) {
                        obj.put(key, value)
                    }
                    arr.put(obj)
                }
                params.put("data" , arr)
                httpService.postJson(
                    "${url}services/rest/zktecoTerminal/offlineWorkingTime",
                    params.toString(),
                    null,
                    { _, _, msg ->
                        if(msg != null && msg.toLong() == projectTimes.size.toLong()) {
                            scope.launch {
                                for (projectTime in projectTimes) {
                                    if (projectTime.id != null)
                                        projectTimeRepository.setIsSend(projectTime.id!!)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}