package com.timo.timoterminal.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.SettingsService

class SettingsFragmentViewModel(
    private val loginService: LoginService
) : ViewModel() {

    fun logout(context: Context) {
        loginService.logout(context,viewModelScope)
    }

}