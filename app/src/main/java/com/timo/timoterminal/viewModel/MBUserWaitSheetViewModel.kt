package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.service.UserService
import kotlinx.coroutines.launch
import org.json.JSONObject

class MBUserWaitSheetViewModel(
    private val userService: UserService,
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService,
    private val languageService: LanguageService,
) : ViewModel() {

    fun updateUser(
        paramMap: HashMap<String, String>,
        callback: (obj: JSONObject) -> Unit?
    ) {
        viewModelScope.launch {
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val tId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID,-1)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
                paramMap["company"] = company
                paramMap["token"] = token
                paramMap["terminalSN"] = MainApplication.lcdk.getSerialNumber()
                paramMap["terminalId"] = tId.toString()
                paramMap["terminalId"] = tId.toString()
                httpService.post("${url}services/rest/zktecoTerminal/updateUserCard",
                    paramMap,
                    null,
                    { obj, _, _ ->
                        if (obj != null) {
                            callback(obj)
                            if (obj.getBoolean("success")) {
                                val userEntity: UserEntity =
                                    UserEntity.parseJsonToUserEntity(obj)
                                viewModelScope.launch {
                                    userService.insertOne(userEntity)
                                }
                            }
                        }
                    }, { _, _, _, _ ->
                        val obj = JSONObject()
                        obj.putOpt(
                            "error", languageService.getText("#TimoServiceNotReachable") + " " +
                                    languageService.getText("#ChangesReverted")
                        )
                        callback(obj)
                    }
                )
            }
        }
    }

    fun getUserName(id: String?, callback: (name: String) -> Unit?){
        if (id != null){
            viewModelScope.launch {
                val users = userService.getEntity(id.toLong())
                if(users.isNotEmpty())
                    callback(users[0].name())
            }
        }
    }

    fun saveFP(
        id: String?,
        finger: Int,
        template: String,
        callback: (error: String) -> Unit?
    ) {
        if (id != null) {
            viewModelScope.launch {
                val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
                val tId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID,-1)
                val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
                val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)

                if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty() && id != "-1") {
                    val paramMap = mutableMapOf(Pair("fp", template))
                    paramMap["user"] = id
                    paramMap["fingerNo"] = "$finger"
                    paramMap["company"] = company
                    paramMap["token"] = token
                    paramMap["terminalSN"] = MainApplication.lcdk.getSerialNumber()
                    paramMap["terminalId"] = tId.toString()
                    httpService.post(
                        "${url}services/rest/zktecoTerminal/saveFP",
                        paramMap,
                        null,
                        { _, _, _ ->
                            callback("")
                        }, { _, _, _, _ ->
                            callback(
                                languageService.getText("#TimoServiceNotReachable") + " " +
                                        languageService.getText("#ChangesReverted")
                            )
                            MainApplication.lcdk.unregisterFingerPrint("$id|$finger")
                        }
                    )
                }
            }
        }
    }

    fun delFP(id: String?, finger: Int, callback: (error: String) -> Unit?) {
        if (id != null) {
            viewModelScope.launch {

                val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
                val tId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID,-1)
                val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
                val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)

                if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty() && id != "-1") {
                    val paramMap = mutableMapOf(Pair("user", id))
                    paramMap["fingerNo"] = "$finger"
                    paramMap["company"] = company
                    paramMap["token"] = token
                    paramMap["terminalSN"] = MainApplication.lcdk.getSerialNumber()
                    paramMap["terminalId"] = tId.toString()
                    httpService.post(
                        "${url}services/rest/zktecoTerminal/delFP",
                        paramMap,
                        null,
                        { _, _, _ ->
                            MainApplication.lcdk.unregisterFingerPrint("$id|$finger")
                            callback("")
                        }, { _, _, _, _ ->
                            callback(
                                languageService.getText("#TimoServiceNotReachable") +
                                        " " + languageService.getText("#ChangesReverted")
                            )
                        }
                    )
                }
            }
        }
    }
}