package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.service.SharedPrefService

class AttendanceFragmentViewModel(private val sharedPrefService: SharedPrefService): ViewModel(){

    fun getCompany(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
    }

    fun getURl():String? {
        return sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
    }

    fun getTerminalID() : Int? {
        return sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
    }
}