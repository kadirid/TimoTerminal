package com.timo.timoterminal.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.HeartbeatService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.TimoRfidListener
import com.timo.timoterminal.utils.classes.SoundSource
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.sources.IHardwareSource
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivityViewModel(
    private val userRepository: UserRepository,
    private val configRepository: ConfigRepository,
    private val heartbeatService: HeartbeatService,
    private val sharedPrefService: SharedPrefService,
) : ViewModel(), KoinComponent, FingerprintListener, TimoRfidListener {
    private val hardware: IHardwareSource by inject()
    private val soundSource: SoundSource by inject()

    val liveUserEntity: MutableLiveData<UserEntity> = MutableLiveData()

    fun initHeartbeatService(activity: MainActivity) {
        heartbeatService.initHeartbeatWorker(activity)
    }

    fun hideSystemUI() {
        viewModelScope.launch {
            val uiVisible = sharedPrefService.getBoolean(SharedPreferenceKeys.UI_VISIBLE, true)
            Log.d("MainActivityViewModel", "hideSystemUI: $uiVisible")
            if (uiVisible) {
                hardware.hideSystemUI()
                sharedPrefService.getEditor().putBoolean(SharedPreferenceKeys.UI_VISIBLE.toString(), false).commit()
            }
        }
    }

    fun showSystemUI() {
        viewModelScope.launch {
            val uiVisible = sharedPrefService.getBoolean(SharedPreferenceKeys.UI_VISIBLE, false)
            if (!uiVisible) {
                hardware.showSystemUI()
                sharedPrefService.getEditor().putBoolean(SharedPreferenceKeys.UI_VISIBLE.toString(), true).commit()
            }
        }
    }

    fun setSystemUI(visible: Boolean) {
        viewModelScope.launch {
            sharedPrefService.getEditor().putBoolean(SharedPreferenceKeys.UI_VISIBLE.toString(), visible).commit()
        }
    }

    fun reloadSoundSource() {
        viewModelScope.launch {
            soundSource.reloadForLanguage()
        }
    }

    suspend fun count() = userRepository.count()

    suspend fun getUserForLogin(login: String): UserEntity? {
        if (login.contains(":")) {
            return getAdmin(login)
        }
        val users = userRepository.getEntityByLogin(login)
        if (users.isNotEmpty()) {
            return users[0]
        }
        return null
    }

    suspend fun permission(name: String): String {
        return configRepository.getPermissionValue(name)
    }

    private fun getUserForCard(card: String) {
        viewModelScope.launch {
            val users = userRepository.getEntityByCard(card)
            processUserList(users)
        }
    }

    private suspend fun getUser(id: String) {
        val users = userRepository.getEntity(id.toLong())
        processUserList(users)
    }

    private fun processUserList(users: List<UserEntity>) {
        if (users.isNotEmpty() && users[0].seeMenu) {
            soundSource.playSound(SoundSource.successSound)
            liveUserEntity.postValue(users[0])
        } else {
            soundSource.playSound(SoundSource.authenticationFailed)
        }
    }

    private suspend fun getAdmin(login: String): UserEntity? {
        if (login.startsWith("admin:")) {
            if (login == "admin:terminal") {
                return UserEntity(
                    -1,
                    "Terminal",
                    "Admin",
                    "123456789",
                    permission("terminal.zkteco.admin.pin"),
                    login,
                    1L,
                    seeMenu = true,
                    assignedToTerminal = true
                )
            } else if (login == "admin:2004") {
                return UserEntity(
                    -2,
                    "TimO",
                    "Admin",
                    "987654321",
                    permission("terminal.zkteco.support.admin.pin"),
                    login,
                    1L,
                    seeMenu = true,
                    assignedToTerminal = true
                )
            }
        }
        return null
    }

    override fun onFingerprintPressed(
        fingerprint: String,
        template: String,
        width: Int,
        height: Int
    ) {
        viewModelScope.launch {
            // get Key associated to the fingerprint
            FingerprintService.identify(template)?.run {
                Log.d("FP Key", this)
                getUser(this.substring(0, this.length - 2))
            }
        }
    }

    override fun onRfidRead(rfidInfo: String) {
        val rfidCode = rfidInfo.toLongOrNull(16)
        if (rfidCode != null) {
            var oct = rfidCode.toString(8)
            while (oct.length < 9) {
                oct = "0$oct"
            }
            oct = oct.reversed()
            getUserForCard(oct)
        }
    }

    fun loadSoundForFP(finger: Int) {
        viewModelScope.launch {
            soundSource.loadForFP(finger)
        }
    }
}

