package com.timo.timoterminal.service

import android.content.Context
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.serviceUtils.classes.UserInformation
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.ResponseToJSON
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import java.util.Date
import java.util.concurrent.Executors

class UserService(
    private val httpService: HttpService,
    private val sharedPrefService: SharedPrefService,
    private val userRepository: UserRepository
) : KoinComponent {
    private val executor = Executors.newFixedThreadPool(5)

    suspend fun getEntity(id: Long): List<UserEntity> = userRepository.getEntity(id)

    suspend fun insertOne(user: UserEntity) = userRepository.insertOne(user)

    suspend fun getAllAsList(): List<UserEntity> = userRepository.getAllAsList()

    suspend fun getPageAsList(pageNo: Int): List<UserEntity> = userRepository.getPageAsList(pageNo)

    fun loadUsersFromServer(scope: CoroutineScope, unique: String = "") {
        scope.launch {
            //Load from server
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val tId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
            val params = HashMap<String, String>()
            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
                params["company"] = company
                params["token"] = token
                params["terminalSN"] = MainApplication.lcdk.getSerialNumber()
                params["terminalId"] = tId.toString()
                httpService.get("${url}services/rest/zktecoTerminal/loadAllUsers",
                    params,
                    null,
                    { _, arrResponse, _ ->
                        //Save it persistently offline
                        processUserArray(arrResponse, scope, unique, true, deleteOld = true)
                    })
            }
        }
    }

    fun processUserArray(
        arrResponse: JSONArray?,
        scope: CoroutineScope,
        unique: String,
        loadFP: Boolean = false,
        deleteOld: Boolean = false
    ) {
        loadIntoDB(arrResponse, scope, deleteOld)
        if (loadFP) {
            getFPFromServer(scope)
        }
        if (unique.isNotEmpty()) {
            httpService.responseForCommand(unique)
        }
    }

    private fun loadIntoDB(
        arrResponse: JSONArray?,
        coroutineScope: CoroutineScope,
        deleteOld: Boolean = false
    ) {
        coroutineScope.launch {
            val users = ArrayList<UserEntity>()
            if (arrResponse!!.length() > 0) {
                if (deleteOld) {
                    userRepository.deleteAll()
                    MainApplication.lcdk.clearFingerPrint()
                }
            }
            for (i in 0 until arrResponse.length()) {
                val obj = validateUserResponse(arrResponse.getJSONObject(i))
                if (obj != null) {
                    val userEntity: UserEntity = UserEntity.parseJsonToUserEntity(obj)
                    users.add(userEntity)
                    if (userEntity.assignedToTerminal && obj.has("fp")) {
                        val fpObj = obj.getJSONObject("fp")
                        executor.execute {
                            processFPObj(fpObj, userEntity.id.toString())
                        }
                    }
                }
            }
            userRepository.insertUserEntity(users)
        }
    }

    fun deleteAllUsers(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            userRepository.deleteAll()
            MainApplication.lcdk.clearFingerPrint()
        }
    }

    private fun getFPFromServer(viewModelScope: CoroutineScope) {
        viewModelScope.launch {
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val tId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)

            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
                val paramMap = mutableMapOf(Pair("company", company))
                paramMap["token"] = token
                paramMap["terminalSN"] = MainApplication.lcdk.getSerialNumber()
                paramMap["terminalId"] = tId.toString()
                httpService.get(
                    "${url}services/rest/zktecoTerminal/getAllFPForTerminal",
                    paramMap,
                    null,
                    { _, arr, _ ->
                        MainApplication.lcdk.clearFingerPrint()
                        processFPArray(arr)
                    }
                )
            }
        }
    }

    fun getFPForUser(
        url: String?,
        callback: () -> Unit?,
        id: String,
        company: String,
        token: String,
        tId: Int
    ) {
        val paramMap = mutableMapOf(Pair("company", company))
        paramMap["user"] = id
        paramMap["token"] = token
        paramMap["terminalSN"] = MainApplication.lcdk.getSerialNumber()
        paramMap["terminalId"] = tId.toString()
        httpService.get(
            "${url}services/rest/zktecoTerminal/getFP",
            paramMap,
            null,
            { obj, _, _ ->
                if (obj != null) {
                    processFPObj(obj)
                }
                callback()
            }
        )
    }

    private fun validateUserResponse(
        obj: JSONObject?,
    ): JSONObject? {
        if (obj != null) {
            if (obj.has("id")) {
                return obj
            }
        }
        return null
    }

    /**
     * Load user information from server
     * successCallback contains a boolean which shows if the request was successful or not. If not, the message will be filled with a message from the server. Otherwise the objResponse will contain the UserInformation object.
     * errorCallback contains the exception, the response, the context and the output from the server.
     * @author Elias Kadiri
     * @param user UserEntity
     * @param successCallback (success: Boolean, message: String, objResponse: UserInformation?) -> Unit?
     * @param errorCallback (e: Exception?, response: Response?, context: Context?, output: ResponseToJSON?) -> Unit?
     * @return UserInformation
     * @since 0.0.1
     *
     */
    fun loadUserInformation(
        user: UserEntity,
        from: Date,
        successCallback: (success: Boolean, message: String, objResponse: UserInformation?) -> Unit?,
        errorCallback: ((e: Exception?, response: Response?, context: Context?, output: ResponseToJSON?) -> Unit?)
    ) {
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
        val tId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
        val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
        val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
        if (!company.isNullOrEmpty() && !token.isNullOrEmpty()) {
            val params = HashMap<String, String>()
            val card = user.card.ifEmpty { user.id.toString() }
            params["card"] = card
            params["firma"] = company
            params["terminalSN"] = MainApplication.lcdk.getSerialNumber()
            params["terminalId"] = tId.toString()
            params["token"] = token
            params["from"] = from.time.toString()
            return httpService.get(
                "${url}services/rest/zktecoTerminal/info",
                params,
                null,
                { obj, _, _ ->
                    if (obj != null) {
                        if (obj.getBoolean("success")) {
                            val res = obj.getString("message")
                            val responseObj = UserInformation.convertJSONToObject(res)
                            successCallback(true, "", responseObj)
                        } else {
                            successCallback(false, obj.getString("message"), null)
                        }
                    }
                }, errorCallback
            )
        }
    }

    suspend fun deleteUser(userId: String, unique: String = "") {
        val list = userRepository.getEntity(userId.toLong())
        if (list.isNotEmpty()) {
            userRepository.delete(list[0])
            deleteFPForUser(userId)
        }
        if (unique.isNotEmpty()) {
            httpService.responseForCommand(unique)
        }
    }

    private fun deleteFPForUser(userId: String, unique: String = "") {
        MainApplication.lcdk.unregisterFingerPrint("$userId|0")
        MainApplication.lcdk.unregisterFingerPrint("$userId|1")
        MainApplication.lcdk.unregisterFingerPrint("$userId|2")
        MainApplication.lcdk.unregisterFingerPrint("$userId|3")
        MainApplication.lcdk.unregisterFingerPrint("$userId|4")
        MainApplication.lcdk.unregisterFingerPrint("$userId|5")
        MainApplication.lcdk.unregisterFingerPrint("$userId|6")
        MainApplication.lcdk.unregisterFingerPrint("$userId|7")
        MainApplication.lcdk.unregisterFingerPrint("$userId|8")
        MainApplication.lcdk.unregisterFingerPrint("$userId|9")
        if (unique.isNotEmpty()) {
            httpService.responseForCommand(unique)
        }
    }

    fun deleteFP(userId: String, finger: Int, unique: String = "") {
        MainApplication.lcdk.unregisterFingerPrint("$userId|$finger")
        if (unique.isNotEmpty()) {
            httpService.responseForCommand(unique)
        }
    }

    private fun processFPArray(arr: JSONArray?) {
        if (arr != null && arr.length() > 0) {
            for (c in 0 until arr.length()) {
                val obj = arr.getJSONObject(c)
                executor.execute {
                    processFPObj(obj)
                }
            }
        }
    }

    private fun processFPObj(obj: JSONObject, user: String? = null) {
        val userId = user ?: obj.optString("user", "")
        if (userId.isNotEmpty()) {
            for (i in 0..9) {
                val name = "fp$i"
                val template = obj.optString(name)
                if (template.isNotEmpty()) {
                    saveFPinDB(template, i, userId)
                } else {
                    MainApplication.lcdk.unregisterFingerPrint("$userId|$i")
                }
            }
        }
    }

    private fun saveFPinDB(template: String, finger: Int, id: String) {
        val key = "$id|$finger"
        MainApplication.lcdk.getFingerPrintTemplate(key)?.let {//Already registered
            return
        }
        MainApplication.lcdk.registerFingerPrint(key, template)
    }

    fun updateProjectUserValues(data: ArrayList<Pair<String, String>>, coroutineScope: CoroutineScope) {
        val commIds = JSONArray()

        coroutineScope.launch {
            for (d in data) {
                commIds.put(d.second)
                val resObj = Utils.parseResponseToJSON(d.first).obj
                if (resObj == null || !resObj.has("id")) {
                    continue
                }
                userRepository.updateProjectValues(
                    resObj.getLong("id"),
                    resObj.getBoolean("customerBasedProjectTime"),
                    resObj.getLong("timeEntryType")
                )
            }
        }

        httpService.responseForMultiCommand(commIds)
    }

    fun processCrossDayValues(data: JSONArray, coroutineScope: CoroutineScope){
        coroutineScope.launch {
            for (i in 0 until data.length()) {
                val obj = data.getJSONObject(i)
                if (!obj.has("userId")) {
                    continue
                }
                val userId = obj.getLong("userId")
                val crossDay = obj.optBoolean("crossDay", false)
                userRepository.updateCrossDay(userId, crossDay)
            }
        }
    }
}