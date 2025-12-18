package com.timo.timoterminal.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

class UserSettingsFragmentViewModel(
    private val userService: UserService,
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService,
    private val languageService: LanguageService
) : ViewModel() {
    private val _items = MutableLiveData<List<UserEntity>>(emptyList())
    val items: LiveData<List<UserEntity>> = _items
    private var currentPage = 0
    private var isLoading = false

    fun loadMoreItems() {
        if (!isLoading) {
            isLoading = true
            viewModelScope.launch {
                // Fetch data from a remote source or local database
                val newItems = userService.getPageAsList(currentPage)
                _items.value = _items.value?.plus(newItems) ?: newItems
                currentPage++
                isLoading = false
            }
        }
    }

    suspend fun getAllAsList() = userService.getAllAsList()

    fun assignUser(
        id: String,
        editor: Long,
        callback: () -> Unit?
    ) {
        viewModelScope.launch {
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val tId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)

            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
                val paramMap = mutableMapOf(Pair("company", company))
                paramMap["user"] = id
                paramMap["editor"] = "$editor"
                paramMap["token"] = token
                paramMap["terminalSN"] = MainApplication.lcdk.getSerialNumber()
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

    fun reloadUser(userId: String) {
        viewModelScope.launch {
            val user = userService.getEntity(userId.toLong())[0]
            _items.value.let {
                val index = it?.indexOfFirst { it.id.toString() == userId }
                if (index != null && index >= 0) {
                    val mutableList = it.toMutableList()
                    mutableList[index] = user
                    _items.value = mutableList
                }
            }
        }
    }
}