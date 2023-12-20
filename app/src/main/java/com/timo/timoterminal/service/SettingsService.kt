package com.timo.timoterminal.service

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.utils.LocaleHelper
import com.timo.timoterminal.utils.Utils
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

    fun changeLanguage(context:Context?, lang: String) {
        val editor = sharedPrefService.getEditor()
        var chosenLanguageCode = ""
        for (loc in getLanguages()) {
            if (Locale.forLanguageTag(loc.language).displayLanguage.equals(lang) || loc.language.equals(lang)) {
                chosenLanguageCode = loc.language
                break
            }
        }
        editor.putString(SharedPreferenceKeys.LANGUAGE.name, chosenLanguageCode)
        editor.apply()


        if (context != null ) {
            LocaleHelper.setLocale(context, chosenLanguageCode)
        }
    }

    fun getCurrentLanguage(): String {
        val currentLocale = Locale.getDefault()
        return currentLocale.language
    }

    fun setTimeZone(context: Context,timezone: String, callback: (isOnline: Boolean) -> Unit) {
        val editor = sharedPrefService.getEditor()
        editor.putString(SharedPreferenceKeys.TIMEZONE.name, timezone)
        editor.apply()

        val am : AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setTimeZone(timezone)

        callback(Utils.isOnline(context))
    }

    fun saveTimeZone(context: Context){
        val timezone: String? = sharedPrefService.getString(SharedPreferenceKeys.TIMEZONE, "Europe/Berlin")
        if(timezone != null){
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)

            val parameterMap = HashMap<String, String>()
            parameterMap["company"] = company!!
            parameterMap["terminalId"] = terminalId.toString()
            parameterMap["token"] = token!!
            parameterMap["timezone"] = timezone

            httpService.post("${url}services/rest/zktecoTerminal/saveTimezone", parameterMap, context, { _, _, _ ->})
        }
    }

    fun loadTimezone(context: Context) {
        val timezone = sharedPrefService.getString(SharedPreferenceKeys.TIMEZONE, "Europe/Berlin")
        val am : AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            am.setTimeZone(timezone)
        } catch (e: Exception) {
            //If we set no correct timezone identifier, just set Europe/Berlin as standard
            sharedPrefService.getEditor().putString(SharedPreferenceKeys.TIMEZONE.name, "Europe/Berlin")
            am.setTimeZone("Europe/Berlin")
        }
    }

}