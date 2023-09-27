package com.timo.timoterminal.service

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UserService : KoinComponent {
    private val httpService : HttpService by inject()

    fun loadUserFromServer() {
    }

}