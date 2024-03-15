package com.timo.timoterminal.viewModel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.SettingsService
import com.timo.timoterminal.service.SharedPrefService
import kotlinx.coroutines.launch

class LoginFragmentViewModel(
    private val loginService: LoginService,
    private val settingsService: SettingsService,
    private val languageService: LanguageService,
    private val sharedPrefService: SharedPrefService
) : ViewModel() {

    val languages = settingsService.getLanguagesNames()


    fun loadPermissions(context: Context, callback: (worked: Boolean) -> Unit) {
        languageService.requestLanguageFromServer(viewModelScope, context)
        loginService.loadPermissions(viewModelScope, context, callback)
    }

    fun loginCompany(
        company: String,
        username: String,
        password: String,
        customUrl: String?,
        context: Context,
        callback: (isNewTerminal: Boolean) -> Unit?
    ) {
        viewModelScope.launch {
            loginService.loginProcess(
                company,
                username,
                password,
                customUrl,
                context,
                callback
            )
        }
    }

    fun saveLangAndTimezone(activity: Activity, language: String, timezone: String, callback: (isOnline: Boolean) -> Unit) {
        settingsService.changeLanguage(activity, language)
        settingsService.setTimeZone(activity, timezone, callback)
    }

    fun onResume(callback: (isOnline: Boolean) -> Unit) {
        viewModelScope.launch {
            val saved: Boolean = !sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE, "").isNullOrEmpty()
            if (saved) {
                callback(true)
            }
        }
    }
}