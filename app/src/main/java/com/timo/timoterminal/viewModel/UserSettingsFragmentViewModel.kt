package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.service.UserService
import com.zkteco.android.core.sdk.sources.IHardwareSource
import kotlinx.coroutines.launch

class UserSettingsFragmentViewModel(
    private val userService: UserService,
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService,
    private val languageService: LanguageService,
    private val hardware: IHardwareSource
) : ViewModel() {

    suspend fun getAllAsList() = userService.getAllAsList()
    fun getAll() = userService.getAllEntities

    fun assignUser(
        id: String,
        editor: Long,
        callback: () -> Unit?
    ) {
        viewModelScope.launch {
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val tId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID,-1)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)

            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
                val paramMap = mutableMapOf(Pair("company", company))
                paramMap["user"] = id
                paramMap["editor"] = "$editor"
                paramMap["token"] = token
                paramMap["terminalSN"] = hardware.serialNumber()
                paramMap["terminalId"] = tId.toString()
                httpService.post(
                    "${url}services/rest/zktecoTerminal/assignUser",
                    paramMap,
                    null,
                    { _, _, str ->
                        if ("true" == str) {
                            viewModelScope.launch {
                                val user = userService.getEntity(id.toLong())[0]
                                user.assignedToTerminal = true
                                userService.insertOne(user)
                                userService.getFPForUser(
                                    url,
                                    callback,
                                    id,
                                    company,
                                    token,
                                    tId
                                )
                            }
                        }
                    }, { e, res, context, output ->
                        HttpService.handleGenericRequestError(
                            e,
                            res,
                            context,
                            output,
                            languageService.getText("#TimoServiceNotReachable") + " " +
                                    languageService.getText("#ChangesReverted")
                        )
                    }
                )
            }
        }
    }
}