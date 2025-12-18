package com.timo.timoterminal.service

import android.content.Context
import android.util.Log
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.entityClasses.AbsenceEntryEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.AbsenceEntryRepository
import com.timo.timoterminal.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.core.component.KoinComponent

class AbsenceService(
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService,
    private val absenceEntryRepository: AbsenceEntryRepository
) : KoinComponent {

    fun getValuesForAbsence(userId: Long, onResult: (Boolean, JSONObject?) -> Unit) {
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
            httpService.get(
                "${url}services/rest/zktecoTerminal/getValuesForAbsence",
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

    fun saveAbsenceEntry(
        data: HashMap<String, String?>,
        context: Context,
        onResult: (Boolean, JSONObject?) -> Unit
    ) {
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL) ?: ""

        if (data["date"]?.isNotEmpty() == true)
            data["date"] =
                Utils.getDateForTransfer(Utils.parseGCFromDate(data["date"] ?: ""))
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

        if (params["halfDay"] == "1") {
            params["halfDay"] = "true"
        }
        if (params["fullDay"] == "1") {
            params["fullDay"] = "true"
        }

        params["company"] = sharedPrefService.getString(SharedPreferenceKeys.COMPANY) ?: ""
        params["token"] = sharedPrefService.getString(SharedPreferenceKeys.TOKEN) ?: ""
        params["terminalSN"] = MainApplication.lcdk.getSerialNumber()
        params["terminalId"] =
            sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1).toString()

        httpService.post(
            "${url}services/rest/zktecoTerminal/saveAbsenceEntry",
            params,
            context,
            { obj, arr, resp ->
                onResult(true, obj)
            },
            { e, response, _, output ->
                Log.d(
                    "AbsenceService",
                    "Error in saveAbsenceEntry: $e, Response: $response, Output: $output"
                )
                onResult(false, null)
            }
        )
    }

    suspend fun saveAbsenceEntry(entity: AbsenceEntryEntity) {
        if (entity.id == null) {
            absenceEntryRepository.insertAbsenceEntry(entity)
        } else {
            absenceEntryRepository.insert(entity)
        }
    }

    suspend fun deleteById(id: Long) {
        absenceEntryRepository.deleteById(id)
    }

    suspend fun getPageAsList(currentPage: Int, userId: String?) =
        absenceEntryRepository.getPageAsList(currentPage, userId)

    suspend fun getCountOfFailedForUser(userId: String): Int =
        absenceEntryRepository.getCountOfFailedForUser(userId)

    suspend fun getLatestOpenByUserId(userId: String): AbsenceEntryEntity? =
        absenceEntryRepository.getLatestOpenByUserId(userId)

    suspend fun sendSavedAbsenceEntries(scope: CoroutineScope) {
        absenceEntryRepository.deleteOldAbsenceEntries(true)
        val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY) ?: ""
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL) ?: ""
        val tId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
        val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN) ?: ""

        if (company.isNotEmpty() && token.isNotEmpty()) {
            val absenceEntry = absenceEntryRepository.getNextNotSend() ?: return
            val params = JSONObject()
            params.put("terminalSN", MainApplication.lcdk.getSerialNumber())
            params.put("terminalId", "$tId")
            params.put("token", token)
            params.put("firma", company)
            val map = absenceEntry.toMap()
            val jObj = JSONObject()
            for ((key, value) in map) {
                jObj.put(key, value)
            }
            params.put("data", jObj)
            httpService.postJson(
                "${url}services/rest/zktecoTerminal/offlineAbsenceEntry",
                params.toString(),
                null,
                { obj, _, _ ->
                    if (obj?.getBoolean("success") == true) {
                        scope.launch {
                            if (absenceEntry.id != null) {
                                if (obj.getBoolean("isSend")) {
                                    absenceEntryRepository.deleteById(absenceEntry.id!!)
                                } else {
                                    absenceEntryRepository.setIsSend(
                                        absenceEntry.id!!,
                                        true,
                                        obj.optString("message", "")
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    fun startAbsenceTime(
        data: HashMap<String, String?>,
        context: Context,
        onResult: (Boolean, JSONObject?) -> Unit
    ) {
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL) ?: ""

        if (data["date"]?.isNotEmpty() == true)
            data["date"] =
                Utils.getDateForTransfer(Utils.parseGCFromDate(data["date"] ?: ""))

        val params = HashMap<String, String>()
        for (key in data.keys) {
            val value = data[key]
            if (value != null) {
                params[key] = value
            }
        }

        params["company"] = sharedPrefService.getString(SharedPreferenceKeys.COMPANY) ?: ""
        params["token"] = sharedPrefService.getString(SharedPreferenceKeys.TOKEN) ?: ""
        params["terminalSN"] = MainApplication.lcdk.getSerialNumber()
        params["terminalId"] =
            sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1).toString()

        httpService.post(
            "${url}services/rest/zktecoTerminal/startAbsenceTime",
            params,
            context,
            { obj, arr, resp ->
                onResult(true, obj)
            },
            { e, response, _, output ->
                Log.d(
                    "AbsenceService",
                    "Error in startAbsenceTime: $e, Response: $response, Output: $output"
                )
                onResult(false, null)
            }
        )
    }

    fun stopAbsenceTime(
        data: HashMap<String, String?>,
        context: Context,
        onResult: (Boolean, JSONObject?) -> Unit
    ) {
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL) ?: ""

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

        params["company"] = sharedPrefService.getString(SharedPreferenceKeys.COMPANY) ?: ""
        params["token"] = sharedPrefService.getString(SharedPreferenceKeys.TOKEN) ?: ""
        params["terminalSN"] = MainApplication.lcdk.getSerialNumber()
        params["terminalId"] =
            sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1).toString()

        httpService.post(
            "${url}services/rest/zktecoTerminal/stopAbsenceTime",
            params,
            context,
            { obj, arr, resp ->
                onResult(true, obj)
            },
            { e, response, _, output ->
                Log.d(
                    "AbsenceService",
                    "Error in stopAbsenceTime: $e, Response: $response, Output: $output"
                )
                onResult(false, null)
            }
        )
    }

    suspend fun stopOfflineAbsenceTime(data: HashMap<String, String?>) {
        val userId = data["userId"]?.toLongOrNull() ?: return
        val latestOpen = absenceEntryRepository.getLatestOpenByUserId(userId.toString()) ?: return
        if (data["dateTo"]?.isNotEmpty() == true)
            data["dateTo"] =
                Utils.getDateForTransfer(Utils.parseGCFromDate(data["dateTo"] ?: ""))
        absenceEntryRepository.deleteById(latestOpen.id!!)
        val absence = AbsenceEntryEntity.parseFromMap(data)
        absence.date = latestOpen.date
        absence.from = latestOpen.from
        absence.isVisible = true
        absence.isSend = false
        saveAbsenceEntry(absence)
    }

    suspend fun deleteByWtId(wtId: Long, unique: String) {
        if (absenceEntryRepository.deleteByWtId(wtId) > 0 && unique.isNotEmpty()) {
            httpService.responseForCommand(unique)
        }
    }

    fun deleteAll(scope: CoroutineScope) {
        scope.launch {
            absenceEntryRepository.deleteAll()
        }
    }
}