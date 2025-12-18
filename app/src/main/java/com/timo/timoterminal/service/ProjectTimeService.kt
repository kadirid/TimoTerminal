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
    private val httpService: HttpService,
    private val languageService: LanguageService
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
        data: HashMap<String, String?>,
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
            if (data["dateTo"]?.isNotEmpty() == true)
                data["dateTo"] =
                    Utils.getDateForTransfer(Utils.parseGCFromDate(data["dateTo"] ?: ""))

            val params = HashMap<String, String>()
            for (key in data.keys) {
                val value = data[key]
                if (value != null) {
                    params[key] = value
                }
            }

            params["company"] = company
            params["token"] = token
            params["terminalSN"] = MainApplication.lcdk.getSerialNumber()
            params["terminalId"] = tId.toString()

            httpService.post(
                "${url}services/rest/zktecoTerminal/saveWorkingTime",
                params,
                context,
                { obj, _, _ ->
                    liveHideMask.postValue(true)
                    if (obj?.getBoolean("success") == true) {
                        liveMessage.postValue(obj.optString("message", ""))
                        if (data["id"] != null) {
                            viewModelScope.launch {
                                projectTimeRepository.deleteById(data["id"]?.toLong() ?: 0L)
                            }
                        }
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
                    liveMessage.postValue(languageService.getText("#BookingTemporarilySaved"))
                    viewModelScope.launch {
                        if((pte.id ?: 0L) > 0L) {
                            projectTimeRepository.insert(pte)
                        } else {
                            projectTimeRepository.insertWithoutCreatedTime(pte)
                        }
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
                                        projectTimeRepository.setIsSend(
                                            projectTime.id!!,
                                            true,
                                            obj.getString("message")
                                        )
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
                { obj, _, _ ->
                    if (obj?.getBoolean("success") == true) {
                        val savedId = obj.optInt("id", -1)
                        val pte = ProjectTimeEntity.parseFromMap(data).apply {
                            wtId = savedId.toLong()
                            id = savedId.toLong()
                        }
                        pte.isVisible = false
                        viewModelScope.launch { projectTimeRepository.insertWithoutCreatedTime(pte) }
                        if (continuation.isActive) continuation.resume(savedId.toLong())
                    } else {
                        if (obj?.has("errors") == true) {
                            liveMessage.postValue("e${obj.getString("errors")}")
                            if (continuation.isActive) continuation.resume(-1L)
                        }
                    }
                    liveHideMask.postValue(true)
                },
                { e, response, _, output ->
                    Log.d(
                        "ProjectFragmentViewModel",
                        "Error: $e, Response: $response, Output: $output"
                    )
                    val pte = ProjectTimeEntity.parseFromMap(data)
                    liveMessage.postValue(languageService.getText("#BookingTemporarilySaved"))
                    viewModelScope.launch { projectTimeRepository.insertWithoutCreatedTime(pte) }
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
        data: HashMap<String, String>,
        context: Context,
        viewModelScope: CoroutineScope,
        liveMessage: MutableLiveData<String>
    ): Int = suspendCancellableCoroutine { continuation ->
        val company = getCompany() ?: ""
        val url = getURl() ?: ""
        val tId = getTerminalId()
        val token = getToken()

        val parameters = mapOf(
            Pair("company", company),
            Pair("terminalSN", MainApplication.lcdk.getSerialNumber()),
            Pair("terminalId", "$tId"),
            Pair("token", token),
        ) + data.map { Pair(it.key, it.value) }

        try {
            httpService.get(
                "${url}services/rest/zktecoTerminal/stopLatestOpenWorkingTime",
                parameters,
                context,
                { obj, _, _ ->
                    if (obj?.getBoolean("success") == true) {
                        continuation.resume(0)
                    } else {
                        if (obj?.has("message") == true) {
                            liveMessage.postValue("e${obj.getString("message")}")
                        }
                        continuation.resume(1)
                    }
                },
                { e, response, _, output ->
                    Log.d("ProjectTimeService", "Error: $e, Response: $response, Output: $output")
                    val pte = ProjectTimeEntity.parseFromMap(data)
                    liveMessage.postValue(languageService.getText("#BookingTemporarilySaved"))
                    viewModelScope.launch { projectTimeRepository.insertWithoutCreatedTime(pte) }
                    continuation.resume(2)
                }
            )
        } catch (e: Exception) {
            Log.e("ProjectTimeService", "Exception in getLatestOpenWorkingTime: ${e.message}")
            continuation.resume(3)
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
                        if (obj != null && obj.getString("message").equals("ok")) {
                            val payload = obj.getJSONObject("payload")
                            val result =
                                ProjectTimeEntity.parseFromJson(payload.getJSONObject("wt"))
                            if (!payload.isNull("skill") && payload.getString("skill")
                                    .isNotEmpty()
                            ) {
                                result.skillLevel = payload.getString("skill")
                            }
                            if (!payload.isNull("placeOfWork") && payload.getString("placeOfWork")
                                    .isNotEmpty()
                            ) {
                                result.performanceLocation = payload.getString("placeOfWork")
                            }
                            if (!payload.isNull("drivenKm") && payload.getString("drivenKm")
                                    .isNotEmpty()
                            ) {
                                result.drivenKm = payload.getString("drivenKm")
                            }
                            if (!payload.isNull("kmFlatRate") && payload.getString("kmFlatRate")
                                    .isNotEmpty()
                            ) {
                                result.kmFlatRate = payload.getString("kmFlatRate")
                            }
                            if (!payload.isNull("travelTime") && payload.getString("travelTime")
                                    .isNotEmpty()
                            ) {
                                result.travelTime = payload.getString("travelTime")
                            }
                            continuation.resume(result)
                        }
                    } catch (e: Exception) {
                        continuation.resume(null)
                    }
                },
                { _, _, _, output ->
                    continuation.resume(null)
                }
            )
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }

    suspend fun getLatestOpenByUserId(userId: String): ProjectTimeEntity? {
        return projectTimeRepository.getLatestOpenByUserId(userId)
    }

    suspend fun stopOfflineProjectTime(data: HashMap<String, String>) {
        val userId = data["userId"]?.toLongOrNull() ?: return
        val pte = projectTimeRepository.getLatestOpenByUserId(userId.toString()) ?: return
        projectTimeRepository.deleteById(pte.id ?: 0L)
        val time = ProjectTimeEntity.parseFromMap(data)
        time.date = pte.date
        time.from = pte.from
        time.isVisible = true
        time.isSend = false
        projectTimeRepository.insert(time)
    }

    fun deleteAll(scope: CoroutineScope) {
        scope.launch {
            projectTimeRepository.deleteAll()
        }
    }

    suspend fun getCountOfFailedForUser(userId: String): Int {
        return projectTimeRepository.getCountOfFailedForUser(userId)
    }

    suspend fun deleteByWtId(wtId: Long, unique: String) {
        if (projectTimeRepository.deleteByWtId(wtId) > 0 && unique.isNotEmpty()){
            httpService.responseForCommand(unique)
        }
    }
}