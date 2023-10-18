package com.timo.timoterminal.service

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.utils.LocaleHelper
import org.koin.core.component.KoinComponent
import java.util.Locale


class SettingsService(
    val sharedPrefService: SharedPrefService,
    val httpService: HttpService
) : KoinComponent {

    private fun getLanguages(): Array<Locale> {
        return arrayOf(
            Locale("de", "DE"),
            Locale("en", "US")
        )
    }

    fun getLanguagesNames(): Array<String> {
        return getLanguages().map {
            Locale.forLanguageTag(it.language).displayLanguage
        }.toTypedArray()
    }

    fun changeLanguage(activity: Activity, lang: String) {
        val editor = sharedPrefService.getEditor()
        var chosenLanguageCode = ""
        for (loc in getLanguages()) {
            if (Locale.forLanguageTag(loc.language).displayLanguage.equals(lang)) {
                chosenLanguageCode = loc.language
                break
            }
        }
        editor.putString(SharedPreferenceKeys.LANGUAGE.name, chosenLanguageCode)
        editor.apply()


        LocaleHelper.setLocale(activity.applicationContext, chosenLanguageCode)
    }

    fun getCurrentLanguage(): String {
        val currentLocale = Locale.getDefault()
        return currentLocale.language
    }

    fun saveTimeZone(context: Context,timezone: String, callback: () -> Unit) {
        val editor = sharedPrefService.getEditor()
        editor.putString(SharedPreferenceKeys.TIMEZONE.name, timezone)
        editor.apply()

        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
        val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
        val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
        val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)

        val parameterMap = HashMap<String, String>()
        parameterMap.put("company", company!!)
        parameterMap.put("terminalId", terminalId.toString())
        parameterMap.put("token", token!!)
        parameterMap.put("timezone", timezone)

        httpService.post("${url}services/rest/zktecoTerminal/saveTimezone", parameterMap, context, { obj, _, _ ->
            callback()
        })

        val am : AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setTimeZone(timezone);
    }

    fun loadTimezone(context: Context) {
        val timezone = sharedPrefService.getString(SharedPreferenceKeys.TIMEZONE, "Europe/Berlin")
        val am : AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setTimeZone(timezone);
    }

}