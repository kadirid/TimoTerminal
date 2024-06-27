package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.service.SharedPrefService

class InternalTerminalSettingsFragmentViewModel(
    private val sharedPrefService: SharedPrefService
) : ViewModel() {

    fun setTimeOut(inputNumber: String = "5") {
        val seconds = inputNumber.toLong()

        sharedPrefService.getEditor().putLong(SharedPreferenceKeys.BOOKING_MESSAGE_TIMEOUT_IN_SEC.name, seconds).apply()
    }

}