package com.timo.timoterminal.service

import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.UserRepository
import com.zkteco.android.core.sdk.service.FingerprintService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent

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
                        val userEntity: UserEntity = UserEntity.parseJsonToUserEntity(obj)
                        scope.launch {
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

    fun deleteFPForUser(userId: String, unique: String = "") {
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