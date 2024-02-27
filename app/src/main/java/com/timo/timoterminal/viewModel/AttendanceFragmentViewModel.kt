package com.timo.timoterminal.viewModel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.fragmentViews.AttendanceFragment
import com.timo.timoterminal.modalBottomSheets.MBBookingMessageSheet
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.TimoRfidListener
import com.timo.timoterminal.utils.classes.SoundSource
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.sdk.service.FingerprintService
import kotlinx.coroutines.launch

class AttendanceFragmentViewModel(
    private val sharedPrefService: SharedPrefService,
    private val userRepository: UserRepository,
    private val soundSource: SoundSource,
    private val languageService: LanguageService
) : ViewModel(), TimoRfidListener, FingerprintListener {

    val liveUserCard: MutableLiveData<Pair<String, Int>> = MutableLiveData()
    val liveErrorMsg: MutableLiveData<String> = MutableLiveData()

    fun getCompany(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
    }

    fun getURl(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
    }

    fun getTerminalID(): Int {
        return sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
    }

    fun getToken(): String {
        return sharedPrefService.getString(SharedPreferenceKeys.TOKEN, "") ?: ""
    }

    fun showMessage(
        fragment: AttendanceFragment,
        card: String,
        funcCode: Int,
        success: Boolean,
        message: String
    ) {
        viewModelScope.launch {
            val user = getUserByCard(card)
            if (user != null) {
                fragment.activity?.runOnUiThread {
                    val bookingMessage = MBBookingMessageSheet()
                    val bundle = Bundle()
                    bundle.putInt("funcCode", funcCode)
                    bundle.putString("name", user.name())
                    bundle.putString("message", message)
                    bundle.putBoolean("success", success)
                    bookingMessage.arguments = bundle
                    bookingMessage.show(fragment.parentFragmentManager, MBBookingMessageSheet.TAG)
                }
            }
        }
    }

    private suspend fun getUser(id: Long): UserEntity? {
        val users = userRepository.getEntity(id)
        if (users.isNotEmpty()) {
            return users[0]
        }
        return null
    }

    private suspend fun getUserByCard(card: String): UserEntity? {
        val users = userRepository.getEntityByCard(card)
        if (users.isNotEmpty()) {
            return users[0]
        }
        return null
    }

    fun loadSoundForAttendance() {
        viewModelScope.launch {
            soundSource.loadForAttendance()
        }
    }

    override fun onFingerprintPressed(
        fingerprint: String,
        template: String,
        width: Int,
        height: Int
    ) {
        Log.d("FP", fingerprint)
        // get Key associated to the fingerprint
        viewModelScope.launch {
            FingerprintService.identify(template)?.run {
            Log.d("FP Key", this)
            val id = this.substring(0, this.length - 2).toLong()
                val user = getUser(id)
                if (user != null) {
                    liveUserCard.postValue(Pair(user.card, 2))
                }
                return@launch
            }
            liveErrorMsg.postValue(languageService.getText("#VerificationFailed"))
        }
    }

    // get code of scanned card
    override fun onRfidRead(rfidInfo: String) {
        viewModelScope.launch {
            val rfidCode = rfidInfo.toLongOrNull(16)
            if (rfidCode != null) {
                var oct = rfidCode.toString(8)
                while (oct.length < 9) {
                    oct = "0$oct"
                }
                oct = oct.reversed()
                val user = getUserByCard(oct)
                if (user != null) {
                    liveUserCard.postValue(Pair(oct, 1))
                } else {
                    liveErrorMsg.postValue(languageService.getText("#VerificationFailed"))
                }
            }
        }
    }
}