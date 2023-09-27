package com.timo.timoterminal.service

import android.util.Log
import com.timo.timoterminal.entityClasses.ConfigEntity
import com.timo.timoterminal.repositories.ConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class LoginService(
    private val httpService : HttpService,
    private val configRepository: ConfigRepository
) : KoinComponent {
    fun getURlFromServer(company: String, callback: (url: String) -> Unit?, coroutineScope: CoroutineScope) {
        httpService.get(
            "https://www.timo24.de/timoadhttomin/baseurl",
            mapOf(Pair("company", company)),
            { _, _, url ->
                if (!url.isNullOrEmpty()) {
                    coroutineScope.launch {
                        callback(url)
                    }
                }
            }
        )
    }

    fun loadPermissions(coroutineScope: CoroutineScope, url: String, company: String, callback: () -> Unit?) {
        httpService.get(
            "${url}services/rest/zktecoTerminal/permission",
            mapOf(Pair("firma", company)),
            { _, array, _ ->
                if (array != null && array.length() > 0) {
                    for (i in 0 until array.length()){
                        val obj = array.getJSONObject(i)
                        addConfig(coroutineScope,
                            ConfigEntity(
                                ConfigRepository.TYPE_PERMISSION,
                                obj.getString("name"),
                                obj.getString("value")
                            )
                        )
                    }
                    //callback should only be called if the array is really loaded!
                    callback()
                }
            },
            {
                Log.e("ERROR", "loadPermissions: ${it.printStackTrace()}", )
            }
        )
    }

    fun addConfig(scope : CoroutineScope, config: ConfigEntity) {
        scope.launch {
            configRepository.insertConfigEntity(config)
        }
    }
}