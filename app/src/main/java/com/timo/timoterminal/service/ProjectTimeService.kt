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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import org.json.JSONObject
import org.koin.core.component.KoinComponent

class ProjectTimeService(
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
        return sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
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
                val projectTime = projectTimeRepository.getNextNotSend() ?: return
                val params = JSONObject()
                params.put("terminalSN", MainApplication.lcdk.getSerialNumber())
                params.put("terminalId", "$tId")
                params.put("token", token)
                params.put("firma", company)
                val map = projectTime.toMap()
                val jObj = JSONObject()
                for ((key, value) in map) {
                    jObj.put(key, value)
                }
                params.put("data", jObj)
                httpService.postJson(
                    "${url}services/rest/zktecoTerminal/offlineWorkingTime",
                    params.toString(),
                    null,
                    { obj, _, _ ->
                        if (obj?.getBoolean("success") == true) {
                            scope.launch {
                                if (projectTime.id != null) {
                                    if (obj.getBoolean("isSend")) {
                                        projectTimeRepository.deleteById(projectTime.id!!)
                                    } else {
                                        projectTimeRepository.setIsSend(projectTime.id!!)
                                    }
                                }
                            }
                            Unit
                        }
                    }
                )
            }
        }
    }

    suspend fun startWorkingTime(
        data: HashMap<String, String>,
        context: Context,
        viewModelScope: CoroutineScope,
        liveHideMask: MutableLiveData<Boolean>,
        liveMessage: MutableLiveData<String>
    ): Long = suspendCancellableCoroutine { continuation ->
        val company = getCompany() ?: ""
        val url = getURl() ?: ""
        val tId = getTerminalId()
        val token = getToken()

        data["company"] = company
        data["token"] = token
        data["terminalSN"] = MainApplication.lcdk.getSerialNumber()
        data["terminalId"] = tId.toString()

        try {
            httpService.post(
                "${url}services/rest/zktecoTerminal/startWorkingTime",
                data,
                context,
                { obj, arr, resp ->
                    Log.d("ProjectFragmentViewModel", "Response: $resp, Object: $obj, Array: $arr")
                    val savedId = obj?.optInt("id", -1) ?: -1
                    val pte = ProjectTimeEntity.parseFromMap(data).apply { id = savedId.toLong() }
                    viewModelScope.launch { projectTimeRepository.insert(pte) }
                    liveHideMask.postValue(true)
                    if (continuation.isActive) continuation.resume(savedId.toLong())
                },
                { e, response, _, output ->
                    Log.d(
                        "ProjectFragmentViewModel",
                        "Error: $e, Response: $response, Output: $output"
                    )
                    val pte = ProjectTimeEntity.parseFromMap(data)
                    viewModelScope.launch { projectTimeRepository.insert(pte) }
                    liveHideMask.postValue(true)
                    if (continuation.isActive) continuation.resume(-1L)
                }
            )
        } catch (ex: Exception) {
            Log.e("ProjectTimeService", "Exception in startWorkingTime: ${ex.message}")
            if (continuation.isActive) continuation.resume(-1L)
        }
    }

    suspend fun stopLatestOpenWorkingTime(
        userId: Long,
        context: Context
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val company = getCompany() ?: ""
        val url = getURl() ?: ""
        val tId = getTerminalId()
        val token = getToken()

        val parameters = mapOf(
            Pair("company", company),
            Pair("terminalSN", MainApplication.lcdk.getSerialNumber()),
            Pair("terminalId", "$tId"),
            Pair("token", token),
            Pair("userId", "$userId"),
        )

        try {
            httpService.get(
                "${url}services/rest/zktecoTerminal/stopLatestOpenWorkingTime",
                parameters,
                context,
                { obj, arr, resp ->
                    Log.d("ProjectTimeService", "Response: $resp, Object: $obj, Array: $arr")
                    continuation.resume(true)
                },
                { e, response, _, output ->
                    Log.d("ProjectTimeService", "Error: $e, Response: $response, Output: $output")
                    continuation.resume(false)
                }
            )
        } catch (e: Exception) {
            Log.e("ProjectTimeService", "Exception in getLatestOpenWorkingTime: ${e.message}")
            continuation.resume(false)
        }
    }

    suspend fun getLatestOpenWorkingTime(
        userId: Long,
        context: Context
    ): ProjectTimeEntity? = suspendCancellableCoroutine { continuation ->
        val company = getCompany() ?: ""
        val url = getURl() ?: ""
        val tId = getTerminalId()
        val token = getToken()

        val parameters = mapOf(
            Pair("company", company),
            Pair("terminalSN", MainApplication.lcdk.getSerialNumber()),
            Pair("terminalId", "$tId"),
            Pair("token", token),
            Pair("userId", "$userId")
        )

        try {
            httpService.get(
                "${url}services/rest/zktecoTerminal/getLatestOpenWorkingTime",
                parameters,
                context,
                { obj, arr, resp ->
                    try {
                        if (obj != null && obj.getBoolean("success")) {
                            val result = ProjectTimeEntity.parseFromJson(obj.getJSONObject("wt"))
                            if (!obj.isNull("skill") && obj.getString("skill").isNotEmpty()) {
                                result.skillLevel = obj.getString("skill")
                            }
                            continuation.resume(result)
                        }
                    } catch (e: Exception) {
                        continuation.resume(null)
                    }
                },
                { e, response, _, output ->
                    continuation.resume(null)
                }
            )
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }
}