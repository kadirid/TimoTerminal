package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.ConfigEntity
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.service.HttpService
import kotlinx.coroutines.launch

class LoginActivityViewModel(
    private val httpService: HttpService,
    private val configRepository: ConfigRepository
) : ViewModel() {

    fun getURlFromServer(company: String, callback: (url: String) -> Unit?) {
        httpService.get(
            "https://www.timo24.de/timoadmin/baseurl",
            mapOf(Pair("company", company)),
            { _, _, url ->
                if (!url.isNullOrEmpty()) {
                    callback(url)
                }
            },
            {}
        )
    }

    fun loadPermissions(url: String, company: String, callback: () -> Unit?) {
        httpService.get(
            "${url}services/rest/zktecoTerminal/permission",
            mapOf(Pair("firma", company)),
            { _, array, _ ->
                if (array != null && array.length() > 0) {
                    for (i in 0 until array.length()){
                        val obj = array.getJSONObject(i)
                        addConfig(
                            ConfigEntity(
                                ConfigRepository.TYPE_PERMISSION,
                                obj.getString("name"),
                                obj.getString("value")
                            )
                        )
                    }
                }
                callback()
            },
            {}
        )
    }

    fun addConfig(config: ConfigEntity) {
        viewModelScope.launch {
            configRepository.insertConfigEntity(config)
        }
    }

    suspend fun getCompany(): ConfigEntity? = configRepository.getCompany()

    suspend fun getUrl(): ConfigEntity? = configRepository.getUrl()
}