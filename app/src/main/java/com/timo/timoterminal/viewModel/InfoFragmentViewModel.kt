package com.timo.timoterminal.viewModel

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.TimoRfidListener
import com.timo.timoterminal.utils.classes.SoundSource
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.sdk.service.FingerprintService
import kotlinx.coroutines.launch


class InfoFragmentViewModel(
    private val userRepository: UserRepository,
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService,
    private val languageService: LanguageService,
    private val soundSource: SoundSource
) : ViewModel(), TimoRfidListener, FingerprintListener {

    private var isTimerRunning = true
    private val timer = object : CountDownTimer(9999, 950) {
        override fun onTick(millisUntilFinished: Long) {
            isTimerRunning = true
            showSeconds(millisUntilFinished)
        }

        override fun onFinish() {
            hideUserInformation()
        }
    }

    val liveRfidNumber: MutableLiveData<String> = MutableLiveData()
    val liveHideMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveShowMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveDismissInfoSheet: MutableLiveData<Boolean> = MutableLiveData()
    val liveRestartTimer: MutableLiveData<Boolean> = MutableLiveData()
    val liveMessage: MutableLiveData<String> = MutableLiveData()
    val liveErrorMessage: MutableLiveData<String> = MutableLiveData()
    val liveUser: MutableLiveData<UserEntity?> = MutableLiveData()
    val liveInfoSuccess: MutableLiveData<Bundle> = MutableLiveData()
    val liveDismissSheet: MutableLiveData<Boolean> = MutableLiveData()
    val liveShowSeconds: MutableLiveData<String> = MutableLiveData()

    private fun getCompany(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
    }

    private fun getURl(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
    }

    private fun getTerminalID(): Int {
        return sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
    }

    private fun getToken(): String {
        return sharedPrefService.getString(SharedPreferenceKeys.TOKEN, "") ?: ""
    }

    private suspend fun getUserEntityById(id: Long): UserEntity? {
        val users = userRepository.getEntity(id)
        if (users.isNotEmpty()) {
            return users[0]
        }
        return null
    }

    private suspend fun getUserForLogin(login: String): UserEntity? {
        val users = userRepository.getEntityByLogin(login)
        if (users.isNotEmpty()) {
            return users[0]
        }
        return null
    }

    private suspend fun getUserEntityByCard(card: String): UserEntity? {
        val users = userRepository.getEntityByCard(card)
        if (users.isNotEmpty()) {
            return users[0]
        }
        return null
    }

    private fun loadUserInfoById(id: String) {
        viewModelScope.launch {
            val user = getUserEntityById(id.toLong())
            if (user != null) {
                soundSource.playSound(SoundSource.successSound)
                liveUser.postValue(user)
            }
        }
    }

    private fun loadUserInfoByCard(card: String) {
        viewModelScope.launch {
            val user = getUserEntityByCard(card)
            if (user != null) {
                soundSource.playSound(SoundSource.successSound)
                liveUser.postValue(user)
            } else {
                liveRfidNumber.postValue(card)
            }
        }
    }

    fun loadUserInfoByLoginAndPin(login: String, pin: String) {
        viewModelScope.launch {
            val user = getUserForLogin(login)
            if (user != null && user.pin == pin) {
                soundSource.playSound(SoundSource.successSound)
                liveUser.postValue(user)
            } else {
                soundSource.playSound(SoundSource.authenticationFailed)
            }
        }
    }

    fun loadUserInformation(user: UserEntity) {
        viewModelScope.launch {
            val url = getURl()
            val company = getCompany()
            val terminalId = getTerminalID()
            val token = getToken()
            if (!company.isNullOrEmpty() && terminalId > 0 && token.isNotEmpty()) {
                httpService.get(
                    "${url}services/rest/zktecoTerminal/info",
                    mapOf(
                        Pair("card", user.card),
                        Pair("firma", company),
                        Pair("terminalId", terminalId.toString()),
                        Pair("token", token)
                    ),
                    null,
                    { obj, _, _ ->
                        if (obj != null) {
                            if (obj.getBoolean("success")) {
                                val res = obj.getString("message")
                                val bundle = Bundle()
                                bundle.putString("res", res)
                                bundle.putString("card", user.card)
                                liveInfoSuccess.postValue(bundle)
                                timer.start()
                            } else {
                                liveMessage.postValue(obj.getString("message"))
                            }
                            liveHideMask.postValue(true)
                        }
                    }, { _, _, _, _ ->
                        liveHideMask.postValue(true)
                        soundSource.playSound(SoundSource.failedSound)
                        liveErrorMessage.postValue(
                            languageService.getText("#TimoServiceNotReachable")
                        )
                    }
                )
            }
        }
    }

    private fun showSeconds(millisUntilFinished: Long) {
        liveShowSeconds.postValue((millisUntilFinished / 950).toString())
    }

    private fun hideUserInformation() {
        liveDismissSheet.postValue(true)
        isTimerRunning = false
    }

    fun restartTimer() {
        timer.cancel()
        timer.start()
        liveRestartTimer.postValue(true)
    }

    fun dismissInfoSheet() {
        if (isTimerRunning) {
            liveDismissInfoSheet.postValue(true)
            timer.cancel()
        }
    }

    override fun onFingerprintPressed(
        fingerprint: String,
        template: String,
        width: Int,
        height: Int
    ) {
        viewModelScope.launch {
            Log.d("FP", fingerprint)
            // get Key associated to the fingerprint
            FingerprintService.identify(template)?.run {
                soundSource.playSound(SoundSource.successSound)
                Log.d("FP Key", this)
                liveShowMask.postValue(true)
                loadUserInfoById(this.substring(0, this.length - 2))
                return@launch
            }
            soundSource.playSound(SoundSource.authenticationFailed)
        }
    }

    override fun onRfidRead(rfidInfo: String) {
        viewModelScope.launch {
            val rfidCode = rfidInfo.toLongOrNull(16)
            if (rfidCode != null) {
                soundSource.playSound(SoundSource.successSound)
                var oct = rfidCode.toString(8)
                while (oct.length < 9) {
                    oct = "0$oct"
                }
                oct = oct.reversed()
                liveShowMask.postValue(true)
                loadUserInfoByCard(oct)
            }
        }
    }
}