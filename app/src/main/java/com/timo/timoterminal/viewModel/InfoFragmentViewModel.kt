package com.timo.timoterminal.viewModel

import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.service.UserService
import com.timo.timoterminal.utils.TimoRfidListener
import com.timo.timoterminal.utils.classes.SoundSource
import com.zkteco.android.lcdk.data.IFingerprintListener
import kotlinx.coroutines.launch
import java.util.Date


class InfoFragmentViewModel(
    private val userRepository: UserRepository,
    private val languageService: LanguageService,
    private val soundSource: SoundSource,
    private val userService: UserService,
    private val sharedPrefService: SharedPrefService
) : ViewModel(), TimoRfidListener, IFingerprintListener {

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

    var type = -1

    val liveRfidNumber: MutableLiveData<String> = MutableLiveData()
    val liveHideMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveShowMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveDismissInfoSheet: MutableLiveData<Boolean> = MutableLiveData()
    val liveRestartTimer: MutableLiveData<Boolean> = MutableLiveData()
    val liveMessage: MutableLiveData<String> = MutableLiveData()
    val liveErrorMessage: MutableLiveData<String> = MutableLiveData()
    val liveUser: MutableLiveData<Pair<Boolean, UserEntity?>> = MutableLiveData()
    val liveInfoSuccess: MutableLiveData<Bundle> = MutableLiveData()
    val liveDismissSheet: MutableLiveData<Boolean> = MutableLiveData()
    val liveShowSeconds: MutableLiveData<String> = MutableLiveData()
    val liveShowMessageSheet: MutableLiveData<String> = MutableLiveData()

    private suspend fun getUserEntityById(id: Long): UserEntity? {
        val users = userRepository.getEntity(id)
        if (users.isNotEmpty()) {
            return users[0]
        }
        return null
    }

    private suspend fun getUserForPin(pin: String): UserEntity? {
        val users = userRepository.getEntityByPIN(pin)
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
                liveUser.postValue(Pair(true, user))
                type = 2
            } else {
                soundSource.playSound(SoundSource.authenticationFailed)
                liveUser.postValue(Pair(true, null))
            }
        }
    }

    private fun loadUserInfoByCard(card: String) {
        viewModelScope.launch {
            val user = getUserEntityByCard(card)
            if (user != null) {
                soundSource.playSound(SoundSource.successSound)
                liveUser.postValue(Pair(true, user))
                type = 1
            } else {
                liveRfidNumber.postValue(card)
            }
        }
    }

    fun loadUserInfoByPin(pin: String) {
        viewModelScope.launch {
            val user = getUserForPin(pin)
            if (user != null) {
                soundSource.playSound(SoundSource.successSound)
                liveUser.postValue(Pair(true, user))
                type = 3
            } else {
                soundSource.playSound(SoundSource.authenticationFailed)
                liveUser.postValue(Pair(true, null))
            }
        }
    }

    fun loadUserInformation(user: UserEntity, from: Date?) {
        var date = Date()
        if (from != null) {
            date = from
        }
        userService.loadUserInformation(
            user, date,
            { success, errMessage, it ->
                if (success) {
                    val bundle = Bundle()
                    bundle.putParcelable("res", it)
                    bundle.putString("card", user.card.ifEmpty { user.id.toString() })
                    liveInfoSuccess.postValue(bundle)
                    timer.start()
                } else {
                    liveMessage.postValue(errMessage)
                }
                liveHideMask.postValue(true)
            }
        ) { _, _, _, _ ->
            liveHideMask.postValue(true)
            soundSource.playSound(SoundSource.failedSound)
            liveErrorMessage.postValue(
                languageService.getText("#TimoServiceNotReachable")
            )
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
        type = -1
        if (isTimerRunning) {
            timer.cancel()
        }
        liveDismissInfoSheet.postValue(true)
    }

    override fun onRfidRead(text: String) {
        viewModelScope.launch {
            val rfidCode = text.toLongOrNull(16)
            if (rfidCode != null) {
                soundSource.playSound(SoundSource.successSound)
                var oct = rfidCode.toString(8)
                while (oct.length < 9) {
                    oct = "0$oct"
                }
                oct = oct.reversed()
                liveShowMask.postValue(true)
                loadUserInfoByCard(oct)
            } else {
                soundSource.playSound(SoundSource.authenticationFailed)
                liveShowMessageSheet.postValue(languageService.getText("#VerificationFailed"))
            }
        }
    }

    fun getVersionName(): String {
        val version = sharedPrefService.getString(SharedPreferenceKeys.LAST_VERSION)
        if (version.isNullOrEmpty()) {
            return ""
        }
        return "${languageService.getText("#CurrentVersion")}: $version"
    }

    override fun onFingerprintPressed(template: ByteArray): Boolean {
        viewModelScope.launch {
            MainApplication.lcdk.identifyFingerPrint(template, 70).run {
                if (this.isNotEmpty()) {
                    soundSource.playSound(SoundSource.successSound)
                    liveShowMask.postValue(true)
                    loadUserInfoById(this.substring(0, this.length - 2))
                    return@launch
                }
            }
        }
        return true
    }

    override fun onFingerprintPressed(template: ByteArray, bmp: Bitmap?) {}
}