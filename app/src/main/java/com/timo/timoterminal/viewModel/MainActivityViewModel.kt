package com.timo.timoterminal.viewModel

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.BuildConfig
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.HeartbeatService
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.ProjectPrefService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.TimoRfidListener
import com.timo.timoterminal.utils.classes.SoundSource
import com.zkteco.android.lcdk.data.IFingerprintListener
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivityViewModel(
    private val userRepository: UserRepository,
    private val configRepository: ConfigRepository,
    private val heartbeatService: HeartbeatService,
    private val sharedPrefService: SharedPrefService,
    private val projPrefService: ProjectPrefService,
    private val httpService: HttpService
) : ViewModel(), KoinComponent, IFingerprintListener, TimoRfidListener {
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
                MainApplication.lcdk.hideSystemUI()
                sharedPrefService.getEditor()
                    .putBoolean(SharedPreferenceKeys.UI_VISIBLE.toString(), false).commit()
            }
        }
    }

    fun showSystemUI() {
        viewModelScope.launch {
            val uiVisible = sharedPrefService.getBoolean(SharedPreferenceKeys.UI_VISIBLE, false)
            if (!uiVisible) {
                MainApplication.lcdk.showSystemUI()
                sharedPrefService.getEditor()
                    .putBoolean(SharedPreferenceKeys.UI_VISIBLE.toString(), true).commit()
            }
        }
    }

    fun setSystemUI(visible: Boolean) {
        viewModelScope.launch {
            sharedPrefService.getEditor()
                .putBoolean(SharedPreferenceKeys.UI_VISIBLE.toString(), visible).commit()
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
                    permission("terminal.zkteco.admin.pin"),
                    login,
                )
            } else if (login == "admin:2004") {
                return UserEntity(
                    -2,
                    "TimO",
                    "Admin",
                    permission("terminal.zkteco.support.admin.pin"),
                    login,
                )
            }
        }
        return null
    }

    override fun onRfidRead(text: String) {
        val rfidCode = text.toLongOrNull(16)
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

    fun hasUpdate(): Boolean {
        return sharedPrefService.getBoolean(SharedPreferenceKeys.HAS_UPDATE, false)
    }

    fun getDownloadUrl(): String {
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
        val terminalSN = MainApplication.lcdk.getSerialNumber()
        val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN, "") ?: ""
        val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
        val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY) ?: ""

        if (!url.isNullOrEmpty()) {
            return "${url}services/rest/zktecoTerminal/downloadAPK/?firma=${company}&token=${token}&terminalSN=${terminalSN}&terminalId=${terminalId}"
        }
        return ""
    }

    fun getFutureVersionName(): String {
        return sharedPrefService.getString(SharedPreferenceKeys.UPDATE_VERSION) ?: ""
    }

    fun checkAndSaveVersionName(activity: MainActivity) {
        val versionName = BuildConfig.VERSION_NAME
        val oldVersion = sharedPrefService.getString(SharedPreferenceKeys.LAST_VERSION, versionName)
        if (oldVersion != versionName) {
            val editor = sharedPrefService.getEditor()
            editor.putBoolean(SharedPreferenceKeys.HAS_UPDATE.name, false)
            editor.apply()

            httpService.responseForUpdate(versionName)
            activity.runOnUiThread {
                activity.getBinding().terminalHasUpdateButton.visibility = View.INVISIBLE
            }
        }

        sharedPrefService.getEditor().putString(SharedPreferenceKeys.LAST_VERSION.name, versionName)
            .apply()
    }

    override fun onFingerprintPressed(template: ByteArray): Boolean {
        viewModelScope.launch {
            MainApplication.lcdk.identifyFingerPrint(template, 70).run {
                if(this.isNotEmpty()) {
                    getUser(this.substring(0, this.length - 2))
                }
            }
        }
        return false
    }

    override fun onFingerprintPressed(template: ByteArray, bmp: Bitmap?) {}
    fun getProjectTimeTrackSetting() {
        viewModelScope.launch {
            projPrefService.getProjectTimeTrackSetting(true)
        }
    }
}

