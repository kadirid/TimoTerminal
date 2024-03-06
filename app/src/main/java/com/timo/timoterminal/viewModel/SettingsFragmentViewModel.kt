package com.timo.timoterminal.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.UserService
import com.timo.timoterminal.utils.classes.SoundSource
import com.zkteco.android.core.sdk.sources.IHardwareSource
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SettingsFragmentViewModel(
    private val loginService: LoginService,
    private val userService: UserService,
    private val soundSource: SoundSource,
    private val hardwareSource: IHardwareSource
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

    fun showSystemUI() {
        viewModelScope.launch {
            hardwareSource.showSystemUI()
        }
    }
}