package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.ConfigEntity
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.service.LoginService

class LoginActivityViewModel(
    private val loginService: LoginService,
    private val configRepository: ConfigRepository
) : ViewModel() {

    fun getURlFromServer(company: String, callback: (url: String) -> Unit?) {
        loginService.getURlFromServer(company, callback, viewModelScope)
    }

    fun loadPermissions(url: String, company: String, callback: () -> Unit?) {
        loginService.loadPermissions(viewModelScope, url, company, callback)
    }

    fun addConfig(config: ConfigEntity) {
        loginService.addConfig(viewModelScope, config)
    }

    suspend fun getCompany(): ConfigEntity? = configRepository.getCompany()

    suspend fun getUrl(): ConfigEntity? = configRepository.getUrl()
}