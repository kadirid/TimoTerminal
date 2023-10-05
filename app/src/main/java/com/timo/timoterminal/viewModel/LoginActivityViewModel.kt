package com.timo.timoterminal.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.activities.LoginActivity
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.Utils
import kotlinx.coroutines.launch

class LoginActivityViewModel(
    private val loginService: LoginService,
    private val sharedPrefService: SharedPrefService
) : ViewModel() {

    fun loadPermissions(context: Context, callback: (worked: Boolean) -> Unit) {
        loginService.loadPermissions(viewModelScope, context, callback)
    }

    fun checkIfCredsAreLocallySaved(): Boolean {
        return sharedPrefService.checkIfCredsAreSaved()
    }

    fun loginCompany(
        company: String,
        username: String,
        password: String,
        customUrl: String?,
        context: Context,
        callback: () -> Unit?
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

    fun onResume(context: Context, callback: () -> Unit) {
        viewModelScope.launch {
            val saved: Boolean = checkIfCredsAreLocallySaved()
            if (saved) {
                Log.d(LoginActivity.TAG, "onResume: $saved")
                if (Utils.isOnline(context)) {
                    loginService.autoLogin(context, callback);
                } else {
                    //If we are offline, we just check whether the credentials are stored or not
                    callback()
                }
            }
        }
    }
}