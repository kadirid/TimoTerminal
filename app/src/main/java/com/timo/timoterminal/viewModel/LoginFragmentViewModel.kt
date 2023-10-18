package com.timo.timoterminal.viewModel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.SettingsService
import kotlinx.coroutines.launch

class LoginFragmentViewModel(
    private val loginService: LoginService,
    private val settingsService: SettingsService
) : ViewModel(

) {

    val languages = settingsService.getLanguagesNames()


    fun loadPermissions(context: Context, callback: (worked: Boolean) -> Unit) {
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

    fun saveLangAndTimezone(activity: Activity, language: String, timezone: String, callback: () -> Unit) {
        settingsService.changeLanguage(activity, language)
        settingsService.saveTimeZone(activity, timezone, callback)
    }
}