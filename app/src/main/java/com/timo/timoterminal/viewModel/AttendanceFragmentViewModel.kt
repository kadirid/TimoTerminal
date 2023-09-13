package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import com.timo.timoterminal.repositories.ConfigRepository

class AttendanceFragmentViewModel(private val configRepository: ConfigRepository): ViewModel(){

    suspend fun getCompany(): String {
        return configRepository.getCompanyString()
    }

    suspend fun  getURl():String {
        return configRepository.getUrlString()
    }
}