package com.timo.timoterminal.service

import android.content.Context
import com.timo.timoterminal.entityClasses.LanguageEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.LanguageRepository
import com.timo.timoterminal.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import java.util.concurrent.ConcurrentHashMap

class LanguageService(
    val sharedPrefService: SharedPrefService,
    val httpService: HttpService,
    private val languageRepository: LanguageRepository
) : KoinComponent {

    private val languages = ConcurrentHashMap<String, ConcurrentHashMap<String, String>>()

    fun requestLanguageFromServer(
        coroutineScope: CoroutineScope,
        context: Context,
        unique: String = ""
    ) {
        val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
        if (!company.isNullOrEmpty()) {
            if (Utils.isOnline(context)) {
                val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
                val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
                val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN, "") ?: ""
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        httpService.get("${url}services/rest/zktecoTerminal/language",
                            mapOf(
                                Pair("firma", company),
                                Pair("terminalId", "$terminalId"),
                                Pair("token", token)
                            ),
                            context,
                            { obj, _, _ ->
                                if (obj != null) {
                                    val values = Utils.parseResponseToJSON(obj.getString("values"))
                                    val list = mutableListOf<LanguageEntity>()
                                    if (values.array != null) {
                                        for (i in 0 until values.array.length()) {
                                            val oneValue = values.array.getJSONObject(i)
                                            list.add(
                                                LanguageEntity.convertJSONObjectToLanguageEntity(
                                                    oneValue
                                                )
                                            )
                                        }
                                    }
                                    coroutineScope.launch {
                                        withContext(Dispatchers.IO) {
                                            languageRepository.insertLanguageEntities(list)
                                        }
                                    }
                                    putLanguageValuesInMap(list)
                                    if(unique.isNotEmpty()){
                                        httpService.responseForCommand(unique)
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        val list = languageRepository.getAllAsList()
                        putLanguageValuesInMap(list)
                    }
                }
            }
        }
    }

    private fun putLanguageValuesInMap(list: List<LanguageEntity>) {
        for (entity in list) {
            var map = languages[entity.language]
            if (map == null) {
                map = ConcurrentHashMap()
            }
            map[entity.keyname] = entity.value
            languages[entity.language] = map
        }
    }

    fun getText(key: String): String {
        if (languages[sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE)] != null) {
            return (languages[sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE)]?.get(key))
                ?: ""
        }
        return ""
    }

    fun deleteAll(scope: CoroutineScope) {
        scope.launch {
            languageRepository.deleteAll()
        }
    }
}