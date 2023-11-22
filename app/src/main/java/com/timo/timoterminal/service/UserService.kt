package com.timo.timoterminal.service

import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.fragmentViews.UserSettingsFragment
import com.timo.timoterminal.repositories.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.koin.core.component.KoinComponent

class UserService(
    private val httpService: HttpService,
    private val sharedPrefService: SharedPrefService,
    private val userRepository: UserRepository
) : KoinComponent {


    fun loadUserFromServer(scope: CoroutineScope) {
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
                    })
            }
        }
    }

    private fun loadIntoDB(arrResponse: JSONArray?, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            val users = ArrayList<UserEntity>()
            for (i in 0 until arrResponse!!.length()) {
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
        }
    }

    fun sendUpdateRequest(
        paramMap: HashMap<String, String>,
        fragment: UserSettingsFragment,
        scope: CoroutineScope
    ) {
        scope.launch {
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, 0)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
                paramMap["company"] = company
                paramMap["token"] = token
                paramMap["terminalId"] = terminalId.toString()
                withContext(Dispatchers.IO) {
                    httpService.post("${url}services/rest/zktecoTerminal/updateUserCard",
                        paramMap,
                        fragment.requireContext(),
                        { obj, _, _ ->
                            if (obj != null) {
                                if (obj.getBoolean("success")) {
                                    val userEntity: UserEntity =
                                        UserEntity.parseJsonToUserEntity(obj)
                                    scope.launch(Dispatchers.IO) {
                                        userRepository.insertOne(userEntity)
                                    }
                                }
                                fragment.afterUpdate(
                                    obj.getBoolean("success"),
                                    obj.optString("message", "")
                                )
                            }
                            null
                        })
                }
            }
        }
    }
}