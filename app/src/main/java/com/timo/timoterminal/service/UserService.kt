package com.timo.timoterminal.service

import android.content.Context
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.serviceUtils.classes.UserInformation
import com.timo.timoterminal.utils.classes.ResponseToJSON
import com.zkteco.android.core.sdk.service.FingerprintService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import java.util.Date

class UserService(
    private val httpService: HttpService,
    private val sharedPrefService: SharedPrefService,
    private val userRepository: UserRepository
) : KoinComponent {

    suspend fun getEntity(id: Long): List<UserEntity> = userRepository.getEntity(id)

    suspend fun insertOne(user: UserEntity) = userRepository.insertOne(user)

    val getAllEntities: Flow<List<UserEntity>> = userRepository.getAllEntities

    suspend fun getAllAsList(): List<UserEntity> = userRepository.getAllAsList()

    fun loadUsersFromServer(scope: CoroutineScope, unique: String = "") {
        scope.launch {
            //Load from server
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, 0)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
            val params = HashMap<String, String>()
            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
                params["company"] = company
                params["token"] = token
                params["terminalId"] = terminalId.toString()
                httpService.get("${url}services/rest/zktecoTerminal/loadAllUsers",
                    params,
                    null,
                    { _, arrResponse, _ ->
                        //Save it persistently offline
                        loadIntoDB(arrResponse, scope)
                        getFPFromServer(scope)
                        if (unique.isNotEmpty()) {
                            httpService.responseForCommand(unique)
                        }
                    })
            }
        }
    }

    private fun loadIntoDB(arrResponse: JSONArray?, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            val users = ArrayList<UserEntity>()
            if (arrResponse!!.length() > 0) {
                userRepository.deleteAll()
                FingerprintService.clear()
            }
            for (i in 0 until arrResponse.length()) {
                val obj = arrResponse.getJSONObject(i)
                val userEntity: UserEntity = UserEntity.parseJsonToUserEntity(obj)
                users.add(userEntity)
            }
            userRepository.insertUserEntity(users)
        }
    }

    fun deleteAllUsers(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            userRepository.deleteAll()
            FingerprintService.clear()
        }
    }

    private fun getFPFromServer(viewModelScope: CoroutineScope) {
        viewModelScope.launch {
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, 0)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)

            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
                val paramMap = mutableMapOf(Pair("company", company))
                paramMap["token"] = token
                paramMap["terminalId"] = terminalId.toString()
                httpService.get(
                    "${url}services/rest/zktecoTerminal/getAllFPForTerminal",
                    paramMap,
                    null,
                    { _, arr, _ ->
                        FingerprintService.clear()
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
        terminalId: Int,
        token: String
    ) {
        val paramMap = mutableMapOf(Pair("company", company))
        paramMap["user"] = id
        paramMap["token"] = token
        paramMap["terminalId"] = terminalId.toString()
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

    fun loadUserFromServer(scope: CoroutineScope, userId: String, unique: String = "") {
        //Load from server
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
        val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
        val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, 0)
        val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
        val params = HashMap<String, String>()
        if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
            params["company"] = company
            params["token"] = token
            params["terminalId"] = terminalId.toString()
            params["userId"] = userId
            httpService.get("${url}services/rest/zktecoTerminal/loadUser",
                params,
                null,
                { obj, _, _ ->
                    //Save it persistently offline
                    if (obj != null) {
                        scope.launch {
                            val userEntity: UserEntity = UserEntity.parseJsonToUserEntity(obj)
                            userRepository.insertOne(userEntity)
                            if (userEntity.assignedToTerminal) {
                                getFPForUser(url, { }, userId, company, terminalId, token)
                            }
                            if (unique.isNotEmpty()) {
                                httpService.responseForCommand(unique)
                            }
                        }
                    }
                }
            )
        }
    }

    /**
     * Load user information from server
     * successCallback contains a boolean which shows if the request was successful or not. If not, the message will be filled with a message from the server. Otherwise the objResponse will contain the UserInformation object.
     * errorCallback contains the exception, the response, the context and the output from the server.
     * @author Elias Kadiri
     * @param scope CoroutineScope
     * @param user UserEntity
     * @param successCallback (success: Boolean, message: String, objResponse: UserInformation?) -> Unit?
     * @param errorCallback (e: Exception?, response: Response?, context: Context?, output: ResponseToJSON?) -> Unit?
     * @return UserInformation
     * @since 0.0.1
     *
     */
    fun loadUserInformation(
        scope: CoroutineScope,
        user: UserEntity,
        successCallback: (success: Boolean, message: String, objResponse: UserInformation?) -> Unit?,
        errorCallback: ((e: Exception?, response: Response?, context: Context?, output: ResponseToJSON?) -> Unit?)
    ) {
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
        val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
        val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, 0)
        val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
        if (!company.isNullOrEmpty() && terminalId > 0 && !token.isNullOrEmpty()) {
            val params = HashMap<String, String>();
            params["card"] = user.card
            params["firma"] = company
            params["terminalId"] = terminalId.toString()
            params["token"] = token
            return httpService.get(
                "${url}services/rest/zktecoTerminal/info",
                params,
                null,
                { obj, arr, str ->
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
        FingerprintService.delete("$userId|0")
        FingerprintService.delete("$userId|1")
        FingerprintService.delete("$userId|2")
        FingerprintService.delete("$userId|3")
        FingerprintService.delete("$userId|4")
        FingerprintService.delete("$userId|5")
        FingerprintService.delete("$userId|6")
        FingerprintService.delete("$userId|7")
        FingerprintService.delete("$userId|8")
        FingerprintService.delete("$userId|9")
        if (unique.isNotEmpty()) {
            httpService.responseForCommand(unique)
        }
    }

    fun deleteFP(userId: String, finger: Int, unique: String = "") {
        FingerprintService.delete("$userId|$finger")
        if (unique.isNotEmpty()) {
            httpService.responseForCommand(unique)
        }
    }

    private fun processFPArray(arr: JSONArray?) {
        if (arr != null && arr.length() > 0) {
            for (c in 0 until arr.length()) {
                val obj = arr.getJSONObject(c)
                processFPObj(obj)
            }
        }
    }

    private fun processFPObj(obj: JSONObject) {
        val user = obj.optString("user", "")
        if (user.isNotEmpty()) {
            for (i in 0..9) {
                val name = "fp$i"
                val template = obj.optString(name)
                if (template.isNotEmpty()) {
                    saveFPinDB(template, i, user)
                } else {
                    FingerprintService.delete("$user|$i")
                }
            }
        }
    }

    private fun saveFPinDB(template: String, finger: Int, id: String) {
        val key = "$id|$finger"
        FingerprintService.getTemplate(key)?.let {//Already registered
            return
        }
        FingerprintService.load(key, template)
    }

}