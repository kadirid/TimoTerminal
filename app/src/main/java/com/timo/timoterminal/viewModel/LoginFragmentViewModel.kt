package com.timo.timoterminal.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.service.LoginService
import kotlinx.coroutines.launch

class LoginFragmentViewModel(
    private val loginService: LoginService
) : ViewModel(

) {

    fun loadPermissions(context: Context, callback: (worked: Boolean) -> Unit) {
        loginService.loadPermissions(viewModelScope, context, callback)
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
}