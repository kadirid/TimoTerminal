package com.timo.timoterminal.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.BookingEntity
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.BookingService
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.TimoRfidListener
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.SoundSource
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch

class MBSheetFingerprintCardReaderViewModel(
    private val userRepository: UserRepository,
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService,
    private val bookingService: BookingService,
    private val languageService: LanguageService,
    private val soundSource: SoundSource
) : ViewModel(), TimoRfidListener, FingerprintListener {

    val liveDone: MutableLiveData<Boolean> = MutableLiveData()
    val liveShowErrorColor: MutableLiveData<Boolean> = MutableLiveData()
    val liveSetText: MutableLiveData<String> = MutableLiveData()
    val liveHideMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveShowMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveOfflineBooking: MutableLiveData<BookingEntity> = MutableLiveData()
    val liveShowInfo: MutableLiveData<Pair<String, String>> = MutableLiveData()

    var status = 0

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

    // maybe useful for fingerprint
    private suspend fun getUserEntity(id: Long): UserEntity? {
        val users = userRepository.getEntity(id)
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

    private suspend fun getUserEntityByLogin(login: String): UserEntity? {
        val users = userRepository.getEntityByLogin(login)
        if (users.isNotEmpty()) {
            return users[0]
        }
        return null
    }

    private fun sendBookingById(id: Long) {
        viewModelScope.launch {
            val user = getUserEntity(id)
            if (user != null) {
                val greg = Utils.getCal()
                liveShowInfo.postValue(Pair(Utils.getTimeFromGC(greg), user.name()))
                sendBooking(
                    BookingEntity(
                        user.card,
                        2,
                        Utils.getDateTimeFromGC(greg),
                        status
                    )
                )
            } else {
                errorNoUser("#VerificationFailed")
            }
        }
    }

    private fun sendBookingByCard(card: String) {
        viewModelScope.launch {
            val user = getUserEntityByCard(card)
            if (user != null) {
                val greg = Utils.getCal()
                liveShowInfo.postValue(Pair(Utils.getTimeFromGC(greg), user.name()))
                sendBooking(
                    BookingEntity(
                        user.card,
                        1,
                        Utils.getDateTimeFromGC(greg),
                        status
                    )
                )
            } else {
                errorNoUser("#Number unknown")
            }
        }
    }

    fun sendBookingByLogin(login: String, pin: String) {
        viewModelScope.launch {
            val user = getUserEntityByLogin(login)
            if (user != null && user.pin == pin) {
                val greg = Utils.getCal()
                liveShowInfo.postValue(Pair(Utils.getTimeFromGC(greg), user.name()))
                sendBooking(
                    BookingEntity(
                        user.card,
                        0,
                        Utils.getDateTimeFromGC(greg),
                        status
                    )
                )
            } else {
                errorNoUser("#VerificationFailed")
            }
        }
    }

    private fun errorNoUser(key: String) {
        soundSource.playSound(SoundSource.authenticationFailed)
        liveDone.postValue(true)
        liveSetText.postValue(languageService.getText(key))
        liveShowErrorColor.postValue(true)
    }

    // send all necessary information to timo to create a booking
    private fun sendBooking(
        entity: BookingEntity
    ) {
        viewModelScope.launch {
            val url = getURl()
            val company = getCompany()
            val terminalId = getTerminalID()
            val token = getToken()
            if (!company.isNullOrEmpty() && terminalId > 0 && entity.status > 0 && token.isNotEmpty()) {
                httpService.post(
                    "${url}services/rest/zktecoTerminal/booking",
                    mapOf(
                        Pair("card", entity.card),
                        Pair("firma", company),
                        Pair("date", entity.date),
                        Pair("funcCode", entity.status.toString()),
                        Pair("inputCode", entity.inputCode.toString()),
                        Pair("terminalId", terminalId.toString()),
                        Pair("token", token),
                        Pair("validate", "true")
                    ),
                    null,
                    { obj, _, _ ->
                        if (obj != null) {
                            liveDone.postValue(true)
                            RfidService.unregister()
                            FingerprintService.unregister()
                            liveSetText.postValue(obj.getString("message"))
                            if (!obj.getBoolean("success")) {
                                soundSource.playSound(SoundSource.failedSound)
                                liveShowErrorColor.postValue(true)
                            } else {
                                soundSource.playSound(SoundSource.successSound)
                            }
                        }
                        liveHideMask.postValue(true)
                    }, { _, _, _, _ ->
                        liveOfflineBooking.postValue(entity)
                        processOffline(entity)
                    }
                )
            }
        }
    }

    private fun processOffline(entity: BookingEntity) {
        viewModelScope.launch {
            soundSource.playSound(SoundSource.offlineSaved)
            RfidService.unregister()
            FingerprintService.unregister()
            bookingService.insertBooking(entity)
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
                sendBookingByCard(oct)
            }
        }
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
                val id = this.substring(0, this.length - 2).toLong()
                sendBookingById(id)
                return@launch
            }
            errorNoUser("#VerificationFailed")
        }
    }

}