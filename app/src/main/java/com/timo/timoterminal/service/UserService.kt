package com.timo.timoterminal.service

import android.util.Log
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.repositories.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
            val params = HashMap<String, String>()
            if (!url.isNullOrEmpty() && !company.isNullOrEmpty()) {
                params["company"] = company
                httpService.get("${url}services/rest/zktecoTerminal/loadAllUsers",
                    params,
                    null,
                    { _,arrResponse,_ ->
                    //Save it persistently offline
                    loadIntoDB(arrResponse, scope)
                })
            }
        }
    }

    private fun loadIntoDB(arrResponse : JSONArray?, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            val users = ArrayList<UserEntity>()
            for (i in 0 until arrResponse!!.length()) {
                val obj = arrResponse.getJSONObject(i)
                val userEntity : UserEntity = UserEntity.parseJsonToUserEntity(obj)
                users.add(userEntity)
            }
            userRepository.insertUserEntity(users)
        }
    }

}