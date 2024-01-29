package com.timo.timoterminal.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.UserService

class SettingsFragmentViewModel(
    private val loginService: LoginService,
    private val userService: UserService
) : ViewModel() {

    fun logout(context: Context) {
        loginService.logout(context,viewModelScope)
    }

    fun actualizeTerminal(context: Context){
        loginService.loadPermissions(viewModelScope, context) { _ -> }
        userService.loadUserFromServer(viewModelScope)
    }

    fun resetTerminal(context: Context){
        loginService.resetTerminal(context, viewModelScope)
    }
}