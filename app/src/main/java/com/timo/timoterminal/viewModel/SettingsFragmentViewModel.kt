package com.timo.timoterminal.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.UserService
import com.timo.timoterminal.utils.classes.SoundSource
import kotlinx.coroutines.launch

class SettingsFragmentViewModel(
    private val loginService: LoginService,
    private val userService: UserService,
    private val soundSource: SoundSource
) : ViewModel() {

    fun logout(context: Context) {
        loginService.logout(context)
    }

    fun actualizeTerminal(context: Context){
        loginService.loadPermissions(viewModelScope, context) { _ -> }
        userService.loadUsersFromServer(viewModelScope)
    }

    fun resetTerminal(context: Context){
        loginService.resetTerminal(context, viewModelScope)
    }

    fun loadSound() {
        viewModelScope.launch {
            soundSource.loadForFP()
        }
    }
}