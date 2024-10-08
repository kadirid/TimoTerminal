package com.timo.timoterminal.viewModel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.SettingsService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.Utils
import kotlinx.coroutines.launch
import java.util.Locale

class LoginActivityViewModel(
    private val loginService: LoginService,
    private val sharedPrefService: SharedPrefService,
    private val settingsService: SettingsService,
    private val languageService: LanguageService
) : ViewModel() {
    val liveShowPleaseWaitMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveHidePleaseWaitMask: MutableLiveData<Boolean> = MutableLiveData()

    fun loadPermissions(context: Context, callback: (worked: Boolean) -> Unit) {
        loginService.loadPermissions(viewModelScope, context, callback)
    }

    private fun checkIfCredsAreLocallySaved(): Boolean {
        return sharedPrefService.checkIfCredsAreSaved()
    }

    fun onResume(context: Context, callback: () -> Unit) {
        viewModelScope.launch {
            val saved: Boolean = checkIfCredsAreLocallySaved()
            if (saved) {
                languageService.requestLanguageFromServer(viewModelScope, context)
                if (Utils.isOnline(context)) {
                    liveShowPleaseWaitMask.postValue(true)
                    loginService.autoLogin(context) {
                        callback()
                    }
                } else {
                    settingsService.loadTimezone(context)
                    //If we are offline, we just check whether the credentials are stored or not
                    callback()
                }
            }
        }
    }

    fun getLocal() = Locale(sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE, "de")!!)

}