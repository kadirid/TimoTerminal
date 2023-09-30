package com.timo.timoterminal.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.ConfigEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.SharedPrefService
import kotlinx.coroutines.CoroutineScope
import java.util.UUID

class LoginActivityViewModel(
    private val loginService: LoginService,
    private val sharedPrefService: SharedPrefService
) : ViewModel() {

    fun loadPermissions(context: Context, callback: (worked: Boolean) -> Unit?) {
        loginService.loadPermissions(viewModelScope, context , callback)
    }

    fun checkIfCredsAreLocallySaved() : Boolean {
        return sharedPrefService.checkIfCredsAreSaved()
    }

    fun loginCompany(company: String,
                     username: String,
                     password: String,
                     customUrl: String?,
                     context: Context,
                     callback: () -> Unit?) {
        loginService.loginProcess(company, username, password, customUrl, context, viewModelScope, callback)
    }

    fun getUrl(): String? = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
}